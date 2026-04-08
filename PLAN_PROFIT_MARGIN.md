# Plan: Sistema de Margen de Ganancia (Profit Margin)

> **Fecha**: 8 de abril de 2026  
> **Microservicio**: mic-productcategory  
> **Autor**: PO Técnico  
> **Estado**: En desarrollo

---

## 1. Problema de Negocio

Actualmente los productos de la tienda muestran **el precio de costo del proveedor CJ Dropshipping** directamente al cliente final.  
Esto significa que **no se obtiene ganancia alguna** por cada venta.

### Flujo actual (sin margen)

```
CJ API (costPrice) → DB → API → Storefront → Cliente ve el precio de costo
```

### Datos concretos del problema

| Campo | Tipo DB | Ejemplo | Descripción |
|-------|---------|---------|-------------|
| `products.sell_price` | `VARCHAR(50)` | `"0.78 -- 0.81"` | Rango de precios CJ (String) |
| `product_details.sell_price` | `VARCHAR(50)` | `"3.45-3.97"` | Rango de precios CJ (String) |
| `product_detail_variants.variant_sell_price` | `NUMERIC(10,2)` | `6.11` | Precio exacto por variante (Money) |
| `product_detail_variants.variant_sug_sell_price` | `NUMERIC(10,2)` | `39.54` | Precio sugerido de CJ (Money) |

**Observación clave**: Solo las **variantes** tienen precios numéricos exactos. Los precios a nivel de producto son rangos en formato string.

---

## 2. Objetivo

Implementar un sistema de **reglas de margen de ganancia** configurable que:

1. **Calcule precios de venta** al público a partir del precio de costo + margen
2. **Sea administrable** desde el panel admin (por global, categoría, producto y variante)
3. **Se valide en el backend** para que no sea manipulable desde el frontend
4. **Soporte herencia de reglas**: Global → Categoría → Producto → Variante (la más específica gana)

### Flujo objetivo (con margen)

```
CJ costPrice → Regla de margen → retailPrice = costPrice × (1 + margin%) → API → Storefront
```

---

## 3. Alcance

### MVP (esta iteración)

- [x] Tabla `price_rules` con reglas de margen por scope
- [x] CRUD completo de reglas vía API REST
- [x] Cálculo automático de `retailPrice` en variantes al devolver productos
- [x] Recálculo del `sellPrice` (rango) del producto basado en variantes con margen
- [x] Endpoint admin para ver precios de costo vs. venta
- [x] Margen por defecto global del 40%

### Fase 2 (futura)

- Márgenes con fechas de vigencia (promos temporales)
- Reglas por segmento de cliente
- Dashboard de márgenes con métricas de ganancia
- Historial de cambios de reglas (auditoría)

### Fuera de alcance

- Cupones de descuento (sistema separado)
- Precios por región/país
- Impuestos (se calculan en checkout)

---

## 4. Diseño Técnico

### 4.1 Modelo de datos: `price_rules`

```sql
CREATE TABLE price_rules (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scope         VARCHAR(20) NOT NULL,  -- GLOBAL, CATEGORY, PRODUCT, VARIANT
    scope_id      VARCHAR(100),          -- NULL para GLOBAL, ID específico para otros
    margin_type   VARCHAR(20) NOT NULL DEFAULT 'PERCENTAGE', -- PERCENTAGE | FIXED
    margin_value  NUMERIC(10,2) NOT NULL, -- ej: 40.00 = 40%
    priority      INTEGER NOT NULL DEFAULT 0,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_scope CHECK (scope IN ('GLOBAL','CATEGORY','PRODUCT','VARIANT')),
    CONSTRAINT chk_margin_type CHECK (margin_type IN ('PERCENTAGE','FIXED')),
    CONSTRAINT chk_margin_value CHECK (margin_value >= 0),
    CONSTRAINT uq_scope_scope_id UNIQUE (scope, scope_id)
);

-- Insertar regla global por defecto: 40% de margen
INSERT INTO price_rules (scope, scope_id, margin_type, margin_value, priority, active)
VALUES ('GLOBAL', NULL, 'PERCENTAGE', 40.00, 0, TRUE);
```

### 4.2 Jerarquía de reglas (resolución de margen)

```
Prioridad de resolución (la primera encontrada activa gana):

1. VARIANT  (scope_id = variantId)   → Margen específico para esa variante
2. PRODUCT  (scope_id = productId)   → Margen para todas las variantes del producto
3. CATEGORY (scope_id = categoryId)  → Margen para todos los productos de la categoría
4. GLOBAL   (scope_id = NULL)        → Margen por defecto para todo el catálogo
```

### 4.3 Fórmula de cálculo

```
Si margin_type = PERCENTAGE:
    retailPrice = costPrice × (1 + marginValue / 100)
    Ejemplo: costPrice=6.11, margin=40% → retailPrice = 6.11 × 1.40 = 8.55

Si margin_type = FIXED:
    retailPrice = costPrice + marginValue
    Ejemplo: costPrice=6.11, margin=3.00 → retailPrice = 6.11 + 3.00 = 9.11

Redondeo: HALF_UP a 2 decimales
```

### 4.4 Capas del backend afectadas

```
┌─────────────────────────────────────────────────────────────────┐
│  API Layer                                                       │
│  PriceRuleController  → CRUD admin de reglas                     │
│  ProductController    → Productos con precios de venta aplicados │
├─────────────────────────────────────────────────────────────────┤
│  Application Layer                                               │
│  PriceRuleUseCase     → Gestión de reglas                        │
│  PricingService       → Motor de cálculo de margen               │
│  ProductUseCaseImpl   → Inyecta precios de venta en respuestas   │
├─────────────────────────────────────────────────────────────────┤
│  Domain Layer                                                    │
│  PriceRule            → Modelo de dominio                        │
│  PriceRuleRepository  → Puerto de persistencia                   │
├─────────────────────────────────────────────────────────────────┤
│  Infrastructure Layer                                            │
│  PriceRuleEntity      → JPA Entity                               │
│  PriceRuleJpaRepo     → Spring Data repository                   │
│  PriceRuleRepoImpl    → Implementación del puerto                │
└─────────────────────────────────────────────────────────────────┘
```

### 4.5 Campos nuevos en DTOs de respuesta

**ProductDtoOut** — campos adicionales:
```java
private String costPrice;       // Precio de costo original (solo admin)
private String retailPrice;     // Precio de venta con margen aplicado (público)
private String marginApplied;   // Porcentaje o valor de margen aplicado
```

**ProductDetailVariantDtoOut** — campos adicionales:
```java
private BigDecimal costPrice;          // variantSellPrice original de CJ
private BigDecimal retailPrice;        // Precio con margen aplicado
private BigDecimal retailSugSellPrice; // Sugerido con margen (opcional)
```

### 4.6 API REST de reglas de margen

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/price-rules` | Listar todas las reglas |
| `GET` | `/api/v1/price-rules/{id}` | Obtener regla por ID |
| `POST` | `/api/v1/price-rules` | Crear regla |
| `PUT` | `/api/v1/price-rules/{id}` | Actualizar regla |
| `DELETE` | `/api/v1/price-rules/{id}` | Eliminar regla |
| `GET` | `/api/v1/price-rules/resolve/{scope}/{scopeId}` | Preview: ver qué regla aplica |

---

## 5. Plan de Implementación

### Paso 1: Base de datos
- Crear `db.changelog-3.2.sql` con tabla `price_rules` + seed global
- Registrar en `db.changelog-master.yaml` como changeset 22

### Paso 2: Domain layer
- `PriceRule.java` — modelo de dominio
- `PriceRuleScope.java` — enum (GLOBAL, CATEGORY, PRODUCT, VARIANT)
- `MarginType.java` — enum (PERCENTAGE, FIXED)
- `PriceRuleRepository.java` — interfaz del puerto

### Paso 3: Infrastructure layer
- `PriceRuleEntity.java` — entidad JPA
- `PriceRuleJpaRepository.java` — Spring Data JPA repository
- `PriceRuleRepositoryImpl.java` — implementación del puerto
- `PriceRuleInfraMapper.java` — MapStruct mapper entity↔domain

### Paso 4: Application layer
- `PricingService.java` — motor de cálculo de margen
- `PriceRuleUseCase.java` — interfaz del caso de uso
- `PriceRuleUseCaseImpl.java` — implementación CRUD

### Paso 5: API layer
- `PriceRuleController.java` — endpoints REST
- `PriceRuleDtoIn.java` / `PriceRuleDtoOut.java` — DTOs
- `PriceRuleApiMapper.java` — MapStruct mapper DTO↔domain

### Paso 6: Integración con productos
- Modificar `ProductUseCaseImpl` para inyectar `PricingService`
- Modificar DTOs de producto para incluir `retailPrice`
- Aplicar margen a variantes y recalcular rango del producto

### Paso 7: Frontend (Ecomerce - React)
- Usar `retailPrice` como precio de venta (en vez de `sellPrice`)
- Mostrar `costPrice` tachado como "precio original" (efecto descuento visual)

---

## 6. Seguridad

- **Cálculo en backend**: El frontend NUNCA calcula precios. Solo los muestra.
- **Validación**: El `retailPrice` se calcula en `PricingService` y se aplica en la capa de aplicación.
- **Admin-only**: Los endpoints de `price-rules` deben estar protegidos con rol `ADMIN`.
- **Inmutabilidad**: El `costPrice` no se modifica, solo se agrega el `retailPrice` calculado.

---

## 7. Riesgos y Mitigaciones

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Sin regla configurada → precio 0 | Alto | Regla GLOBAL obligatoria con seed |
| Performance en listados grandes | Medio | Cache de reglas con TTL 5 min |
| Margen negativo (error admin) | Alto | Validación `margin_value >= 0` en DB y API |
| Precios inconsistentes entre producto/variante | Medio | Recálculo range a partir de variantes |

---

## 8. Métricas / KPIs

- **Margen promedio aplicado**: avg(marginValue) por scope
- **Revenue estimado**: sum(retailPrice - costPrice) × unidades vendidas
- **Cobertura de reglas**: % de productos con regla específica vs. global
