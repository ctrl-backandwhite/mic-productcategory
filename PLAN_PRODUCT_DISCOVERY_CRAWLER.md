# Plan de Implementación — Product Discovery Crawler (Multi-Estrategia)

> **Microservicio:** mic-productcategory  
> **Objetivo:** Escalar de ~6,000 → 100,000+ productos descubiertos en BD local  
> **API:** CJ Dropshipping API v2.0 — `/product/listV2` + `/product/list`  
> **Plan CJ:** Plus (2 req/s)  
> **Fecha:** Abril 2026

---

## 0. Diagnóstico: ¿Por qué solo ~6,000 productos?

### Causa raíz
El sistema actual usa `getProductList(page, size)` con **cero filtros** — solo pagina `pageNum` y `pageSize`.

```
listV2?pageNum=1&pageSize=100  → 100 productos
listV2?pageNum=2&pageSize=100  → 100 productos
...
listV2?pageNum=60&pageSize=100 → ~6,000 productos → se agota
```

### ¿Por qué se agota?

| Factor | Explicación |
|--------|-------------|
| **Elasticsearch window** | `/product/listV2` usa Elasticsearch internamente. ES tiene un límite `max_result_window` (típicamente 10,000). Sin filtros, devuelve un subconjunto "default". |
| **`page` max = 1000** | La API dice `page max 1000`, pero en la práctica sin keyword/category la profundidad efectiva es mucho menor. |
| **No hay keyword** | Sin `keyWord`, CJ devuelve un subconjunto genérico. Con keyword distinto, devuelve resultados completamente diferentes. |
| **No hay categoryId** | Sin categoría, CJ no puede segmentar el índice ES — devuelve un "best match" global limitado. |

### Lo que el Plan Plus NO cambia
- ❌ No desbloquea más productos en el catálogo
- ❌ No aumenta los resultados de búsqueda
- ✅ Solo da más req/s (2 en vez de 1) y más estabilidad

### La solución real
**Cambiar la estrategia de extracción**: en vez de paginar sin filtros, usar múltiples dimensiones de consulta para "barrer" el catálogo CJ completo.

---

## 1. Arquitectura del Crawler Multi-Estrategia

### 1.1 Diseño general

```
┌─────────────────────────────────────────────────────────┐
│            PRODUCT DISCOVERY CRAWLER                     │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Estrategia 1 │  │ Estrategia 2 │  │ Estrategia 3 │  │
│  │ By Category  │  │ By Keyword   │  │ By Time      │  │
│  │ (principal)  │  │ (ampliación) │  │ (incremental)│  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
│         │                  │                  │          │
│         └──────────┬───────┘──────────────────┘          │
│                    ▼                                     │
│         ┌──────────────────┐                             │
│         │ Deduplication    │  PID ya en BD → skip        │
│         │ + PID Registry   │  PID nuevo → persist        │
│         └──────────┬───────┘                             │
│                    ▼                                     │
│         ┌──────────────────┐                             │
│         │ Discovery Queue  │  Tabla: discovered_pids     │
│         │ (persist PIDs)   │  status: NEW → SYNCED       │
│         └──────────┬───────┘                             │
│                    ▼                                     │
│         ┌──────────────────┐                             │
│         │ Detail Enricher  │  /product/query?pid={pid}   │
│         │ (existing Full   │  Ya implementado            │
│         │  Sync pipeline)  │                             │
│         └──────────────────┘                             │
└─────────────────────────────────────────────────────────┘
```

### 1.2 Separación de responsabilidades

| Componente | Responsabilidad |
|------------|-----------------|
| **ProductDiscoveryUseCase** | Orquesta las 3 estrategias, deduplicación, persiste PIDs nuevos |
| **DiscoveryByCategoryStrategy** | Itera TODAS las categorías L3 → listV2 paginado |
| **DiscoveryByKeywordStrategy** | Itera una lista configurable de keywords → listV2 paginado |
| **DiscoveryByTimeStrategy** | Consulta productos nuevos en CJ por `timeStart`/`timeEnd` incremental |
| **DiscoveredPidRepository** | Tabla `discovered_pids` — registro de PIDs descubiertos y su estado |
| **CjDropshippingClient** (existente) | Se extiende con nuevo método `getProductListFiltered(...)` |
| **Full Sync Pipeline** (existente) | Procesa los PIDs descubiertos → detalle + variantes + inventario |

---

## 2. Estrategias de Descubrimiento (detalle)

### 2.1 Estrategia 1 — By Category (PRINCIPAL)

**Concepto:** La API `getCategory` devuelve ~200+ categorías L3. Consultar `/product/listV2?categoryId={L3}` por cada una multiplica enormemente los resultados.

**Flujo:**

```
1. SELECT categoryId FROM categories WHERE level = 3
   (ya sincronizadas en BD)

2. Para cada categoryId:
   page = 1
   loop:
     GET /product/listV2?categoryId={catId}&page={page}&size=100
       &orderBy=3&sort=desc  (por createTime descendente)
     
     extraer PIDs de la respuesta
     filtrar contra discovered_pids (ya visto → skip)
     insertar nuevos en discovered_pids (status=NEW)
     
     if page >= totalPages OR page >= 1000 → break
     page++
     
     wait (rate limiter: 500ms entre requests)

3. Log: "Category {catName}: discovered {N} new PIDs, {total} total"
```

**Estimación de alcance:**

| Dato | Valor |
|------|-------|
| Categorías L3 estimadas | ~200-300 |
| Productos promedio por categoría | ~500-2,000 |
| Páginas por categoría (size=100) | ~5-20 |
| Total requests estimado | ~3,000-6,000 |
| Tiempo a 2 req/s | ~25-50 min |
| **Productos descubiertos estimados** | **50,000 - 100,000+** |

**Filtros adicionales opcionales por categoría:**

```
countryCode=US      → solo con stock US (más relevante para envío rápido)
countryCode=CN      → stock China (más variedad)
verifiedWarehouse=1 → solo inventario verificado
```

### 2.2 Estrategia 2 — By Keyword (AMPLIACIÓN)

**Concepto:** Incluso con categorías, hay productos que CJ indexa mejor por keyword. Una lista configurable de ~200 keywords de nicho amplía la cobertura.

**Flujo:**

```
1. Cargar keywords desde config:
   app.cj.discovery.keywords:
     - hoodie, shirt, pants, dress, jacket, sweater, ...
     - phone case, laptop bag, tablet stand, ...
     - kitchen, bathroom, bedroom, garden, ...
     - toy, doll, puzzle, game, ...
     - jewelry, ring, necklace, bracelet, ...
     - (configurable, YAML o tabla BD)

2. Para cada keyword:
   page = 1
   loop:
     GET /product/listV2?keyWord={kw}&page={page}&size=100
       &orderBy=3&sort=desc
     
     extraer PIDs → deduplicar → insertar nuevos
     if page >= totalPages OR page >= 200 → break  (cap conservador)
     page++

3. Log resultados por keyword
```

**Estimación:**

| Dato | Valor |
|------|-------|
| Keywords configurados | ~100-200 |
| Productos únicos por keyword | ~200-1,000 (con mucho solapamiento) |
| **PIDs nuevos netos** (no vistos en Estrategia 1) | **10,000 - 30,000** |

### 2.3 Estrategia 3 — By Time (INCREMENTAL DIARIO)

**Concepto:** CJ tiene campos `timeStart` / `timeEnd` (timestamp ms) en listV2 que filtran por fecha de listado. Ejecutar diariamente para descubrir productos nuevos sin re-barrer todo.

**Flujo:**

```
1. Leer último timestamp procesado:
   SELECT last_crawled_at FROM discovery_state WHERE strategy = 'BY_TIME'

2. timeStart = last_crawled_at (o NOW - 24h si es primera vez)
   timeEnd   = NOW

3. Loop paginado:
   GET /product/listV2?timeStart={ms}&timeEnd={ms}&page={page}&size=100
     &orderBy=3&sort=asc  (cronológico ascendente para no perder nada)
   
   extraer PIDs → deduplicar → insertar
   if page >= totalPages → break
   page++

4. UPDATE discovery_state SET last_crawled_at = {timeEnd}
```

**Estimación:**

| Dato | Valor |
|------|-------|
| Productos nuevos/día en CJ | ~100-500 (estimado) |
| Requests diarios | ~5-20 |
| **Productos nuevos descubiertos/mes** | **3,000 - 15,000** |

### 2.4 Uso combinado de endpoints

| Endpoint | Uso en discovery | Ventaja | Límite |
|----------|-----------------|---------|--------|
| **`/product/listV2`** | Estrategias 1, 2, 3 | Elasticsearch, más filtros, `timeStart/timeEnd` | page max 1000, size max 100 |
| **`/product/list`** (v1) | Fallback si listV2 falla | `createTimeFrom/createTimeTo`, `pageSize` max 200 | 1000 req/día para free (no aplica a Plus) |

> **Nota:** Usaremos `/product/listV2` como endpoint principal. `/product/list` se reserva como fallback o para filtros exclusivos (`deliveryTime`, `brandOpenId`, `minListedNum`).

---

## 3. Modelo de Datos

### 3.1 Nueva tabla: `discovered_pids`

```sql
-- Registro de cada PID descubierto por el crawler
CREATE TABLE discovered_pids (
    id            VARCHAR(36)  PRIMARY KEY DEFAULT gen_random_uuid(),
    pid           VARCHAR(200) NOT NULL UNIQUE,       -- CJ product ID
    category_id   VARCHAR(200),                        -- categoría L3 donde se descubrió
    keyword       VARCHAR(200),                        -- keyword que lo descubrió (si aplica)
    strategy      VARCHAR(20)  NOT NULL,               -- BY_CATEGORY | BY_KEYWORD | BY_TIME
    status        VARCHAR(20)  NOT NULL DEFAULT 'NEW', -- NEW | QUEUED | SYNCED | FAILED | SKIPPED
    name_en       VARCHAR(500),                        -- nombre del listado (para referencia rápida)
    sell_price    DECIMAL(10,2),                        -- precio del listado
    error_count   INT          NOT NULL DEFAULT 0,
    last_error    TEXT,
    discovered_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    synced_at     TIMESTAMP,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_discovered_pids_status ON discovered_pids(status);
CREATE INDEX idx_discovered_pids_pid ON discovered_pids(pid);
CREATE INDEX idx_discovered_pids_strategy ON discovered_pids(strategy);
CREATE INDEX idx_discovered_pids_discovered_at ON discovered_pids(discovered_at);
```

### 3.2 Nueva tabla: `discovery_state`

```sql
-- Estado persistente de cada estrategia para reanudación
CREATE TABLE discovery_state (
    strategy         VARCHAR(30) PRIMARY KEY,  -- BY_CATEGORY | BY_KEYWORD | BY_TIME
    last_crawled_at  TIMESTAMP,                -- para BY_TIME: último timestamp procesado
    last_category_id VARCHAR(200),             -- para BY_CATEGORY: última categoría procesada
    last_keyword     VARCHAR(200),             -- para BY_KEYWORD: último keyword procesado
    last_page        INT DEFAULT 0,            -- última página procesada (para reanudar)
    total_discovered INT DEFAULT 0,            -- total descubiertos por esta estrategia
    last_run_at      TIMESTAMP,                -- cuándo se ejecutó por última vez
    status           VARCHAR(20) DEFAULT 'IDLE', -- IDLE | RUNNING | PAUSED | COMPLETED
    updated_at       TIMESTAMP DEFAULT NOW()
);

INSERT INTO discovery_state (strategy) VALUES ('BY_CATEGORY'), ('BY_KEYWORD'), ('BY_TIME');
```

### 3.3 Modificar tabla existente: `product_details`

Agregar campo para marcar origen de descubrimiento:

```sql
ALTER TABLE product_details ADD COLUMN IF NOT EXISTS discovery_source VARCHAR(20);
-- Valores: MANUAL | CRAWL_CATEGORY | CRAWL_KEYWORD | CRAWL_TIME | LEGACY
```

---

## 4. Componentes a Implementar

### 4.1 Mapa de archivos (Hexagonal Architecture)

```
src/main/java/com/backandwhite/
├── domain/
│   ├── model/
│   │   └── DiscoveredPid.java                          [NUEVO]
│   ├── valueobject/
│   │   ├── DiscoveryStrategy.java                      [NUEVO] enum
│   │   └── DiscoveryStatus.java                        [NUEVO] enum
│   └── repository/
│       └── DiscoveredPidRepository.java                [NUEVO]
│
├── application/
│   ├── usecase/
│   │   ├── ProductDiscoveryUseCase.java                [NUEVO] interface
│   │   └── impl/
│   │       └── ProductDiscoveryUseCaseImpl.java        [NUEVO]
│   ├── strategy/
│   │   ├── DiscoveryStrategy.java                      [NUEVO] interface
│   │   ├── DiscoveryByCategoryStrategy.java            [NUEVO]
│   │   ├── DiscoveryByKeywordStrategy.java             [NUEVO]
│   │   └── DiscoveryByTimeStrategy.java                [NUEVO]
│   └── scheduler/
│       └── CjProductDiscoveryScheduler.java            [NUEVO]
│
├── infrastructure/
│   ├── client/cj/
│   │   ├── client/
│   │   │   └── CjDropshippingClient.java               [MODIFICAR] añadir getProductListFiltered()
│   │   └── dto/
│   │       └── CjProductListV2ItemDto.java             [NUEVO] DTO ligero del listado
│   ├── configuration/
│   │   └── CjDropshippingProperties.java               [MODIFICAR] añadir discovery config
│   └── db/postgres/
│       ├── entity/
│       │   ├── DiscoveredPidEntity.java                [NUEVO]
│       │   └── DiscoveryStateEntity.java               [NUEVO]
│       ├── repository/
│       │   ├── DiscoveredPidJpaRepository.java         [NUEVO]
│       │   └── DiscoveryStateJpaRepository.java        [NUEVO]
│       ├── mapper/
│       │   └── DiscoveredPidInfraMapper.java           [NUEVO]
│       └── repository/impl/
│           └── DiscoveredPidRepositoryImpl.java        [NUEVO]
│
├── api/
│   ├── controller/
│   │   └── CjDiscoveryController.java                 [NUEVO] admin endpoints
│   └── dto/out/
│       ├── DiscoveryStatusDtoOut.java                  [NUEVO]
│       └── DiscoveryStatsDtoOut.java                   [NUEVO]
│
└── resources/
    └── db/changelog/
        └── db.changelog-X.Y.sql                        [NUEVO] migration tables
```

**Total archivos:** ~20 nuevos + ~3 modificados

### 4.2 Detalle de componentes principales

#### 4.2.1 `DropshippingPort` (modificar)

Añadir método con filtros:

```java
// Nuevo método — listado con filtros para discovery
CjProductListPageDto getProductListFiltered(
    Integer page, Integer size,
    String categoryId, String keyword,
    Long timeStart, Long timeEnd,
    Integer orderBy, String sort
);
```

#### 4.2.2 `ProductDiscoveryUseCase`

```java
public interface ProductDiscoveryUseCase {
    /** Ejecutar discovery completo (3 estrategias en orden) */
    DiscoveryResult runFullDiscovery();
    
    /** Ejecutar solo una estrategia específica */
    DiscoveryResult runStrategy(DiscoveryStrategy strategy);
    
    /** Discovery incremental (solo BY_TIME — para cron diario) */
    DiscoveryResult runIncremental();
    
    /** Obtener estado actual de todas las estrategias */
    List<DiscoveryState> getStatus();
    
    /** Pausar/reanudar una estrategia */
    void pause(DiscoveryStrategy strategy);
    void resume(DiscoveryStrategy strategy);
}
```

#### 4.2.3 `DiscoveryByCategoryStrategy` (la más importante)

```java
@Component
@RequiredArgsConstructor
public class DiscoveryByCategoryStrategy implements DiscoveryStrategyExecutor {

    private final DropshippingPort dropshippingPort;
    private final DiscoveredPidRepository discoveredPidRepository;
    private final CategoryRepository categoryRepository; // existente
    private final DiscoveryStateRepository stateRepository;
    
    @Override
    public DiscoveryResult execute(DiscoveryState state) {
        // 1. Obtener TODAS las categorías L3 de BD local
        List<String> categoryIds = categoryRepository.findAllLevel3Ids();
        
        // 2. Buscar desde dónde quedó (reanudable)
        int startIdx = findCategoryIndex(categoryIds, state.getLastCategoryId());
        
        int totalNew = 0;
        for (int i = startIdx; i < categoryIds.size(); i++) {
            String catId = categoryIds.get(i);
            int newFromCat = crawlCategory(catId);
            totalNew += newFromCat;
            
            // Actualizar checkpoint
            state.setLastCategoryId(catId);
            state.setTotalDiscovered(state.getTotalDiscovered() + newFromCat);
            stateRepository.save(state);
            
            // Check si debemos pausar
            if (isPaused(state.getStrategy())) break;
        }
        
        return new DiscoveryResult(totalNew, /*...*/);
    }
    
    private int crawlCategory(String categoryId) {
        int page = 1, newPids = 0;
        while (page <= 1000) {
            var pageResult = dropshippingPort.getProductListFiltered(
                page, 100, categoryId, null, null, null, 3, "desc"
            );
            
            if (pageResult == null || pageResult.getContent() == null 
                || pageResult.getContent().isEmpty()) break;
            
            for (var item : extractProductItems(pageResult)) {
                if (!discoveredPidRepository.existsByPid(item.getId())) {
                    discoveredPidRepository.save(buildDiscoveredPid(item, categoryId));
                    newPids++;
                }
            }
            
            if (page >= pageResult.getTotalPages()) break;
            page++;
            rateLimitWait();  // 500ms
        }
        return newPids;
    }
}
```

#### 4.2.4 Schedulers

```java
// Discovery completo — semanal (domingo 2 AM)
@Scheduled(cron = "${app.cj.discovery.cron-full:0 0 2 * * SUN}")
@ConditionalOnProperty(name = "app.cj.discovery.enabled", havingValue = "true")
public void fullDiscovery() { ... }

// Discovery incremental — diario (4 AM)
@Scheduled(cron = "${app.cj.discovery.cron-incremental:0 0 4 * * *}")
@ConditionalOnProperty(name = "app.cj.discovery.enabled", havingValue = "true")
public void incrementalDiscovery() { ... }

// Enrich discovered PIDs — cada 6 horas
// Toma PIDs con status=NEW, llama /product/query, actualiza → SYNCED
@Scheduled(cron = "${app.cj.discovery.cron-enrich:0 0 */6 * * *}")
@ConditionalOnProperty(name = "app.cj.discovery.enabled", havingValue = "true")
public void enrichDiscoveredPids() { ... }
```

---

## 5. Cálculos de Throughput y Rate Limiting

### 5.1 Budget de requests

| Recurso | Valor |
|---------|-------|
| Rate limit (Plus) | 2 req/s |
| Budget por hora | 7,200 req |
| Budget diario (24h teoría) | 172,800 req |
| Budget realista (ventana nocturna 8h) | 57,600 req |

### 5.2 Estimación por estrategia

| Estrategia | Categorías/Keywords | Páginas/cada | Total req | Tiempo (2 req/s) |
|------------|---------------------|-------------|-----------|-------------------|
| **By Category** | ~250 cats L3 | ~10 pág/cat | ~2,500 | ~21 min |
| **By Keyword** | ~150 keywords | ~8 pág/kw | ~1,200 | ~10 min |
| **By Time** (diario) | 1 ventana | ~5-20 pág | ~20 | ~10 seg |
| **TOTAL Discovery** | — | — | **~3,720** | **~31 min** |

### 5.3 Estimación de enriquecimiento (detalle)

| Escenario | PIDs nuevos | Req (1 per PID) | Tiempo (2 req/s) |
|-----------|-------------|-----------------|-------------------|
| Primer barrido | ~50,000 | 50,000 | ~7h |
| Post-semanal | ~5,000 | 5,000 | ~42 min |
| Post-diario | ~300 | 300 | ~2.5 min |

> **Importante:** El enriquecimiento (detail fetch) es el cuello de botella, no el discovery. Priorizar PIDs por relevancia (listedNum, precio, categoría target).

### 5.4 Rate limiter — implementación

```yaml
# application.yaml
resilience4j:
  ratelimiter:
    instances:
      cjApi:
        limit-for-period: 2
        limit-refresh-period: 1s
        timeout-duration: 30s
```

En el crawler:

```java
private void rateLimitWait() {
    try { Thread.sleep(500); } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

> **Nota:** 500ms entre requests = 2 req/s. El rate limiter Resilience4j actúa como safety net.

---

## 6. Pipeline Completo: Discovery → Enrich → Publicar

```
┌─────────────────────────────────────────────────────────────────┐
│  FASE 1: DISCOVERY (semanal completo + diario incremental)      │
│                                                                  │
│  listV2 (filtros) → extraer PIDs → dedup → discovered_pids      │
│  Status: NEW                                                     │
│                                                                  │
│  Resultado: ~50,000-100,000 PIDs descubiertos                   │
│  Costo: ~3,700 requests (~31 min)                                │
└──────────────────────────┬──────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  FASE 2: PRIORIZACIÓN (automática)                               │
│                                                                  │
│  Ordenar PIDs por relevancia:                                    │
│    1. Precio en rango target ($5-$50 para DS)                    │
│    2. listedNum alto (popular)                                   │
│    3. Categorías prioritarias (configurable)                     │
│    4. Stock verificado (verifiedWarehouse=1)                     │
│                                                                  │
│  Status: NEW → QUEUED (los que pasan filtros)                    │
│  Status: NEW → SKIPPED (los que no pasan)                        │
└──────────────────────────┬──────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  FASE 3: ENRICHMENT (cada 6h, batch de PIDs QUEUED)              │
│                                                                  │
│  Para cada PID con status=QUEUED:                                │
│    GET /product/query?pid={pid}&features=enable_inventory        │
│    → Guardar en product_details + variants + inventories         │
│    → Insertar traducciones (EN)                                  │
│                                                                  │
│  Status: QUEUED → SYNCED (éxito)                                 │
│  Status: QUEUED → FAILED (error, errorCount++)                   │
│                                                                  │
│  Batch size: configurable (default 500 por ejecución)            │
│  Costo: 500 req → ~4 min por batch                              │
└──────────────────────────┬──────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  FASE 4: PUBLICACIÓN (ya existente)                              │
│                                                                  │
│  Admin aprueba productos → status = ACTIVE                       │
│  Frontend los muestra                                            │
│  Inventario sync cada 4h (ya implementado)                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. Configuración (application.yaml)

```yaml
app:
  cj:
    discovery:
      enabled: false  # activar manualmente
      cron-full: "0 0 2 * * SUN"          # Discovery completo: domingos 2 AM
      cron-incremental: "0 0 4 * * *"     # Discovery incremental: diario 4 AM
      cron-enrich: "0 0 */6 * * *"        # Enrich PIDs: cada 6h
      batch-size-enrich: 500               # PIDs por batch de enrichment
      max-pages-per-category: 200          # tope páginas por categoría
      max-pages-per-keyword: 100           # tope páginas por keyword
      page-size: 100                       # tamaño de página listV2
      rate-limit-wait-ms: 500             # pausa entre requests

      # Filtros de calidad mínima para discovery
      min-sell-price: 1.00                # descartar productos < $1
      max-sell-price: 500.00              # descartar > $500
      require-verified-inventory: false   # solo stock verificado?
      target-country-codes:               # países de interés
        - US
        - CN

      # Keywords para estrategia 2
      keywords:
        - hoodie
        - t-shirt
        - pants
        - dress
        - sneakers
        - phone case
        - laptop bag
        - watch
        - sunglasses
        - jewelry
        - earrings
        - necklace
        - kitchen gadget
        - bathroom
        - garden tool
        - toy
        - puzzle
        - wireless earbuds
        - led light
        - fitness
        - yoga mat
        - water bottle
        - backpack
        - wallet
        - beauty
        - skincare
        - makeup
        - hair
        - pet
        - dog
        - cat
        - car accessories
        - camping
        - fishing
        - cycling
        - running
        - baby
        - kids
        - home decor
        - candle
        - pillow
        - blanket
        - organizer
        - storage
        - tools
        - drill
        - pen
        - notebook
        - sticker
        - mask
        - gloves
        - hat
        - scarf
        - belt
        - socks
```

---

## 8. Endpoints Admin (REST)

| Método | Path | Descripción |
|--------|------|-------------|
| `POST` | `/api/v1/discovery/run/full` | Trigger manual discovery completo |
| `POST` | `/api/v1/discovery/run/incremental` | Trigger manual discovery incremental |
| `POST` | `/api/v1/discovery/run/{strategy}` | Trigger estrategia específica (BY_CATEGORY, BY_KEYWORD, BY_TIME) |
| `POST` | `/api/v1/discovery/pause/{strategy}` | Pausar estrategia en ejecución |
| `POST` | `/api/v1/discovery/resume/{strategy}` | Reanudar estrategia pausada |
| `GET`  | `/api/v1/discovery/status` | Estado de todas las estrategias |
| `GET`  | `/api/v1/discovery/stats` | Estadísticas: total descubiertos, por estrategia, por status |
| `POST` | `/api/v1/discovery/enrich` | Trigger manual enrichment batch |
| `GET`  | `/api/v1/discovery/pids?status=NEW&page=0&size=20` | Listar PIDs descubiertos con filtros |

---

## 9. Secuencia de Implementación (Checklist)

### Fase A — Infraestructura de datos (DB + Domain)

- [x] A1. Crear migración SQL: `discovered_pids` + `discovery_state` + ALTER `product_details`
- [x] A2. Registrar en `db.changelog-master.yaml`
- [x] A3. Crear `DiscoveryStrategy` enum (BY_CATEGORY, BY_KEYWORD, BY_TIME)
- [x] A4. Crear `DiscoveryStatus` enum (NEW, QUEUED, SYNCED, FAILED, SKIPPED)
- [x] A5. Crear `DiscoveredPid` domain model
- [x] A6. Crear `DiscoveredPidRepository` interface (domain)
- [x] A7. Crear `DiscoveredPidEntity` + `DiscoveryStateEntity` (JPA entities)
- [x] A8. Crear `DiscoveredPidJpaRepository` + `DiscoveryStateJpaRepository` (Spring Data)
- [x] A9. Crear `DiscoveredPidInfraMapper` (MapStruct)
- [x] A10. Crear `DiscoveredPidRepositoryImpl` (implementa interfaz domain)

### Fase B — Client extension + Port

- [x] B1. Modificar `DropshippingPort`: añadir `getProductListFiltered(...)`
- [x] B2. Modificar `CjDropshippingClient`: implementar `getProductListFiltered()` con query params opcionales
- [x] B3. Crear `CjProductListV2ItemDto` (DTO ligero: id, nameEn, sellPrice, categoryId, listedNum, warehouseInventoryNum, verifiedWarehouse, createAt)
- [x] B4. Ajustar `CjProductListPageDto` si necesita mapear la estructura `content[].productList[]`

### Fase C — Estrategias de discovery

- [x] C1. Crear interface `DiscoveryStrategyExecutor` (execute, getStrategy, supportsResume)
- [x] C2. Crear `DiscoveryByCategoryStrategy` (implementación completa con paginación, dedup, checkpoint)
- [x] C3. Crear `DiscoveryByKeywordStrategy` (implementación completa)
- [x] C4. Crear `DiscoveryByTimeStrategy` (implementación incremental con timeStart/timeEnd)

### Fase D — Use Case + Scheduler

- [x] D1. Crear `ProductDiscoveryUseCase` interface
- [x] D2. Crear `ProductDiscoveryUseCaseImpl` (orquesta estrategias, maneja estado, pausar/reanudar)
- [x] D3. Crear `CjProductDiscoveryScheduler` (3 crons: full, incremental, enrich)
- [x] D4. Modificar `CjDropshippingProperties`: añadir sección `discovery` con todos los campos

### Fase E — API layer

- [x] E1. Crear `DiscoveryStatusDtoOut` + `DiscoveryStatsDtoOut` (DTOs de respuesta)
- [x] E2. Crear `CjDiscoveryController` (admin endpoints)

### Fase F — Configuración + Build

- [x] F1. Actualizar `application.yaml` con sección `app.cj.discovery`
- [x] F2. Compilar: `mvn compile` ✅ BUILD SUCCESS
- [ ] F3. Test manual: ejecutar discovery by category para 3 categorías → verificar `discovered_pids`

---

## 10. Proyección de Resultados

### Primera ejecución (Domingo)

| Métrica | Estimación |
|---------|-----------|
| Categorías L3 barridas | ~250 |
| Keywords barridos | ~50 |
| **PIDs totales descubiertos** | **60,000 - 120,000** |
| PIDs nuevos (no en BD) | ~55,000 - 115,000 |
| Tiempo discovery | ~35 min |
| Enrichment (500/batch, 6h) | ~4 días para todo |

### Estado estable (post primera semana)

| Métrica | Estimación |
|---------|-----------|
| Productos en BD | **80,000 - 100,000+** |
| Nuevos diarios (BY_TIME) | ~200-500 |
| Re-discovery semanal (netos nuevos) | ~2,000-5,000 |
| API requests/día (discovery + sync + inventory) | ~5,000-10,000 |

### Comparativa antes/después

| Métrica | ANTES | DESPUÉS |
|---------|-------|---------|
| Productos descubiertos | ~6,000 | ~100,000+ |
| Estrategias de extracción | 1 (paginación ciega) | 3 (categoría + keyword + tiempo) |
| Descubrimiento de nuevos | Manual (alguien ejecuta sync) | Automático diario |
| Reanudabilidad ante fallo | ❌ reinicia desde 0 | ✅ checkpoint por categoría/keyword |
| Priorización | ❌ todos iguales | ✅ por precio, popularidad, stock |

---

## 11. Riesgos y Mitigaciones

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Rate limit 429 | Crawler se bloquea | Resilience4j retry + backoff + 500ms pause |
| Elasticsearch window (10k) | No se ven más de 10k por query | Ya mitigado: dividimos por categoría (~2k/cat) |
| Categorías vacías | Requests desperdiciados | Skip si totalRecords=0 en primera página |
| Duplicados masivos entre estrategias | Disco desperdiciado | `UNIQUE(pid)` en discovered_pids + check antes de INSERT |
| Datos de listV2 incompletos | PIDs sin detalle útil | Por eso existe Fase 3 (enrichment con /product/query) |
| Discovery demora mucho (>1h) | Compite con otros sync | Ventana nocturna (2-4 AM), schedulers separados |
| CJ cambia API | Crawler falla | Circuit breaker + fallback a /product/list (v1) |
| Daily call limit (unverified) | 1000/día bloqueante | Asegurar que la cuenta está verificada (Plus ya debería estar) |

---

## 12. Decisiones de Diseño

| Decisión | Justificación |
|----------|---------------|
| Separar discovery de enrichment | Discovery es barato (1 req = 100 PIDs). Enrichment es caro (1 req = 1 PID). Permite priorizar qué enriquecer primero. |
| Tabla `discovered_pids` separada de `product_details` | No contaminar la tabla de productos reales con PIDs que aún no tienen detalle. Permite ver progreso de discovery independientemente. |
| 3 estrategias sueltas (Strategy pattern) | Cada una tiene su cadencia, su estado de reanudación, y sus parámetros. Se pueden activar/desactivar independientemente. |
| Checkpoint por categoría/keyword | Si el crawler falla en la categoría #120 de 250, al reanudar salta a #120 en vez de empezar de 0. |
| Keywords configurables en YAML | Se pueden añadir/quitar keywords sin cambiar código ni redesplegar (solo restart). En fase 2 podrían moverse a BD. |
| `orderBy=3&sort=desc` (createTime desc) | Productosos más nuevos primero — más relevante para discovery incremental. |
| Enrichment batch de 500 | A 2 req/s ≈ 4 min por batch. Suficientemente rápido sin consumir todo el rate limit. |
