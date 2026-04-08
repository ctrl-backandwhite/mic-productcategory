# Plan: Márgenes de Ganancia por Rangos de Precio

## Objetivo

Permitir definir márgenes de ganancia diferenciados según el **rango de precio de costo** del producto. Ejemplo:

| Rango de precio de costo | Margen |
|--------------------------|--------|
| $0.00 – $10.00          | 80%    |
| $10.01 – $50.00         | 60%    |
| $50.01 – $100.00        | 40%    |
| $100.01 – ∞             | 25%    |

Esto se combina con el sistema de scopes existente (GLOBAL → CATEGORY → PRODUCT → VARIANT).

---

## 1. Base de Datos

### 1.1 Migración: `db.changelog-3.3.sql`

```sql
-- Agregar columnas de rango de precio
ALTER TABLE price_rules ADD COLUMN min_price NUMERIC(10,2) DEFAULT NULL;
ALTER TABLE price_rules ADD COLUMN max_price NUMERIC(10,2) DEFAULT NULL;

-- Eliminar constraint UNIQUE actual (scope, scope_id) porque ahora
-- puede haber múltiples reglas por scope con diferentes rangos
ALTER TABLE price_rules DROP CONSTRAINT IF EXISTS uq_price_rules_scope;

-- Nuevo constraint: (scope, scope_id, min_price, max_price) debe ser único
-- Usamos índice parcial para manejar NULLs correctamente
CREATE UNIQUE INDEX uq_price_rules_scope_range
ON price_rules (scope, COALESCE(scope_id, ''), COALESCE(min_price, -1), COALESCE(max_price, -1));

-- Índice para búsqueda eficiente por rango
CREATE INDEX idx_price_rules_price_range ON price_rules (min_price, max_price);

-- Constraint: min_price < max_price cuando ambos están definidos
ALTER TABLE price_rules ADD CONSTRAINT chk_price_range
  CHECK (min_price IS NULL OR max_price IS NULL OR min_price < max_price);
```

### 1.2 Lógica de rangos

- `min_price = NULL` y `max_price = NULL` → Regla sin rango (aplica a cualquier precio, funciona como fallback)
- `min_price = 0` y `max_price = 10` → Aplica a productos con costo entre $0 y $10
- `min_price = 10.01` y `max_price = NULL` → Aplica a productos con costo >= $10.01 (sin tope)
- `min_price = NULL` y `max_price = 50` → Aplica a productos con costo <= $50

---

## 2. Backend

### 2.1 Modelo de dominio: `PriceRule.java`

Agregar campos:
```java
private BigDecimal minPrice;  // Precio mínimo del rango (nullable)
private BigDecimal maxPrice;  // Precio máximo del rango (nullable)
```

### 2.2 Entidad JPA: `PriceRuleEntity.java`

Agregar columnas:
```java
@Column(name = "min_price", precision = 10, scale = 2)
private BigDecimal minPrice;

@Column(name = "max_price", precision = 10, scale = 2)
private BigDecimal maxPrice;
```

Eliminar `@UniqueConstraint` de `@Table` (ahora se maneja con índice en DB).

### 2.3 DTOs

**`PriceRuleDtoIn.java`**: Agregar `minPrice`, `maxPrice` con `@DecimalMin("0.00")`  
**`PriceRuleDtoOut.java`**: Agregar `minPrice`, `maxPrice`

### 2.4 `PricingService.java` — Nueva lógica de resolución

```
resolveRule(variantId, productId, categoryId, costPrice):
  1. Obtener todas las reglas activas
  2. Para cada scope (VARIANT → PRODUCT → CATEGORY → GLOBAL):
     a. Filtrar reglas del scope que coincidan con el scopeId
     b. De esas, buscar la que tenga rango que contenga el costPrice
        - costPrice >= minPrice (si minPrice no es null)
        - costPrice <= maxPrice (si maxPrice no es null)
     c. Si hay match por rango → usar esa regla
     d. Si no hay match por rango → buscar regla sin rango (fallback)
     e. Si encontró regla → retornarla
  3. Si ningún scope tiene regla → sin margen
```

**Prioridad de resolución:**
1. Regla con rango que coincida (más específica)
2. Regla sin rango del mismo scope (fallback)
3. Subir al scope padre

### 2.5 `PriceRuleRepositoryImpl.java`

Actualizar método `update()` para incluir `minPrice` y `maxPrice`.

### 2.6 Mappers

Los mappers MapStruct (`PriceRuleInfraMapper`, `PriceRuleApiMapper`) detectan automáticamente los nuevos campos por convención de nombres.

---

## 3. Frontend

### 3.1 Tipos: `PriceRuleRepository.ts`

```typescript
export interface PriceRule {
    // ... campos existentes
    minPrice: number | null;
    maxPrice: number | null;
}

export interface PriceRulePayload {
    // ... campos existentes
    minPrice?: number | null;
    maxPrice?: number | null;
}
```

### 3.2 Admin UI: `AdminPricing.tsx`

**Modal de creación/edición:**
- Agregar sección "Rango de precio de costo" con campos `Min ($)` y `Max ($)`
- Validación: min < max cuando ambos están definidos
- Campos opcionales (vacío = sin límite)

**Tabla:**
- Nueva columna "Rango" que muestra el rango de precios
- Formato: `$0.00 – $10.00`, `≥ $10.01`, `≤ $50.00`, o `Sin rango`

**Vista previa:**
- Usar el valor medio del rango para el ejemplo de cálculo

---

## 4. Flujo de ejemplo

### Configuración del admin:
1. Crear regla GLOBAL: $0–$10, PERCENTAGE 80%
2. Crear regla GLOBAL: $10.01–$50, PERCENTAGE 60%
3. Crear regla GLOBAL: $50.01–$100, PERCENTAGE 40%
4. Crear regla GLOBAL: $100.01–∞, PERCENTAGE 25%
5. Crear regla GLOBAL sin rango: PERCENTAGE 35% (fallback)

### Resolución:
- Producto con costo $5.00 → Regla 1 (80%) → Precio venta: $9.00
- Producto con costo $25.00 → Regla 2 (60%) → Precio venta: $40.00
- Producto con costo $75.00 → Regla 3 (40%) → Precio venta: $105.00
- Producto con costo $150.00 → Regla 4 (25%) → Precio venta: $187.50
- Producto sin costo parseable → Regla 5 (fallback 35%)

---

## 5. Archivos a modificar

### Backend (mic-productcategory)
| Archivo | Cambio |
|---------|--------|
| `db.changelog-3.3.sql` | Nueva migración con columnas y constraints |
| `PriceRule.java` | +minPrice, +maxPrice |
| `PriceRuleEntity.java` | +minPrice, +maxPrice, quitar unique constraint |
| `PriceRuleDtoIn.java` | +minPrice, +maxPrice |
| `PriceRuleDtoOut.java` | +minPrice, +maxPrice |
| `PricingService.java` | Resolución por rango de precio |
| `PriceRuleRepositoryImpl.java` | Update incluye min/maxPrice |

### Frontend (Ecomerce)
| Archivo | Cambio |
|---------|--------|
| `PriceRuleRepository.ts` | +minPrice, +maxPrice en tipos |
| `AdminPricing.tsx` | Campos rango en modal, columna rango en tabla |

---

## 6. Riesgos y mitigación

| Riesgo | Mitigación |
|--------|-----------|
| Regla existente GLOBAL sin rango sigue funcionando | Si no encuentra match por rango, usa regla sin rango como fallback |
| Superposición de rangos | El índice único previene duplicados exactos; la resolución toma el primero por prioridad |
| Performance con muchas reglas | Cache de 5 min existente; filtrado en memoria es O(n) con n típico < 100 |
