# Análisis del Flujo de Descubrimiento y Sincronización de Productos CJ

> Fecha: 12 de abril de 2026  
> Estado actual: **539 categorías L3** en BD, **2.382 productos** importados, **70 categorías** con productos

---

## 1. Arquitectura General

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│  FRONTEND (React)                                                               │
│                                                                                 │
│  AdminProducts.tsx                                                              │
│    └── handleDiscover() / runDiscover()                                         │
│          └── NexaProductAdminRepository.discoverByCategory(onProgress, offset)  │
│                │                                                                │
│                │  while (hasMore) {                                              │
│                │    POST /api/v1/products/sync/discover/page?offset=N            │
│                │    offset++                                                    │
│                │    sleep(2000ms)                                                │
│                │  }                                                              │
│                │                                                                │
│                ▼                                                                │
├─────────────────────────────────────────────────────────────────────────────────┤
│  BACKEND (Spring Boot — mic-productcategory :6002)                              │
│                                                                                 │
│  ProductController                                                              │
│    └── POST /sync/discover/page?offset=N                                        │
│          └── ProductSyncUseCaseImpl.discoverNewProductsByCategory(offset)        │
│                │                                                                │
│                │  1. Obtiene TODAS las categorías L3 de BD                       │
│                │  2. Ordena → selecciona la categoría en posición [offset]       │
│                │  3. for (cjPage = 1..MAX_CJ_PAGES_PER_CATEGORY) {              │
│                │       GET /product/listV2?page=cjPage&size=100&categoryId=X    │
│                │       ├── Si productList vacío → break                          │
│                │       ├── Filtra solo PIDs nuevos (no existen en BD)            │
│                │       ├── Llama detail API por cada PID nuevo                   │
│                │       └── Si cjPage * 100 >= totalRecords → break              │
│                │     }                                                           │
│                │  4. Retorna { created, updated, hasMore, totalCategories }      │
│                │                                                                │
│                ▼                                                                │
│  CjDropshippingClient                                                           │
│    └── GET https://developers.cjdropshipping.com/api2.0/v1/product/listV2       │
│          ?page=N &size=100 &categoryId=UUID                                     │
│          Header: CJ-Access-Token: xxx                                           │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. ¿Cómo funciona actualmente?

### 2.1 Frontend → Backend: Una categoría por llamada

El **frontend** controla qué categoría procesar mediante el parámetro `offset`:

```
POST /sync/discover/page?offset=0   →  Procesa categoría #1
POST /sync/discover/page?offset=1   →  Procesa categoría #2
POST /sync/discover/page?offset=2   →  Procesa categoría #3
...
POST /sync/discover/page?offset=538 →  Procesa categoría #539 (última)
```

El bucle del frontend (`discoverByCategory`) incrementa `offset` hasta que el backend responde con `hasMore: false`.

### 2.2 Backend: Paginación interna por categoría

Para **cada categoría**, el backend itera las páginas del API de CJ:

```
Categoría "76C86F19-..." (offset=0):
  → CJ API: GET listV2?page=1&size=100&categoryId=76C86F19-...  →  43 productos
  → Como 1 × 100 >= 43 → BREAK (solo 1 página necesaria)

Categoría con 250 productos (offset=5):
  → CJ API: page=1 → 100 productos → importa los nuevos
  → CJ API: page=2 → 100 productos → importa los nuevos
  → CJ API: page=3 → 50 productos  → importa los nuevos
  → Como 3 × 100 >= 250 → BREAK (3 páginas)
```

### 2.3 Condiciones de parada por categoría

El backend deja de pedir páginas cuando ocurre **cualquiera** de estas:

| # | Condición | Código | Ubicación |
|---|-----------|--------|-----------|
| 1 | `productList` vacío | `if (products.isEmpty()) break;` | Línea 248 |
| 2 | Se alcanzó el total | `if (cjPage * PAGE_SIZE >= totalRecords) break;` | Línea 273 |
| 3 | Se alcanzó el máximo de páginas | `cjPage <= MAX_CJ_PAGES_PER_CATEGORY` (3) | Línea 239 |

---

## 3. Constantes críticas actuales

| Constante | Valor | Archivo | Línea | Impacto |
|-----------|-------|---------|-------|---------|
| `PAGE_SIZE` | **100** | ProductSyncUseCaseImpl.java | 32 | Productos por página CJ |
| `MAX_CJ_PAGES_PER_CATEGORY` | **3** | ProductSyncUseCaseImpl.java | 211 | **Máx 300 productos/categoría** |
| `DETAIL_CALL_DELAY_MS` | **600ms** | ProductSyncUseCaseImpl.java | 296 | Delay entre llamadas detail |
| `RATE_LIMIT_BACKOFF_MS` | **3.000ms** | ProductSyncUseCaseImpl.java | 298 | Backoff por rate limit |
| `MAX_RATE_LIMIT_RETRIES` | **2** | ProductSyncUseCaseImpl.java | 300 | Reintentos por rate limit |
| `DELAY_MS` (frontend) | **2.000ms** | NexaProductAdminRepository.ts | 600 | Delay entre categorías |

---

## 4. ¿Se están iterando TODAS las páginas hasta vacío?

### Respuesta: **NO completamente**

Hay un **límite duro de 3 páginas** por categoría (`MAX_CJ_PAGES_PER_CATEGORY = 3`).

Esto significa:

```
Con PAGE_SIZE = 100 y MAX_CJ_PAGES = 3:

Categoría con    43 productos → ✅ Se importan todos (1 página)
Categoría con   200 productos → ✅ Se importan todos (2 páginas)
Categoría con   300 productos → ✅ Se importan todos (3 páginas)
Categoría con   301 productos → ⚠️  Se pierden 1 producto
Categoría con 1.000 productos → ❌ Se pierden 700 productos (solo se importan 300)
Categoría con 5.000 productos → ❌ Se pierden 4.700 productos
```

### El proveedor permite hasta 1.000 páginas

Según la documentación de CJ, el número máximo de página es **1.000** (100.000 productos por categoría). El límite actual de 3 páginas es **muy conservador**.

---

## 5. Estructura de respuesta CJ vs DTOs

### Respuesta real de CJ (`listV2`):

```json
{
    "code": 200,
    "result": true,
    "message": "Success",
    "data": {
        "pageSize": 100,        ← CjProductListPageDto.pageSize
        "pageNumber": 1,        ← CjProductListPageDto.pageNumber
        "totalRecords": 43,     ← CjProductListPageDto.totalRecords ✅ USADO
        "totalPages": 1,        ← CjProductListPageDto.totalPages (no se usa)
        "content": [            ← CjProductListPageDto.content
            {
                "productList": [    ← CjProductListV2ContentDto.productList
                    {
                        "id": "2503140848341614700",       ← CjProductListV2ItemDto.id ✅
                        "nameEn": "Men's Swimsuit...",      ← CjProductListV2ItemDto.nameEn ✅
                        "sku": "CJYD2327699",               ← CjProductListV2ItemDto.sku ✅
                        "sellPrice": "3.86",                ← CjProductListV2ItemDto.sellPrice ✅
                        "categoryId": "76C86F19-...",       ← CjProductListV2ItemDto.categoryId ✅
                        "warehouseInventoryNum": 61188,     ← CjProductListV2ItemDto.warehouseInventoryNum ✅
                        ...
                    }
                ],
                "keyWord": "",
                "keyWordOld": ""
            }
        ]
    }
}
```

### Mapeo DTO → Usado:

| Campo CJ | DTO Java | ¿Se usa? | ¿Para qué? |
|----------|----------|----------|-------------|
| `data.totalRecords` | `CjProductListPageDto.totalRecords` | ✅ | Calcular si hay más páginas |
| `data.totalPages` | `CjProductListPageDto.totalPages` | ❌ | Mapeado pero no usado en lógica |
| `data.pageNumber` | `CjProductListPageDto.pageNumber` | ❌ | Solo logging |
| `data.pageSize` | `CjProductListPageDto.pageSize` | ❌ | Solo mapeado |
| `data.content[].productList[]` | `CjProductListV2ContentDto.productList` | ✅ | Lista de productos |
| `productList[].id` | `CjProductListV2ItemDto.id` | ✅ | PID para buscar detail |
| `productList[].nameEn` | `.nameEn` | ✅ | Nombre del producto |
| `productList[].sellPrice` | `.sellPrice` | ✅ | Precio referencia |
| `productList[].categoryId` | `.categoryId` | ✅ | Categoría CJ |
| `productList[].sku` | `.sku` | ✅ | SKU del producto |
| `productList[].warehouseInventoryNum` | `.warehouseInventoryNum` | ✅ | Stock almacén |

### Método de aplanamiento:

```java
// CjProductListPageDto.getAllProducts()
// Recorre content[] → para cada entry recoge productList[] → lista plana
public List<CjProductListV2ItemDto> getAllProducts() {
    List<CjProductListV2ItemDto> all = new ArrayList<>();
    for (CjProductListV2ContentDto entry : content) {
        if (entry.getProductList() != null) {
            all.addAll(entry.getProductList());
        }
    }
    return all;
}
```

---

## 6. Flujo secuencial completo para 1 categoría

```
Frontend: POST /sync/discover/page?offset=0
  │
  ▼
Backend: discoverNewProductsByCategory(0)
  │
  ├── 1. categoryRepository.findAllLevel3Ids() → [539 IDs ordenados]
  ├── 2. categoryId = l3Ids.get(0) → "76C86F19-2411-450E-8F69-DDE1DC6580E9"
  ├── 3. hasMore = (0 + 1 < 539) → true
  │
  ├── 4. LOOP: cjPage = 1
  │     ├── CJ API: GET listV2?page=1&size=100&categoryId=76C86F19-...
  │     ├── Respuesta: totalRecords=43, productList=[43 items]
  │     ├── Filtra: 43 productos → ¿cuáles NO están en BD?
  │     │     └── productRepository.existsById("2503140848341614700") → false → NUEVO
  │     │     └── productRepository.existsById("2408070523081609400") → false → NUEVO
  │     │     └── ... (filtra los 43)
  │     ├── newPids = [lista de PIDs nuevos]
  │     ├── fetchDetailAndSave(newPids):
  │     │     ├── Para cada PID nuevo (secuencial, 600ms entre cada uno):
  │     │     │     ├── CJ API: GET /product/query?pid=XXXXX
  │     │     │     ├── Mapea detail → Product entity
  │     │     │     └── productRepository.save(product) → created++ o updated++
  │     │     └── Total: created=38, updated=5
  │     ├── Check: 1 × 100 >= 43 → true → BREAK
  │     └── (No necesita page 2)
  │
  └── 5. Return: { created: 38, updated: 5, total: 43,
                    page: 0, hasMore: true, totalCategories: 539 }

Frontend: Recibe respuesta → muestra toast → sleep(2000ms) → offset++ → repite
```

---

## 7. Problemas identificados

### 7.1 CRÍTICO — Límite de 3 páginas por categoría

```java
private static final int MAX_CJ_PAGES_PER_CATEGORY = 3;  // ⚠️ Solo 300 productos/categoría
```

**Impacto**: Cualquier categoría con más de 300 productos **pierde datos**. CJ permite hasta 1.000 páginas × 100 = 100.000 productos.

**Categorías con muchos productos** (ejemplo real del response):
- "Men's Beach Pants" (`listedNum: 282` = muy popular, probablemente miles de productos)
- "Split Swimwear Skirt" (`listedNum: 419`)

**Recomendación**: Subir a al menos `10` páginas (1.000 productos/categoría) o mejor usar `totalPages` de la respuesta CJ:

```java
// En lugar de:
for (int cjPage = 1; cjPage <= MAX_CJ_PAGES_PER_CATEGORY; cjPage++)

// Usar:
int maxPages = Math.min(MAX_CJ_PAGES_PER_CATEGORY, pageResult.getTotalPages());
// Y subir MAX_CJ_PAGES_PER_CATEGORY a 1000
```

### 7.2 MEDIO — `totalPages` de CJ no se usa

El campo `totalPages` viene en la respuesta pero el backend calcula manualmente:
```java
if (cjPage * PAGE_SIZE >= totalCjProducts) break;
```

Esto funciona correctamente, pero el campo `totalPages` ya lo da calculado CJ. No es un bug, es redundancia.

### 7.3 BAJO — Sin paginación interna para detail

Cuando una categoría tiene 100 PIDs nuevos, `fetchDetailAndSave` los procesa todos secuencialmente con 600ms de delay:

```
100 productos × 600ms = 60 segundos solo en detail API para 1 página
3 páginas × 60 seg = 180 segundos (3 min) por categoría
539 categorías × 3 min = 1.617 min ≈ 27 horas (peor caso)
```

En la práctica es mucho menos porque la mayoría de productos ya existen en BD y se filtran.

---

## 8. Tiempos estimados por escenario

| Escenario | Categorías nuevas | Productos nuevos/cat (prom.) | Tiempo estimado |
|-----------|-------------------|------------------------------|-----------------|
| Primera importación | 539 | ~50 nuevos | ~9 horas |
| Actualización incremental | 539 | ~2-3 nuevos | ~30 min |
| Categoría individual | 1 | ~43 | ~30 seg |

**Cálculo**: 
- 539 categorías × 2s delay entre categorías = 18 min solo en delays
- 539 × 1 página CJ × ~1s respuesta = 9 min en API listV2
- N productos nuevos × 0.6s detail = variable

---

## 9. Resumen del estado actual

| Aspecto | Estado | Detalle |
|---------|--------|---------|
| ¿Itera todas las categorías L3? | ✅ Sí | Frontend incrementa offset 0..538 |
| ¿Pagina la API de CJ por categoría? | ⚠️ Parcial | Máx 3 páginas = 300 productos |
| ¿Para cuando productList está vacío? | ✅ Sí | `if (products.isEmpty()) break;` |
| ¿Para cuando se acabaron las páginas? | ✅ Sí | `if (cjPage * 100 >= totalRecords) break;` |
| ¿Filtra productos ya existentes? | ✅ Sí | `productRepository.existsById(id)` |
| ¿Rate limiting controlado? | ✅ Sí | 600ms delay + 3s backoff + 2 retries |
| ¿Persiste progreso (page refresh)? | ✅ Sí | localStorage + auto-resume |
| ¿Importa TODOS los productos de CJ? | ❌ No | Límite 300/categoría puede perder datos |

---

## 10. Recomendación de mejora prioritaria

Cambiar `MAX_CJ_PAGES_PER_CATEGORY` de `3` a un valor basado en `totalPages` de CJ:

```java
// ProductSyncUseCaseImpl.java línea 211
private static final int MAX_CJ_PAGES_PER_CATEGORY = 1000; // CJ permite hasta 1000
```

Con este cambio, el sistema iterará **todas** las páginas hasta que `productList` esté vacío o se alcance el `totalRecords`, que es el comportamiento que describes como esperado.
