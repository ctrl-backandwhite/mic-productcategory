-- =============================================
-- Seed data: 20 warranties (idempotent)
-- Types: MANUFACTURER, STORE, EXTENDED, LIMITED
-- =============================================

INSERT INTO warranties (id, name, type, duration_months, coverage, conditions, includes_labor, includes_parts, includes_pickup, repair_limit, contact_phone, contact_email, active, created_at, updated_at)
VALUES
-- ── MANUFACTURER warranties ─────────────────────────────────────────────
('seed-warranty-001', 'Garantía Estándar del Fabricante', 'MANUFACTURER', 12,
 'Cubre defectos de fabricación en materiales y mano de obra bajo uso normal.',
 'No cubre daños por mal uso, caídas ni líquidos. Requiere factura original.',
 true, true, false, NULL, '+34 900 100 001', 'soporte@fabricante.com', true, NOW(), NOW()),

('seed-warranty-002', 'Garantía Premium del Fabricante', 'MANUFACTURER', 24,
 'Cobertura total: defectos de fábrica, componentes internos y pantalla.',
 'Se excluyen daños cosméticos y modificaciones no autorizadas. Registro online obligatorio.',
 true, true, true, NULL, '+34 900 100 002', 'premium@fabricante.com', true, NOW(), NOW()),

('seed-warranty-003', 'Garantía Fabricante Electrónica', 'MANUFACTURER', 36,
 'Cobertura completa para componentes electrónicos y placa base.',
 'Válida solo con número de serie registrado. No incluye accesorios.',
 true, true, false, 3, '+34 900 100 003', 'electronica@fabricante.com', true, NOW(), NOW()),

('seed-warranty-004', 'Garantía Fabricante Electrodomésticos', 'MANUFACTURER', 24,
 'Cobertura de motor, compresor y componentes principales del electrodoméstico.',
 'No cubre daños por sobretensión eléctrica ni uso comercial.',
 true, true, true, 2, '+34 900 100 004', 'electrodomesticos@fabricante.com', true, NOW(), NOW()),

('seed-warranty-005', 'Garantía Fabricante Básica', 'MANUFACTURER', 6,
 'Cobertura mínima por defectos de fabricación evidentes.',
 'Solo aplica a productos vendidos en canales oficiales.',
 false, true, false, 1, '+34 900 100 005', 'basica@fabricante.com', true, NOW(), NOW()),

-- ── STORE warranties ────────────────────────────────────────────────────
('seed-warranty-006', 'Garantía Tienda Satisfacción Total', 'STORE', 12,
 'Cobertura completa incluyendo devolución o cambio durante el primer año.',
 'Producto debe estar en condiciones razonables. Sin gastos de envío.',
 true, true, true, NULL, '+34 900 200 006', 'garantia@tienda.com', true, NOW(), NOW()),

('seed-warranty-007', 'Garantía Tienda Básica', 'STORE', 6,
 'Cambio directo por unidad nueva en caso de defecto confirmado.',
 'Aplica solo a productos comprados directamente en tienda o web oficial.',
 false, true, false, 1, '+34 900 200 007', 'soporte@tienda.com', true, NOW(), NOW()),

('seed-warranty-008', 'Garantía Tienda Premium', 'STORE', 24,
 'Reparación o reemplazo sin coste. Incluye recogida a domicilio.',
 'No cubre accidentes ni daños por agua. Máximo 1 dispositivo por cliente.',
 true, true, true, 3, '+34 900 200 008', 'premium@tienda.com', true, NOW(), NOW()),

('seed-warranty-009', 'Garantía Tienda Tecnología', 'STORE', 18,
 'Cobertura especial para dispositivos tecnológicos: portátiles, tablets y smartphones.',
 'Incluye diagnóstico gratuito. No cubre software ni daños por virus.',
 true, true, false, 2, '+34 900 200 009', 'tech@tienda.com', true, NOW(), NOW()),

('seed-warranty-010', 'Garantía Tienda Moda', 'STORE', 3,
 'Cambio por defectos en costuras, cremalleras y materiales durante 3 meses.',
 'No cubre desgaste normal, decoloración por lavado ni modificaciones.',
 false, false, false, 1, '+34 900 200 010', 'moda@tienda.com', true, NOW(), NOW()),

-- ── EXTENDED warranties ─────────────────────────────────────────────────
('seed-warranty-011', 'Extensión de Garantía 3 Años', 'EXTENDED', 36,
 'Extensión de cobertura del fabricante por 3 años adicionales. Incluye mano de obra y piezas.',
 'Se activa al finalizar la garantía del fabricante. Requiere contrato firmado.',
 true, true, false, NULL, '+34 900 300 011', 'extension@garantias.com', true, NOW(), NOW()),

('seed-warranty-012', 'Extensión Premium 5 Años', 'EXTENDED', 60,
 'Cobertura extendida total durante 5 años. Incluye recogida, reparación y sustitución.',
 'Solo disponible para productos con precio superior a 200€. No acumulable con otras ofertas.',
 true, true, true, NULL, '+34 900 300 012', 'premium5@garantias.com', true, NOW(), NOW()),

('seed-warranty-013', 'Extensión Pantalla y Display', 'EXTENDED', 24,
 'Cobertura adicional exclusiva para pantallas: rotura accidental, píxeles muertos y retroiluminación.',
 'Máximo 2 reparaciones. No cubre daños intencionados.',
 true, true, false, 2, '+34 900 300 013', 'pantalla@garantias.com', true, NOW(), NOW()),

('seed-warranty-014', 'Extensión Hogar Inteligente', 'EXTENDED', 48,
 'Cobertura para dispositivos IoT y domótica: sensores, hubs, cámaras y asistentes.',
 'Requiere conexión activa al servicio en la nube del fabricante.',
 true, true, true, 3, '+34 900 300 014', 'hogar@garantias.com', true, NOW(), NOW()),

('seed-warranty-015', 'Extensión Gaming Pro', 'EXTENDED', 36,
 'Cobertura extendida para hardware gaming: consolas, monitores, teclados y ratones.',
 'Incluye limpieza profesional anual. No cubre accesorios de menos de 30€.',
 true, true, false, 4, '+34 900 300 015', 'gaming@garantias.com', true, NOW(), NOW()),

-- ── LIMITED warranties ──────────────────────────────────────────────────
('seed-warranty-016', 'Garantía Limitada Componentes', 'LIMITED', 12,
 'Cobertura exclusiva para componentes internos: placa, procesador y memoria.',
 'No incluye carcasa, batería ni pantalla. Una sola reparación por incidencia.',
 true, true, false, 1, '+34 900 400 016', 'componentes@garantialimitada.com', true, NOW(), NOW()),

('seed-warranty-017', 'Garantía Limitada Batería', 'LIMITED', 6,
 'Sustitución de batería si la capacidad cae por debajo del 80% de la original.',
 'Requiere diagnóstico oficial. No aplica si la batería ha sido reemplazada por terceros.',
 false, true, false, 1, '+34 900 400 017', 'bateria@garantialimitada.com', true, NOW(), NOW()),

('seed-warranty-018', 'Garantía Limitada Motor', 'LIMITED', 24,
 'Cobertura del motor y sistema de transmisión en electrodomésticos y herramientas.',
 'No cubre desgaste por uso industrial ni piezas plásticas externas.',
 true, true, false, 2, '+34 900 400 018', 'motor@garantialimitada.com', true, NOW(), NOW()),

('seed-warranty-019', 'Garantía Limitada Software', 'LIMITED', 12,
 'Reinstalación y soporte para el software preinstalado de fábrica.',
 'No cubre recuperación de datos, virus ni software de terceros.',
 true, false, false, 3, '+34 900 400 019', 'software@garantialimitada.com', false, NOW(), NOW()),

('seed-warranty-020', 'Garantía Limitada Accesorios', 'LIMITED', 3,
 'Cambio por defecto de fabricación en accesorios incluidos: cables, cargadores, fundas.',
 'Solo accesorios originales incluidos en la caja. Sin reparación, solo sustitución.',
 false, true, false, 1, '+34 900 400 020', 'accesorios@garantialimitada.com', false, NOW(), NOW())

ON CONFLICT (id) DO NOTHING;
