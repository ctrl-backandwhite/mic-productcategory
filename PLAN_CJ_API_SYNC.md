# Plan de Integración — Sincronización CJ Dropshipping API

> **Microservicio:** mic-productcategory  
> **API:** CJ Dropshipping API v2.0 (`https://developers.cjdropshipping.com/api2.0/v1`)  
> **Plan de tarifa:** Plus (~2 req/s efectivos)  
> **Autenticación:** Header `CJ-Access-Token` (gestionado por `CjTokenManager`)

---

## 1. Estado Actual de la Integración

### 1.1 Componentes existentes

| Componente | Ubicación | Estado |
|---|---|---|
| `CjDropshippingClient` | `infrastructure/client/cj/` | ✅ Implementado (getCategory, query, listV2) |
| `DropshippingPort` | `domain/port/output/` | ✅ Puerto de salida definido |
| `CjTokenManager` | `infrastructure/client/cj/` | ✅ Gestión del ciclo de vida del token |
| `ProductSyncUseCase` | `application/usecase/` | ✅ Orquestación de sync batch |
| `ProductDetailUseCase` | `application/usecase/` | ✅ Sync individual de productos |
| Resilience4j (retry) | `application.yaml` | ✅ 3 intentos, 2s espera |

### 1.2 Endpoints CJ ya consumidos

| Endpoint | Método | Uso actual |
|---|---|---|
| `/product/getCategory` | GET | Sincroniza categorías 3 niveles |
| `/product/query?pid={pid}` | GET | Detalle de producto con variantes |
| `/product/listV2` | GET | Listado paginado de productos |

### 1.3 Tablas existentes que ya mapean datos CJ

| Tabla | Campos CJ mapeados |
|---|---|
| `product_details` | pid, product_name_en, product_sku, sell_price, big_image, category_id, etc. |
| `product_detail_variants` | vid, pid, variant_sku, variant_sell_price, dimensiones, peso |
| `product_detail_variant_inventories` | vid, country_code, total_inventory, cj_inventory, factory_inventory |
| `reviews` | product_id, rating, title, body, author_name, images |

### 1.4 Tablas de traducción existentes (i18n)

El sistema maneja traducciones mediante tablas separadas con PK compuesta `(entity_id, locale)`.
Idiomas soportados: **`en`** (English), **`es`** (Español), **`pt-BR`** (Português).
Definidos en `LanguageContext.tsx` → `Locale = "es" | "en" | "pt"`.

| Tabla | PK | Columnas traducibles | Migración |
|---|---|---|---|
| `category_translations` | `(category_id, locale)` | `name` | `db.changelog-1.0.sql` |
| `product_translations` | `(product_id, locale)` | `name` | `db.changelog-1.4.sql` |
| `product_detail_translations` | `(pid, locale)` | `product_name`, `entry_name`, `material_name`, `packing_name`, `product_key`, `product_pro` | `db.changelog-1.7.sql` |
| `product_detail_variant_translations` | `(vid, locale)` | `variant_name` | `db.changelog-1.7.sql` |

**Mapper existente:** `CjProductDetailMapper.java` ya inserta traducciones `locale="en"` vía:
- `buildTranslations(cj)` → `product_detail_translations`
- `buildVariantTranslations(v)` → `product_detail_variant_translations`
- `buildProductTranslations(cj)` → `product_translations`

> **Nota i18n:** CJ API solo provee campos en inglés (`productNameEn`, `entryNameEn`, etc.).
> No existe endpoint CJ para español ni portugués. Ver sección 5.5 para la estrategia completa.

---

## 2. Endpoints CJ a Integrar

### 2.1 Endpoints CJ seleccionados (solo última versión, sin redundancias)

| # | Endpoint | Método | Prioridad | Estado | Uso previsto |
|---|---|---|---|---|---|
| 1 | `/product/getCategory` | GET | — | ✅ Ya integrado | Categorías (3 niveles) |
| 2 | `/product/listV2` | GET | — | ✅ Ya integrado | Listado productos (Elasticsearch, última versión) |
| 3 | `/product/query` | GET | — | ✅ Ya integrado | Detalle producto + variantes + inventario (con `features=enable_inventory`) |
| 4 | `/product/stock/getInventoryByPid` | GET | **P1** | 🔴 Pendiente | Inventario por producto (todas las variantes en 1 llamada) |
| 5 | `/product/productComments` | GET | **P2** | 🔴 Pendiente | Reviews de producto (última versión, reemplaza `/product/comments`) |
| 6 | `/product/globalWarehouseList` | GET | **P3** | 🔴 Pendiente | Catálogo de warehouses (referencia estática) |
| 7 | `/product/addToMyProduct` | POST | **P3** | 🔴 Pendiente | Agregar a "Mis Productos" |
| 8 | `/product/myProduct/query` | GET | **P3** | 🔴 Pendiente | Listar "Mis Productos" |
| 9 | `/product/sourcing/create` | POST | **P4** | 🔴 Pendiente | Crear solicitud de sourcing |
| 10 | `/product/sourcing/query` | POST | **P4** | 🔴 Pendiente | Consultar estado sourcing |

### 2.2 Endpoints descartados por redundancia

| Endpoint descartado | Razón de eliminación |
|---|---|
| `/product/list` (v1) | **Reemplazado por `/product/listV2`** — v2 usa Elasticsearch, más filtros, más campos (description, category, inventory, video vía `features`). v1 tiene límite de 1000 req/día para free/v1. |
| `/product/variant/query` | **Redundante con `/product/query`** — `/product/query` ya devuelve TODAS las variantes del producto en su respuesta. |
| `/product/variant/queryByVid` | **Redundante con `/product/query`** — si tenemos el PID (siempre lo tenemos en BD), `/product/query?pid=X&features=enable_inventory` devuelve variantes con inventario. Para inventario individual usar `getInventoryByPid` y filtrar. |
| `/product/stock/queryByVid` | **Redundante con `/product/stock/getInventoryByPid`** — `getInventoryByPid` devuelve inventario de TODAS las variantes en 1 llamada. Siempre tenemos PID→VID en BD local. |
| `/product/stock/queryBySku` | **Redundante** — mismo dato que `getInventoryByPid` pero usando SKU como clave. Ya almacenamos PIDs y VIDs. |
| `/product/comments` (v1) | **DEPRECADO desde junio 2024.** Reemplazado por `/product/productComments`. |

---

## 3. Estrategia de Rate Limiting

### 3.1 Restricciones

| Nivel | Límite |
|---|---|
| Plan Plus (cuenta) | ~2 req/s |
| IP (oficial) | ~10 req/s |
| General (oficial) | ~30 req/s |

### 3.2 Implementación — Token Bucket con Resilience4j

```yaml
# application.yaml
resilience4j:
  ratelimiter:
    instances:
      cjApi:
        limit-for-period: 2
        limit-refresh-period: 1s
        timeout-duration: 30s
  retry:
    instances:
      cjApi:
        max-attempts: 3
        wait-duration: 2s
        retry-exceptions:
          - java.net.SocketTimeoutException
          - org.springframework.web.client.HttpServerErrorException
```

### 3.3 Clase: `CjRateLimitedClient` (decorador)

```
CjDropshippingClient (actual)
       ↓ decorado por
CjRateLimitedClient
       ↓ usa
Resilience4j RateLimiter + Retry + CircuitBreaker
```

**Estrategia ante 429 Too Many Requests:**
1. Resilience4j intercepta → espera `Retry-After` del header (o 5s default)
2. 3 reintentos con backoff exponencial (2s, 4s, 8s)
3. Si falla después de 3 intentos → registra en `sync_failures` y continúa con siguiente item
4. CircuitBreaker: se abre tras 5 fallos consecutivos → espera 60s antes de reintentar

---

## 4. Estrategia de Sincronización

### 4.1 Tipos de sincronización

| Tipo | Frecuencia | Trigger | Datos |
|---|---|---|---|
| **Full Sync** | 1x/día (madrugada) | Scheduled (`@Scheduled`) | Productos + variantes + inventario |
| **Inventory Sync** | Cada 4h | Scheduled | Solo inventario (es lo que más cambia) |
| **Review Sync** | 1x/día | Scheduled | Reviews de productos publicados |
| **On-Demand Sync** | Manual (admin) | REST endpoint | Producto individual o batch |
| **Category Sync** | 1x/semana | Scheduled | Categorías CJ (raramente cambian) |

### 4.2 Sync Incremental y Flag `force`

Todas las tablas sincronizadas tienen un campo `last_synced_at` (TIMESTAMP).
Cuando un registro se actualiza exitosamente, se graba `last_synced_at = NOW()`.

**Comportamiento por defecto (incremental):**
- El job solo procesa registros donde `last_synced_at < DATE_TRUNC('day', NOW())` (no sincronizados hoy).
- Si el proceso falla a mitad de ejecución y se relanza, los registros ya actualizados se omiten.
- Esto permite **reanudación segura** sin repetir llamadas a la API de CJ.

**Comportamiento con `force=true`:**
- Ignora `last_synced_at` y re-sincroniza TODOS los registros.
- Útil cuando se sospecha de datos corruptos o se cambió la lógica de mapeo.

```java
// Ejemplo de uso en InventorySyncUseCase
public SyncResult syncAll(boolean force) {
    List<String> pids = force
        ? productRepo.findAllActivePids()
        : productRepo.findPidsNotSyncedToday(SyncScope.INVENTORY);
    // ...
}

// Query para obtener PIDs pendientes
// SELECT pd.pid FROM product_details pd
// WHERE pd.status = '3'
//   AND (pd.inventory_synced_at IS NULL
//        OR pd.inventory_synced_at < DATE_TRUNC('day', NOW()))
```

**Campos `last_synced_at` por tabla:**

| Tabla | Campo | Qué marca |
|---|---|---|
| `product_details` | `product_synced_at` | Full Sync (producto + variantes + inventario) |
| `product_details` | `inventory_synced_at` | Inventory Sync (solo inventario) |
| `product_details` | `reviews_synced_at` | Review Sync |

> Se usa `product_details` como tabla de control porque es la entidad raíz.
> No se añaden columnas en `variants` ni `inventories` — el PID es la unidad de sync.

### 4.3 Flujo de Full Sync (productos + variantes + inventario)

```
┌─────────────────────────────────────────────────────────────┐
│  FULL SYNC PIPELINE (ProductFullSyncJob)                    │
│  Param: force (boolean, default false)                      │
│                                                              │
│  1. Obtener PIDs a procesar:                                │
│     force=false → SELECT pid FROM product_details           │
│       WHERE status != 'DELETED'                             │
│         AND (product_synced_at IS NULL                      │
│              OR product_synced_at < DATE_TRUNC('day', NOW()))│
│     force=true  → SELECT pid FROM product_details           │
│       WHERE status != 'DELETED'                             │
│                                                              │
│  2. Para cada PID (en lotes de 50):                         │
│     ┌───────────────────────────────────────┐               │
│     │ GET /product/query?pid={pid}          │  1 req        │
│     │   &features=enable_inventory          │               │
│     │    → Actualizar product_details       │               │
│     │    → Actualizar product_detail_variants│               │
│     │    → Actualizar inventories (variant) │               │
│     │    (inventarios vienen inline en cada  │               │
│     │     variante con enable_inventory)     │               │
│     │    → SET product_synced_at = NOW()     │               │
│     │    → SET inventory_synced_at = NOW()   │               │
│     └───────────────────────────────────────┘               │
│     = 1 request por producto                                │
│     Con rate limit 2 req/s → 2 productos/segundo            │
│                                                              │
│  3. Registrar resultado en sync_log                         │
│  4. Emitir evento Kafka: product.sync.completed             │
│                                                              │
│  NOTA: Si el job falla en el PID #200 de 500, al relanzar  │
│  (force=false) solo procesará los 300 PIDs restantes.       │
└─────────────────────────────────────────────────────────────┘
```

### 4.4 Cálculo de throughput

| Escenario | Productos | Requests | Tiempo estimado (2 req/s) |
|---|---|---|---|
| 100 productos | 100 req | ~50s |
| 500 productos | 500 req | ~4.2 min |
| 1,000 productos | 1,000 req | ~8.3 min |
| 5,000 productos | 5,000 req | ~41.7 min (~0.7h) |

> **Optimización 1:** Gracias a `features=enable_inventory` en `/product/query`, el Full Sync
> requiere solo 1 request por producto (antes eran 2). Esto reduce tiempos a la mitad.
>
> **Optimización 2:** Con sync incremental, si el job falla al 60% y se relanza,
> solo procesa el 40% restante. Ejemplo: 5,000 productos, falla en #3,000 →
> relanzar solo hace 2,000 req (~16.7 min) en vez de repetir las 5,000.

### 4.5 Flujo de Inventory Sync (cada 4h)

```
┌─────────────────────────────────────────────────────────────┐
│  INVENTORY SYNC (InventorySyncJob)                          │
│  Param: force (boolean, default false)                      │
│                                                              │
│  1. Obtener PIDs a procesar:                                │
│     force=false → SELECT pid FROM product_details           │
│       WHERE status = '3'                                    │
│         AND (inventory_synced_at IS NULL                    │
│              OR inventory_synced_at < NOW() - INTERVAL '4h')│
│     force=true  → SELECT pid FROM product_details           │
│       WHERE status = '3'                                    │
│                                                              │
│  2. Para cada PID:                                          │
│     GET /product/stock/getInventoryByPid?pid={pid}          │
│     → Actualizar product_detail_variant_inventories         │
│     → Detectar cambios de stock significativos              │
│     → SET inventory_synced_at = NOW()                       │
│                                                              │
│  3. Si inventario baja a 0:                                 │
│     → Log warning                                           │
│     → Emitir evento Kafka: product.inventory.depleted       │
│                                                              │
│  4. Actualizar campo warehouse_inventory_num en products    │
│     (sum total de inventarios de todas las variantes)       │
│                                                              │
│  NOTA: Inventory sync usa intervalo de 4h (no día completo) │
│  porque se ejecuta varias veces al día. Si falla y se       │
│  relanza, solo retoma los PIDs no actualizados en las       │
│  últimas 4 horas.                                           │
└─────────────────────────────────────────────────────────────┘
```

### 4.6 Flujo de Review Sync (1x/día)

```
┌─────────────────────────────────────────────────────────────┐
│  REVIEW SYNC (ReviewSyncJob)                                │
│  Param: force (boolean, default false)                      │
│                                                              │
│  1. Obtener PIDs a procesar:                                │
│     force=false → SELECT pid FROM product_details           │
│       WHERE status = '3'                                    │
│         AND (reviews_synced_at IS NULL                      │
│              OR reviews_synced_at < DATE_TRUNC('day', NOW()))│
│     force=true  → SELECT pid FROM product_details           │
│       WHERE status = '3'                                    │
│                                                              │
│  2. Para cada PID:                                          │
│     GET /product/productComments?pid={pid}&pageNum=1&pageSize=20│
│     → Solo primera página (reviews más recientes)           │
│     → SET reviews_synced_at = NOW()                         │
│                                                              │
│  3. Mapear campos CJ → reviews:                            │
│     commentId → id (external_review_id)                     │
│     comment → body                                          │
│     score → rating                                          │
│     commentUser → author_name                               │
│     commentUrls → images (JSONB)                            │
│     countryCode → country_code (nuevo campo)                │
│     commentDate → created_at                                │
│                                                              │
│  4. Deduplicar por commentId (evitar duplicados)            │
│     INSERT ... ON CONFLICT (external_review_id) DO NOTHING  │
│                                                              │
│  5. Auto-moderar: status = 'APPROVED' (CJ ya las filtra)   │
│                                                              │
│  NOTA: Si falla en PID #80 de 200, al relanzar solo        │
│  procesará los 120 PIDs cuyo reviews_synced_at < hoy.       │
└─────────────────────────────────────────────────────────────┘
```

### 4.6 Sync On-Demand (bajo demanda)

Sigue el mismo patrón ya implementado para productos y variantes con `POST /products/sync`,
`POST /products/sync/page` y `GET /products/detail/{pid}`.

#### 4.6.1 Endpoints on-demand existentes (referencia)

| Endpoint existente | Método | Qué hace |
|---|---|---|
| `POST /api/v1/products/sync` | POST | Full sync: itera todos los PIDs locales, fetch paralelo (5 threads) con `bulkSyncProducts` |
| `POST /api/v1/products/sync/page?page=1&size=100` | POST | Sync paginado: el frontend itera `page++` hasta `hasMore=false` |
| `GET /api/v1/products/detail/{pid}` | GET | On-demand individual: si no existe en BD local → `getOrFetchFromCj(pid)` → persiste y devuelve |
| `POST /api/v1/categories/sync` | POST | Sync categorías CJ (3 niveles) con `upsertCategory` |

#### 4.6.2 Nuevos endpoints on-demand para inventario

```
POST /api/v1/sync/inventory/product/{pid}
```
Sync inventario de un producto específico y todas sus variantes.

```
┌─────────────────────────────────────────────────────────────┐
│  ON-DEMAND INVENTORY SYNC (single product)                  │
│                                                              │
│  1. Recibir pid por path variable                           │
│  2. GET /product/stock/getInventoryByPid?pid={pid}          │
│     → CJ devuelve inventories[] + variantInventories[]      │
│                                                              │
│  3. UPSERT product_detail_variant_inventories               │
│     (INSERT ... ON CONFLICT (vid, country_code) DO UPDATE)  │
│                                                              │
│  4. Actualizar warehouse_inventory_num en products          │
│     (SELECT SUM(total_inventory) WHERE pid via variants)    │
│                                                              │
│  5. Devolver InventorySyncResultDtoOut:                     │
│     { pid, variantsUpdated, warehousesUpdated, timestamp }  │
└─────────────────────────────────────────────────────────────┘
```

> **Nota:** Se eliminó `POST /sync/inventory/variant/{vid}` porque el endpoint CJ
> `/product/stock/queryByVid` fue descartado por redundancia (ver sección 2.2).
> Para inventario de una variante específica, usar `POST /sync/inventory/product/{pid}`
> que trae TODAS las variantes del producto con `getInventoryByPid`.

```
POST /api/v1/sync/inventory/page?page=1&size=50
```
Sync inventario paginado (mismo patrón que `products/sync/page`).
El frontend itera `page++` hasta `hasMore=false`.

```
┌─────────────────────────────────────────────────────────────┐
│  ON-DEMAND INVENTORY SYNC (paginado)                        │
│                                                              │
│  1. Leer PIDs de product_details (page, size)               │
│                                                              │
│  2. Para cada PID en la página (paralelo, 5 threads):       │
│     GET /product/stock/getInventoryByPid?pid={pid}          │
│     → UPSERT inventories de todas las variantes             │
│                                                              │
│  3. Devolver InventoryPageSyncResultDtoOut:                 │
│     { page, updated, failed, total, hasMore }               │
│                                                              │
│  Frontend loop:                                              │
│     while (hasMore) { page++; POST /sync/inventory/page }   │
└─────────────────────────────────────────────────────────────┘
```

#### 4.6.3 Nuevos endpoints on-demand para reviews

```
POST /api/v1/sync/reviews/product/{pid}
```
Sync reviews de un producto específico.

```
┌─────────────────────────────────────────────────────────────┐
│  ON-DEMAND REVIEW SYNC (single product)                     │
│                                                              │
│  1. Recibir pid y optional pageNum, pageSize por query param│
│  2. GET /product/productComments?pid={pid}&pageNum=1        │
│     &pageSize=20                                            │
│                                                              │
│  3. Mapear CJ comment → Review domain:                     │
│     - commentId → external_review_id                        │
│     - comment → body                                        │
│     - score → rating                                        │
│     - commentUser → author_name                             │
│     - commentUrls → images (JSONB)                          │
│     - source = 'CJ_DROPSHIPPING'                            │
│     - status = 'APPROVED'                                   │
│     - verified = true                                       │
│                                                              │
│  4. INSERT ... ON CONFLICT (external_review_id) DO NOTHING  │
│     (deduplicación — reviews ya importadas se ignoran)      │
│                                                              │
│  5. Devolver ReviewSyncResultDtoOut:                        │
│     { pid, imported, skipped, total, page, hasMore }        │
└─────────────────────────────────────────────────────────────┘
```

```
POST /api/v1/sync/reviews/page?page=1&size=20
```
Sync reviews paginado — itera productos publicados y trae reviews de cada uno.

```
┌─────────────────────────────────────────────────────────────┐
│  ON-DEMAND REVIEW SYNC (paginado por productos)             │
│                                                              │
│  1. Leer PIDs de product_details WHERE status='3'           │
│     (page, size)                                            │
│                                                              │
│  2. Para cada PID (secuencial, 2 req/s):                    │
│     GET /product/productComments?pid={pid}&pageNum=1        │
│     &pageSize=20                                            │
│     → INSERT ... ON CONFLICT DO NOTHING                     │
│                                                              │
│  3. Devolver ReviewPageSyncResultDtoOut:                    │
│     { page, productsProcessed, reviewsImported, hasMore }   │
│                                                              │
│  Frontend loop:                                              │
│     while (hasMore) { page++; POST /sync/reviews/page }     │
└─────────────────────────────────────────────────────────────┘
```

#### 4.6.4 On-demand full refresh de un producto completo

```
POST /api/v1/sync/product/{pid}/full
```
Refresca producto + variantes + inventario + reviews en una sola llamada.

```
┌─────────────────────────────────────────────────────────────┐
│  ON-DEMAND FULL PRODUCT REFRESH                             │
│                                                              │
│  1. GET /product/query?pid={pid}&features=enable_inventory  │
│     → Actualizar product_details                            │
│     → Actualizar product_detail_variants                    │
│     → Actualizar product_detail_variant_inventories         │
│                                                              │
│  2. GET /product/productComments?pid={pid}&pageNum=1        │
│     → Importar reviews más recientes                        │
│                                                              │
│  = 2 requests totales por producto                          │
│                                                              │
│  3. Devolver FullProductSyncResultDtoOut:                   │
│     { pid, productUpdated, variantsUpdated,                 │
│       inventoriesUpdated, reviewsImported, timestamp }      │
└─────────────────────────────────────────────────────────────┘
```

#### 4.6.5 DTOs de respuesta on-demand

```java
@Builder
public record InventorySyncResultDtoOut(
    String entityId,        // pid o vid
    int variantsUpdated,
    int warehousesUpdated,
    Instant timestamp
) {}

@Builder
public record InventoryPageSyncResultDtoOut(
    int page,
    int updated,
    int failed,
    int total,
    boolean hasMore
) {}

@Builder
public record ReviewSyncResultDtoOut(
    String pid,
    int imported,
    int skipped,
    int total,
    int page,
    boolean hasMore
) {}

@Builder
public record ReviewPageSyncResultDtoOut(
    int page,
    int productsProcessed,
    int reviewsImported,
    boolean hasMore
) {}

@Builder
public record FullProductSyncResultDtoOut(
    String pid,
    boolean productUpdated,
    int variantsUpdated,
    int inventoriesUpdated,
    int reviewsImported,
    Instant timestamp
) {}
```

#### 4.6.6 Resumen completo de endpoints de sync

| Endpoint | Método | Tipo | Uso |
|---|---|---|---|
| `POST /products/sync` | POST | On-demand | Full sync productos (ya existe) |
| `POST /products/sync/page` | POST | On-demand | Sync paginado productos (ya existe) |
| `GET /products/detail/{pid}` | GET | On-demand | Fetch individual producto (ya existe) |
| `POST /categories/sync` | POST | On-demand | Sync categorías (ya existe) |
| `POST /sync/inventory/product/{pid}` | POST | **Nuevo** | Inventario de 1 producto |
| `POST /sync/inventory/page` | POST | **Nuevo** | Inventario paginado |
| `POST /sync/reviews/product/{pid}` | POST | **Nuevo** | Reviews de 1 producto |
| `POST /sync/reviews/page` | POST | **Nuevo** | Reviews paginado |
| `POST /sync/product/{pid}/full` | POST | **Nuevo** | Refresh completo (prod+var+inv+rev) |
| `GET /sync/status` | GET | **Nuevo** | Estado último sync por tipo |
| `GET /sync/log` | GET | **Nuevo** | Historial de syncs |
| `GET /sync/failures` | GET | **Nuevo** | Fallos pendientes |
| `POST /sync/failures/{id}/retry` | POST | **Nuevo** | Reintentar fallo |

---

## 5. Mapeo de Campos — CJ API ↔ Modelo de Dominio

### 5.1 Product Details

| Campo CJ API | Campo DB (`product_details`) | Transformación |
|---|---|---|
| `pid` | `pid` (PK) | Directo |
| `productNameEn` | `product_name_en` | Directo |
| `productSku` | `product_sku` | Directo |
| `bigImage` | `big_image` | Directo (URL) |
| `productImageSet` | `product_image_set` | JSON array → TEXT |
| `productWeight` | `product_weight` | String con unidad (g) |
| `productUnit` | `product_unit` | Directo |
| `productType` | `product_type` | ORDINARY_PRODUCT, SERVICE_PRODUCT, etc. |
| `categoryId` | `category_id` (FK) | UUID CJ → mapear a categoría local |
| `categoryName` | `category_name` | Directo |
| `entryCode` | `entry_code` | HS code (aduanas) |
| `entryNameEn` | `entry_name_en` | Directo |
| `materialNameEn` | `material_name_en` | Directo |
| `materialKey` | `material_key` | JSON array |
| `packingWeight` | `packing_weight` | String (g) |
| `packingNameEn` | `packing_name_en` | Directo |
| `packingKey` | `packing_key` | JSON array |
| `productKeyEn` | `product_key_en` | Directo |
| `productProEn` | `product_pro_en` | JSON array (logistics attributes) |
| `sellPrice` | `sell_price` | NUMERIC(12,2) — precio proveedor USD |
| `suggestSellPrice` | `suggest_sell_price` | Rango: "0.97-4.08" |
| `description` | `description` | HTML/TEXT largo |
| `listedNum` | `listed_num` | Integer |
| `status` | `status` | "3" = On Sale |
| `supplierName` | `supplier_name` | Directo |
| `supplierId` | `supplier_id` | Directo |
| `createrTime` | `creater_time` | ISO 8601 → Instant |
| `productVideo` | — | **Nuevo campo necesario** si se activa `enable_video` |

### 5.2 Variants

| Campo CJ API | Campo DB (`product_detail_variants`) | Transformación |
|---|---|---|
| `vid` | `vid` (PK) | UUID directo |
| `pid` | `pid` (FK) | UUID directo |
| `variantNameEn` | `variant_name_en` | Directo |
| `variantSku` | `variant_sku` | Directo |
| `variantImage` | `variant_image` | URL |
| `variantKey` | `variant_key` | "Color-Size" (ej: "Black-XXL") |
| `variantLength` | `variant_length` | mm → almacenar tal cual |
| `variantWidth` | `variant_width` | mm |
| `variantHeight` | `variant_height` | mm |
| `variantVolume` | `variant_volume` | mm³ |
| `variantWeight` | `variant_weight` | g → NUMERIC(10,2) |
| `variantSellPrice` | `variant_sell_price` | USD → NUMERIC(10,2) |
| `variantSugSellPrice` | `variant_sug_sell_price` | USD → NUMERIC(10,2) |
| `variantStandard` | `variant_standard` | "long=110,width=110,height=30" |
| `createTime` | `create_time` | ISO 8601 → Instant |

### 5.3 Inventories

| Campo CJ API | Campo DB (`product_detail_variant_inventories`) | Transformación |
|---|---|---|
| `countryCode` | `country_code` | ISO 3166-1 alpha-2 ("US", "CN", "ES") |
| `totalInventory` | `total_inventory` | Integer directo |
| `cjInventory` | `cj_inventory` | Integer directo |
| `factoryInventory` | `factory_inventory` | Integer directo |
| `verifiedWarehouse` | `verified_warehouse` | 1=verified, 2=unverified |

### 5.4 Traducciones (i18n)

CJ API solo provee datos en inglés. El mapper `CjProductDetailMapper` genera filas de traducción
`locale="en"` automáticamente durante el sync. Las traducciones a `es` y `pt-BR` se gestionan
manualmente por el admin desde el panel.

#### 5.4.1 `product_detail_translations` (generada por sync)

| Campo CJ API | Campo traducción (`locale="en"`) | Mapper method |
|---|---|---|
| `productNameEn` | `product_name` | `buildTranslations()` |
| `entryNameEn` | `entry_name` | `buildTranslations()` |
| `materialNameEn` | `material_name` | `buildTranslations()` |
| `packingNameEn` | `packing_name` | `buildTranslations()` |
| `productKeyEn` | `product_key` | `buildTranslations()` |
| `productProEn` | `product_pro` | `buildTranslations()` |

#### 5.4.2 `product_detail_variant_translations` (generada por sync)

| Campo CJ API | Campo traducción (`locale="en"`) | Mapper method |
|---|---|---|
| `variantNameEn` | `variant_name` | `buildVariantTranslations()` |

#### 5.4.3 `product_translations` (generada por sync)

| Campo CJ API | Campo traducción (`locale="en"`) | Mapper method |
|---|---|---|
| `productNameEn` | `name` | `buildProductTranslations()` |

#### 5.4.4 Estrategia i18n para `es` y `pt-BR`

| Aspecto | Decisión |
|---|---|
| **Fuente CJ** | Solo inglés (`*En`). CJ no provee español ni portugués |
| **Sync automático** | Inserta únicamente `locale="en"`. NO crea filas `es`/`pt-BR` |
| **Traducciones es/pt-BR** | El admin traduce manualmente desde el panel de administración |
| **Fallback frontend** | Si no existe traducción para el locale solicitado, la API retorna vacío para ese campo. El front muestra el producto con los campos disponibles |
| **Query backend** | `?locale=en` (default). El `CategorySpecification` / `ProductSpecification` filtra por `translations.locale = :locale` |
| **Flujo admin** | 1) Sync importa producto (solo `en`) → 2) Admin ve en listado "sin traducción es/pt" → 3) Admin edita y agrega traducciones manualmente |
| **Topic Kafka futuro** | `config.language.activated` ya definido en `AppConstants.java` (sin consumer aún) — permitirá notificar cuando se active un nuevo idioma |

> **Importante:** El sync de CJ **nunca sobreescribe** traducciones `es`/`pt-BR` existentes.
> Solo actualiza la fila `locale="en"` cuando `force=true` o el producto no ha sido sincronizado hoy.

### 5.5 Reviews (nuevo mapeo)

| Campo CJ API | Campo DB (`reviews`) | Transformación |
|---|---|---|
| `commentId` | `external_review_id` (nuevo) | Long → String |
| `pid` | `product_id` | Mapear CJ pid → product.id local |
| `comment` | `body` | Directo |
| `score` | `rating` | String → SMALLINT (1-5) |
| `commentUser` | `author_name` | Directo (ya anonimizado por CJ: "F***o") |
| `commentUrls` | `images` | String[] → JSONB |
| `countryCode` | — | **Nuevo campo necesario** en tabla `reviews` |
| `commentDate` | `created_at` | ISO 8601 → Timestamp |
| — | `user_id` | NULL (reviews importadas de CJ) |
| — | `verified` | `true` (CJ verifica compras) |
| — | `status` | 'APPROVED' |
| — | `source` | **Nuevo campo**: 'CJ_DROPSHIPPING' vs 'USER' |

---

## 6. Cambios en Base de Datos

### 6.1 Migraciones Liquibase necesarias

#### `db.changelog-X.XX.sql` — Reviews: campos para sync CJ

```sql
-- Agregar campo para deduplicación de reviews importadas
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS external_review_id VARCHAR(64);
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS source VARCHAR(30) DEFAULT 'USER';
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS country_code VARCHAR(5);

CREATE UNIQUE INDEX IF NOT EXISTS idx_reviews_external_id
    ON reviews(external_review_id) WHERE external_review_id IS NOT NULL;

-- CHECK constraint para source
ALTER TABLE reviews ADD CONSTRAINT chk_review_source
    CHECK (source IN ('USER', 'CJ_DROPSHIPPING'));
```

#### `db.changelog-X.XX.sql` — Sync Log table + columnas de tracking incremental

```sql
-- Columnas de tracking de sync incremental en product_details
-- Permiten saber cuándo fue la última vez que se sincronizó cada producto
-- y omitir los ya actualizados si el job se relanza.
ALTER TABLE product_details ADD COLUMN IF NOT EXISTS product_synced_at TIMESTAMP;
ALTER TABLE product_details ADD COLUMN IF NOT EXISTS inventory_synced_at TIMESTAMP;
ALTER TABLE product_details ADD COLUMN IF NOT EXISTS reviews_synced_at TIMESTAMP;

-- Índices parciales para queries de sync incremental (solo filas pendientes)
CREATE INDEX IF NOT EXISTS idx_pd_product_sync_pending
    ON product_details(pid)
    WHERE product_synced_at IS NULL OR product_synced_at < CURRENT_DATE;

CREATE INDEX IF NOT EXISTS idx_pd_inventory_sync_pending
    ON product_details(pid)
    WHERE inventory_synced_at IS NULL OR inventory_synced_at < (NOW() - INTERVAL '4 hours');

CREATE INDEX IF NOT EXISTS idx_pd_reviews_sync_pending
    ON product_details(pid)
    WHERE reviews_synced_at IS NULL OR reviews_synced_at < CURRENT_DATE;

-- Tabla de log de ejecución
CREATE TABLE IF NOT EXISTS sync_log (
    id          VARCHAR(64) PRIMARY KEY,
    sync_type   VARCHAR(30) NOT NULL,  -- FULL, INVENTORY, REVIEW, CATEGORY
    status      VARCHAR(20) NOT NULL,  -- RUNNING, COMPLETED, FAILED, PARTIAL
    started_at  TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    total_items INT DEFAULT 0,
    synced_items INT DEFAULT 0,
    failed_items INT DEFAULT 0,
    error_details TEXT,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_sync_log_type_status ON sync_log(sync_type, status);

CREATE TABLE IF NOT EXISTS sync_failure (
    id          BIGSERIAL PRIMARY KEY,
    sync_log_id VARCHAR(64) REFERENCES sync_log(id),
    entity_type VARCHAR(30) NOT NULL,  -- PRODUCT, VARIANT, INVENTORY, REVIEW
    entity_id   VARCHAR(64) NOT NULL,
    error_code  VARCHAR(20),
    error_message TEXT,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    next_retry_at TIMESTAMP,
    resolved    BOOLEAN DEFAULT false,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_sync_failure_unresolved
    ON sync_failure(resolved, next_retry_at) WHERE resolved = false;
```

---

## 7. Nuevos Componentes a Crear

### 7.1 Estructura de paquetes (hexagonal)

```
src/main/java/com/.../micproductcategory/
├── domain/
│   ├── model/
│   │   ├── SyncLog.java                          (nuevo)
│   │   └── SyncFailure.java                      (nuevo)
│   └── port/
│       └── output/
│           └── DropshippingPort.java              (extender)
│               + fetchProductDetailWithInventory(pid)
│               + fetchInventoryByPid(pid)
│               + fetchReviews(pid, page, size)
│               + fetchWarehouseList()
│               + addToMyProduct(productId)
│               + fetchMyProducts(keyword, page, size)
│
├── application/
│   ├── usecase/
│   │   ├── sync/
│   │   │   ├── ProductFullSyncUseCase.java        (nuevo)
│   │   │   ├── InventorySyncUseCase.java          (nuevo — scheduled + on-demand)
│   │   │   │   + syncAll(force)                    scheduled cada 4h
│   │   │   │   + syncByPid(pid, force)              on-demand 1 producto
│   │   │   │   + syncPage(page, size, force)        on-demand paginado
│   │   │   ├── ReviewSyncUseCase.java             (nuevo — scheduled + on-demand)
│   │   │   │   + syncAll(force)                    scheduled 1x/día
│   │   │   │   + syncByPid(pid, page, size, force)  on-demand 1 producto
│   │   │   │   + syncPage(page, size, force)        on-demand paginado
│   │   │   ├── FullProductRefreshUseCase.java     (nuevo — on-demand)
│   │   │   │   + refresh(pid)                      prod+var+inv+reviews (siempre force)
│   │   │   └── CategorySyncUseCase.java           (refactor del existente)
│   │   └── ...
│   └── service/
│       └── SyncLogService.java                    (nuevo)
│
├── api/
│   └── dto/
│       └── out/
│           ├── InventorySyncResultDtoOut.java      (nuevo)
│           ├── InventoryPageSyncResultDtoOut.java  (nuevo)
│           ├── ReviewSyncResultDtoOut.java         (nuevo)
│           ├── ReviewPageSyncResultDtoOut.java     (nuevo)
│           └── FullProductSyncResultDtoOut.java    (nuevo)
│
├── infrastructure/
│   ├── client/
│   │   └── cj/
│   │       ├── client/
│   │       │   └── CjDropshippingClient.java      (extender)
│   │       ├── dto/
│   │       │   ├── CjInventoryByPidResponse.java  (nuevo)
│   │       │   ├── CjReviewResponse.java          (nuevo)
│   │       │   └── CjWarehouseResponse.java       (nuevo)
│   │       └── mapper/
│   │           ├── CjReviewMapper.java            (nuevo)
│   │           └── CjInventoryMapper.java         (nuevo)
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── SyncLogEntity.java                 (nuevo)
│   │   │   └── SyncFailureEntity.java             (nuevo)
│   │   └── repository/
│   │       ├── SyncLogRepository.java             (nuevo)
│   │       └── SyncFailureRepository.java         (nuevo)
│   └── scheduler/
│       ├── ProductFullSyncScheduler.java          (nuevo)
│       ├── InventorySyncScheduler.java            (nuevo)
│       └── ReviewSyncScheduler.java               (nuevo)
│
└── adapter/
    └── external/
        └── rest/
            └── SyncController.java                (nuevo — scheduled triggers + on-demand)
```

### 7.2 DropshippingPort — Métodos nuevos

```java
public interface DropshippingPort {

    // --- YA EXISTENTES ---
    List<CjCategory> fetchCategories();
    CjProductDetail fetchProductDetail(String pid);
    CjProductListPage fetchProductList(int page, int size);

    // --- NUEVOS ---
    CjProductDetail fetchProductDetailWithInventory(String pid);
    CjProductInventory fetchInventoryByPid(String pid);
    CjReviewPage fetchReviews(String pid, int page, int size, Integer score);
    List<CjWarehouse> fetchGlobalWarehouses();
    boolean addToMyProduct(String productId);
    CjMyProductPage fetchMyProducts(String keyword, int page, int size);
}
```

> **Nota:** Se eliminaron `fetchVariantsByPid`, `fetchVariantByVid`, `fetchInventoryByVid`,
> `fetchInventoryBySku` porque son redundantes (ver sección 2.2).
> `fetchProductDetailWithInventory` usa `/product/query?features=enable_inventory`
> y devuelve producto + variantes + inventario en 1 sola llamada.

### 7.3 SyncController — Endpoints de administración

**Todos los endpoints de sync aceptan `?force=true` (default `false`).**
Cuando `force=false`, se omiten los registros ya sincronizados en el período actual.

```
# --- Sync Scheduled (trigger manual) ---
POST   /api/v1/sync/full?force=false              → Lanza Full Sync (incremental por defecto)
POST   /api/v1/sync/inventory?force=false         → Lanza Inventory Sync (incremental)
POST   /api/v1/sync/reviews?force=false            → Lanza Review Sync (incremental)

# --- On-Demand: Inventario ---
POST   /api/v1/sync/inventory/product/{pid}?force=false  → Inventario de 1 producto
POST   /api/v1/sync/inventory/page?page=1&size=50&force=false → Inventario paginado

# --- On-Demand: Reviews ---
POST   /api/v1/sync/reviews/product/{pid}?force=false    → Reviews de 1 producto
POST   /api/v1/sync/reviews/page?page=1&size=20&force=false → Reviews paginado

# --- On-Demand: Full refresh ---
POST   /api/v1/sync/product/{pid}/full?force=false       → Producto completo (siempre force=true implícito)

# --- Monitoring ---
GET    /api/v1/sync/status             → Estado del último sync por tipo + % completado
GET    /api/v1/sync/log                → Historial de syncs (paginado)
GET    /api/v1/sync/failures           → Fallos pendientes de resolver
POST   /api/v1/sync/failures/{id}/retry → Reintentar un fallo específico
```

**Ejemplo de uso del flag `force`:**
```bash
# Sync incremental (solo pendientes de hoy) — comportamiento normal
curl -X POST http://localhost:6002/api/v1/sync/full

# Sync forzado (re-sincroniza TODO aunque ya se haya hecho hoy)
curl -X POST http://localhost:6002/api/v1/sync/full?force=true

# Inventario de un producto (force=true para refresh inmediato)
curl -X POST http://localhost:6002/api/v1/sync/inventory/product/{pid}?force=true
```

---

## 8. Configuración

### 8.1 application.yaml — Nuevas propiedades

```yaml
sync:
  cj:
    enabled: true
    full-sync:
      cron: "0 0 3 * * *"       # 3:00 AM diario
      batch-size: 50             # productos por lote
    inventory-sync:
      cron: "0 0 */4 * * *"     # cada 4 horas
    review-sync:
      cron: "0 0 5 * * *"       # 5:00 AM diario
      max-pages-per-product: 1   # solo primera página
      page-size: 20
    category-sync:
      cron: "0 0 2 * * 1"       # Lunes 2:00 AM
    retry:
      max-attempts: 3
      initial-delay-ms: 2000
      multiplier: 2.0
    rate-limit:
      requests-per-second: 2
```

### 8.2 application-local.yaml — Override para desarrollo

```yaml
sync:
  cj:
    enabled: false                # desactivado en local por defecto
    full-sync:
      cron: "-"                   # desactivado
    inventory-sync:
      cron: "-"                   # desactivado
    review-sync:
      cron: "-"                   # desactivado
```

---

## 9. Eventos Kafka (integración con saga/notification)

| Evento | Topic | Productor | Consumidor |
|---|---|---|---|
| `product.sync.completed` | `product.sync.completed` | ProductFullSyncUseCase | mic-cmsservice (actualizar campañas) |
| `product.inventory.updated` | `product.inventory.updated` | InventorySyncUseCase | mic-orderservice (validar stock) |
| `product.inventory.depleted` | `product.inventory.depleted` | InventorySyncUseCase | mic-notificationservice (alerta admin) |
| `product.review.imported` | `product.review.imported` | ReviewSyncUseCase | mic-cmsservice (widget reviews) |

---

## 10. Manejo de Errores

### 10.1 Errores CJ API conocidos → mapeo a la estructura del back

El backend usa `ExternalServiceException(code, message)` capturada por
`ProductExceptionHandler` (`@Order(1)`).
El frontend consume errores como `ApiErrorBody { code, message, details, timeStamp }`
(ver `src/app/types/api.ts` y `src/app/lib/apiHelpers.ts → handleRes`).

**Prefijo de código reservado: `ES` (External Service)**

| Código CJ | Msg CJ | Código interno | Excepción Java | HTTP | `ApiErrorBody.message` (al frontend) |
|---|---|---|---|---|---|
| `1600100` | Param error | **ES001** | `ExternalServiceException("ES001", "CJ API param error: " + msg)` | 502 | `"CJ API param error: {detalle}"` |
| `1600000` | Business error | **ES002** | `ExternalServiceException("ES002", "CJ API business error: " + msg)` | 502 | `"CJ API business error: Product already added"` |
| HTTP `429` | Rate limit exceeded | **ES003** | `ExternalServiceException("ES003", "CJ API rate limit exceeded")` | **429** | `"CJ API rate limit exceeded"` |
| HTTP `401` | Token expired | — | Manejado internamente por `CjTokenManager.refreshToken()` | — | No llega al frontend; retry transparente |
| HTTP `500` | Server error | **ES004** | `ExternalServiceException("ES004", "CJ API server error")` | 502 | `"CJ API server error"` |
| Timeout | Connection/Read timeout | **ES005** | `ExternalServiceException("ES005", "CJ API timeout after " + ms + "ms")` | 502 | `"CJ API timeout after 10000ms"` |

> **Referencia existente:** `ProductExceptionHandler.java` ya mapea `ES003` → HTTP 429,
> el resto → HTTP 502. Archivo: `api/exception/ProductExceptionHandler.java`.

**Flujo completo error→frontend:**
```
CJ API responde error
  → CjDropshippingClient lanza ExternalServiceException(code, msg)
  → ProductExceptionHandler atrapa (@Order 1)
  → Construye ApiResponseDtoOut { code, message, details, timestamp }
  → HTTP 502 (o 429 para ES003)
  → Frontend: handleRes() parsea body → ApiErrorBody { code, message, details, timeStamp }
  → UI muestra: sonner toast con message
```

**Registro en sync_failure:**

| Código | Reintentable | `resolved` | `sync_failure.error_code` |
|---|---|---|---|
| ES001 (param) | ❌ No | `true` | `ES001` |
| ES002 (business) | ❌ No | `true` | `ES002` |
| ES003 (rate limit) | ✅ Sí (auto-retry con backoff) | `false` → retry | `ES003` |
| ES004 (server) | ✅ Sí (max 3) | `false` → retry | `ES004` |
| ES005 (timeout) | ✅ Sí (max 3, backoff) | `false` → retry | `ES005` |

> **⚠️ Inconsistencia conocida frontend↔backend:**
> El backend (`ApiResponseDtoOut`) serializa el campo como `timestamp` (todo minúscula),
> pero el tipo frontend (`ApiErrorBody` en `src/app/types/api.ts:23`) lo espera como `timeStamp`
> (camelCase). En la práctica `handleRes()` solo lee `.message`, así que no impacta hoy.
> Se recomienda alinear en un futuro: o cambiar el frontend a `timestamp` o añadir
> `@JsonProperty("timeStamp")` en el backend.

### 10.2 Estrategia de retry para fallos

```
sync_failure:
  ├── retry_count < max_retries
  │     → next_retry_at = NOW() + (initial_delay × 2^retry_count)
  │     → FailureRetryScheduler lo reintenta automáticamente
  └── retry_count >= max_retries
        → resolved = false (queda para revisión manual)
        → Emitir alerta Kafka → notificación al admin
```

---

## 11. Fases de Implementación

### Fase 1 — Inventario en tiempo real (P1) — ~3-4 días

**Objetivo:** Mantener el inventario actualizado para evitar vender productos sin stock.

**Tareas:**
1. Extender `DropshippingPort` con `fetchInventoryByPid(pid)`
2. Crear `CjInventoryByPidResponse` DTO
3. Implementar en `CjDropshippingClient`
4. Crear `InventorySyncUseCase` con:
   - `syncAll(force)` — para el scheduler (cada 4h)
   - `syncByPid(pid, force)` — on-demand 1 producto
   - `syncPage(page, size, force)` — on-demand paginado (frontend itera)
5. Crear `InventorySyncScheduler` (cron cada 4h)
6. Crear DTOs de respuesta: `InventorySyncResultDtoOut`, `InventoryPageSyncResultDtoOut`
7. Crear tablas `sync_log` y `sync_failure` (migración Liquibase)
8. Evento Kafka `product.inventory.updated` + `product.inventory.depleted`
9. Endpoints on-demand en `SyncController`:
   - `POST /sync/inventory/product/{pid}?force=false`
   - `POST /sync/inventory/page?page=1&size=50&force=false`
10. Tests unitarios + test de integración

### Fase 2 — Full Sync mejorado (P1) — ~3-4 días

**Objetivo:** Actualizar productos y variantes diariamente con datos frescos de CJ.

**Tareas:**
1. Crear `ProductFullSyncUseCase` (orquesta producto + variantes + inventario con `fetchProductDetailWithInventory`)
2. Crear DTOs para full sync result
3. Crear `ProductFullSyncScheduler` (cron 3:00 AM)
4. Implementar Rate Limiter con Resilience4j (`limit-for-period: 2`)
5. Crear `FullProductRefreshUseCase.refresh(pid)` — on-demand full refresh
6. Crear `FullProductSyncResultDtoOut`
7. Endpoint on-demand en `SyncController`:
   - `POST /sync/product/{pid}/full?force=false`
8. Crear `SyncLogService` para registro de auditoría
9. Endpoints de monitoring en `SyncController`:
    - `GET /sync/status`, `GET /sync/log`, `GET /sync/failures`, `POST /sync/failures/{id}/retry`
10. Tests unitarios + test de integración

> **⚠️ i18n:** El Full Sync solo actualiza la fila `locale="en"` en `*_translations`.
> Las traducciones manuales `es`/`pt-BR` creadas por el admin **nunca se sobreescriben**.
> `CjProductDetailMapper.buildTranslations()` genera únicamente `locale="en"`.
> El admin puede filtrar productos sin traducción para completar `es` y `pt-BR` manualmente.

### Fase 3 — Reviews Sync (P2) — ~2-3 días

**Objetivo:** Importar reviews de CJ para mostrar social proof en la tienda.

**Tareas:**
1. Migración Liquibase: `external_review_id`, `source`, `country_code` en `reviews`
2. Extender `DropshippingPort` con `fetchReviews(pid, page, size, score)`
3. Crear `CjReviewResponse` DTO + `CjReviewMapper`
4. Crear `ReviewSyncUseCase` con:
   - `syncAll(force)` — para el scheduler (1x/día)
   - `syncByPid(pid, page, size, force)` — on-demand 1 producto
   - `syncPage(page, size, force)` — on-demand paginado (frontend itera)
5. Crear DTOs de respuesta: `ReviewSyncResultDtoOut`, `ReviewPageSyncResultDtoOut`
6. Crear `ReviewSyncScheduler` (cron 5:00 AM)
7. Endpoints on-demand en `SyncController`:
   - `POST /sync/reviews/product/{pid}?force=false`
   - `POST /sync/reviews/page?page=1&size=20&force=false`
8. Evento Kafka `product.review.imported`
9. Tests unitarios

### Fase 4 — Endpoints complementarios (P3) — ~2 días

**Objetivo:** Funcionalidades adicionales para administración.

**Tareas:**
1. `fetchGlobalWarehouses()` — Catálogo de warehouses
2. `addToMyProduct(productId)` — Agregar producto desde admin
3. `fetchMyProducts()` — Listar productos propios en CJ
4. Endpoint REST para consulta manual de stock desde admin panel

### Fase 5 — Sourcing (P4) — ~1-2 días

**Objetivo:** Permitir solicitar sourcing de nuevos productos desde la admin.

**Tareas:**
1. `createSourcing()` — Crear solicitud
2. `querySourcing()` — Consultar estado
3. UI en panel admin (`front/Ecomerce/src/app/pages/admin/` — React 18)

---

## 12. Tests

### 12.1 Tests unitarios

| Test | Qué valida |
|---|---|
| `InventorySyncUseCaseTest` | Lógica de sync, detección de stock 0, evento Kafka emitido |
| `ProductFullSyncUseCaseTest` | Orquestación completa, manejo de errores parciales |
| `ReviewSyncUseCaseTest` | Deduplicación, mapeo de campos, auto-moderación |
| `CjDropshippingClientTest` | Correcta serialización/deserialización de DTOs CJ |
| `CjReviewMapperTest` | Mapeo CJ review → dominio Review |
| `SyncLogServiceTest` | Registro correcto de log y fallos |

### 12.2 Tests de integración

| Test | Qué valida |
|---|---|
| `InventorySyncIntegrationTest` | Sync end-to-end con WireMock (mock CJ API) |
| `ReviewSyncIntegrationTest` | Sync con persistencia real (Testcontainers PostgreSQL) |
| `RateLimiterIntegrationTest` | Que no excede 2 req/s bajo carga |

### 12.3 Test E2E (Cypress — Ecomerce React 18)

La app frontend está en `front/Ecomerce/` (React 18, Vite 6, Tailwind 4, DaisyUI 5).
El admin panel existente vive en `src/app/pages/admin/` con rutas bajo `/admin`.
El sync de productos ya existe en `NexaProductAdminRepository.syncProducts()` que
itera `POST /products/sync/page?page={n}&size=100`. Los nuevos tests de sync
deben seguir el mismo patrón.  Los errores de API se consumen vía `ApiErrorBody`:
`{ code, message, details, timeStamp }` (ver `src/app/types/api.ts`).

| Test | Qué valida |
|---|---|
| `sync-dashboard.cy.ts` | Admin puede ver estado de syncs, trigger manual, ver fallos |
| `product-inventory.cy.ts` | Inventario se muestra actualizado tras sync |

---

## 13. Consideraciones de Seguridad

| Aspecto | Medida |
|---|---|
| API Key CJ | Almacenar en variable de entorno `CJ_API_KEY`, nunca en código |
| Token refresh | `CjTokenManager` renueva automáticamente antes de expiración |
| Sync endpoints | Proteger con `@PreAuthorize("hasRole('ADMIN')")` |
| Rate limiting | Doble protección: Resilience4j (client-side) + respeto 429 (server-side) |
| Datos sensibles | No almacenar supplier credentials; solo IDs públicos de CJ |
| SQL Injection | Usar JPA/Hibernate parameterized queries (ya existente) |
| Sync log | Auditoría completa de cada operación de sync |

---

## 14. Diagrama de Arquitectura Final

```
                    ┌──────────────────────┐
                    │   Admin Panel        │
                    │   (Ecomerce — React  │
                    │    18 + Vite + TW4)  │
                    └──────────┬───────────┘
                               │ REST
                    ┌──────────▼───────────┐
                    │  SyncController      │
                    │  POST /sync/full     │
                    │  POST /sync/inventory│
                    │  GET  /sync/status   │
                    └──────────┬───────────┘
                               │
         ┌─────────────────────┤──────────────────────┐
         │                     │                      │
┌────────▼────────┐ ┌─────────▼──────────┐ ┌─────────▼──────────┐
│ ProductFullSync │ │ InventorySync      │ │ ReviewSync         │
│ UseCase         │ │ UseCase            │ │ UseCase            │
│                 │ │                    │ │                    │
│ @Scheduled 3AM  │ │ @Scheduled /4h     │ │ @Scheduled 5AM     │
└────────┬────────┘ └─────────┬──────────┘ └─────────┬──────────┘
         │                    │                      │
         └────────────────────┼──────────────────────┘
                              │ DropshippingPort
                    ┌─────────▼──────────┐
                    │ CjDropshippingClient│
                    │ + RateLimiter (2/s) │
                    │ + Retry (3x)       │
                    │ + CircuitBreaker   │
                    └─────────┬──────────┘
                              │ HTTPS
                    ┌─────────▼──────────┐
                    │ CJ Dropshipping    │
                    │ API v2.0           │
                    └────────────────────┘
         
         Side effects:
         ┌──────────────────┐    ┌────────────────────┐
         │ PostgreSQL       │    │ Kafka Topics        │
         │ sync_log         │    │ product.sync.*      │
         │ sync_failure     │    │ product.inventory.* │
         │ reviews (updated)│    │ product.review.*    │
         └──────────────────┘    └────────────────────┘
```

---

## 15. Checklist de Implementación

- [ ] **Fase 1:** Migración Liquibase (sync_log, sync_failure, columnas `*_synced_at`)
- [ ] **Fase 1:** `DropshippingPort.fetchInventoryByPid()`
- [ ] **Fase 1:** `CjInventoryByPidResponse` DTO
- [ ] **Fase 1:** `InventorySyncUseCase` (syncAll(force) + syncByPid(pid, force) + syncPage(page, size, force))
- [ ] **Fase 1:** `InventorySyncScheduler` (cron cada 4h)
- [ ] **Fase 1:** `InventorySyncResultDtoOut` + `InventoryPageSyncResultDtoOut`
- [ ] **Fase 1:** Endpoints on-demand inventario en `SyncController` (con `?force=false` default)
- [ ] **Fase 1:** Eventos Kafka inventario
- [ ] **Fase 1:** Tests unitarios inventario
- [ ] **Fase 2:** Resilience4j RateLimiter config
- [ ] **Fase 2:** `ProductFullSyncUseCase` + `ProductFullSyncScheduler` (usa `fetchProductDetailWithInventory`)
- [ ] **Fase 2:** `FullProductRefreshUseCase.refresh(pid)` (on-demand full)
- [ ] **Fase 2:** `FullProductSyncResultDtoOut`
- [ ] **Fase 2:** Endpoint `POST /sync/product/{pid}/full`
- [ ] **Fase 2:** `SyncLogService` + endpoints monitoring
- [ ] **Fase 2:** Tests unitarios + integración
- [ ] **Fase 3:** Migración reviews (external_review_id, source, country_code)
- [ ] **Fase 3:** `DropshippingPort.fetchReviews()`
- [ ] **Fase 3:** `CjReviewResponse` + `CjReviewMapper`
- [ ] **Fase 3:** `ReviewSyncUseCase` (syncAll(force) + syncByPid(pid, page, size, force) + syncPage(page, size, force))
- [ ] **Fase 3:** `ReviewSyncResultDtoOut` + `ReviewPageSyncResultDtoOut`
- [ ] **Fase 3:** `ReviewSyncScheduler` (cron 5:00 AM)
- [ ] **Fase 3:** Endpoints on-demand reviews en `SyncController` (con `?force=false` default)
- [ ] **Fase 3:** Tests unitarios reviews
- [ ] **Fase 4:** Warehouses, MyProduct
- [ ] **Fase 5:** Sourcing (create + query)

### i18n (transversal a todas las fases)

- [ ] Verificar que `CjProductDetailMapper.buildTranslations()` solo inserta `locale="en"` (ya implementado)
- [ ] Verificar que el mapper **no sobreescribe** filas `es`/`pt-BR` existentes durante re-sync
- [ ] Los 3 métodos de traducción del mapper (`buildTranslations`, `buildVariantTranslations`, `buildProductTranslations`) generan exclusivamente `en`
- [ ] Query backend con `?locale=en` (default) retorna solo la traducción solicitada
- [ ] Productos sincronizados sin `es`/`pt-BR` son visibles en admin para traducción manual
- [ ] Topic Kafka `config.language.activated` preparado para futuro broadcast de nuevos idiomas (ya definido en `AppConstants.java`)
