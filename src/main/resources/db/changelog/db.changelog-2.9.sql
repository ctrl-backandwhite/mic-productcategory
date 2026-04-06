-- ============================================================
-- Seed data: Reviews (idempotent via ON CONFLICT DO NOTHING)
-- Provides test data for 3 products with varied ratings
-- ============================================================

INSERT INTO reviews (id, product_id, user_id, author_name, rating, title, body, verified, status, helpful_count, images, created_at, updated_at)
VALUES
-- ── Product 1: 9CBAD815-1AD1-4AC8-B411-E4885A132E6F ─────────────────
('seed-review-001', '9CBAD815-1AD1-4AC8-B411-E4885A132E6F', 'user-001', 'María García', 5,
 'Excelente calidad', 'Superó mis expectativas. El material es resistente y el acabado impecable. Lo recomiendo sin dudas.',
 true, 'APPROVED', 12, '[]', NOW(), NOW()),

('seed-review-002', '9CBAD815-1AD1-4AC8-B411-E4885A132E6F', 'user-002', 'Carlos Rodríguez', 4,
 'Muy buen producto', 'Buena relación calidad-precio. El envío fue rápido y llegó bien embalado.',
 true, 'APPROVED', 8, '[]', NOW(), NOW()),

('seed-review-003', '9CBAD815-1AD1-4AC8-B411-E4885A132E6F', 'user-003', 'Ana López', 3,
 'Correcto, nada más', 'Cumple su función pero esperaba un poco más de calidad en los acabados.',
 false, 'APPROVED', 3, '[]', NOW(), NOW()),

('seed-review-004', '9CBAD815-1AD1-4AC8-B411-E4885A132E6F', NULL, 'Pedro Martínez', 5,
 'Increíble', '100% recomendado. Ya es mi segunda compra de este producto.',
 false, 'APPROVED', 15, '[]', NOW(), NOW()),

-- ── Product 2: 1481173876296781824 ───────────────────────────────────
('seed-review-005', '1481173876296781824', 'user-004', 'Laura Fernández', 5,
 'Lo mejor que he comprado', 'Llevo usándolo dos semanas y funciona de maravilla. Muy contenta con la compra.',
 true, 'APPROVED', 20, '[]', NOW(), NOW()),

('seed-review-006', '1481173876296781824', 'user-005', 'Jorge Sánchez', 2,
 'No es lo que esperaba', 'El producto no corresponde exactamente con las fotos. La calidad es inferior.',
 true, 'APPROVED', 5, '[]', NOW(), NOW()),

('seed-review-007', '1481173876296781824', NULL, 'Sofía Ruiz', 4,
 'Buena compra', 'Relación calidad-precio aceptable. Envío rápido.',
 false, 'APPROVED', 7, '[]', NOW(), NOW()),

('seed-review-008', '1481173876296781824', 'user-006', 'Miguel Torres', 4,
 'Satisfecho', 'Funciona como se describe. Sin sorpresas, que es lo que uno espera.',
 true, 'APPROVED', 4, '[]', NOW(), NOW()),

('seed-review-009', '1481173876296781824', NULL, 'Elena Díaz', 1,
 'Decepcionante', 'Llegó con un defecto y tuve que solicitar devolución. Mala experiencia.',
 false, 'PENDING', 2, '[]', NOW(), NOW()),

-- ── Product 3: 1370220426374025216 ───────────────────────────────────
('seed-review-010', '1370220426374025216', 'user-007', 'Roberto Navarro', 5,
 'Perfecto', 'Exactamente lo que buscaba. Calidad premium a buen precio.',
 true, 'APPROVED', 18, '[]', NOW(), NOW()),

('seed-review-011', '1370220426374025216', 'user-008', 'Carmen Iglesias', 4,
 'Muy bueno', 'Gran producto, aunque el manual de instrucciones podría mejorar.',
 false, 'APPROVED', 6, '[]', NOW(), NOW()),

('seed-review-012', '1370220426374025216', NULL, 'David Moreno', 3,
 'Regular', 'Se ve bien pero el material no es tan resistente como esperaba.',
 false, 'APPROVED', 1, '[]', NOW(), NOW())

ON CONFLICT (id) DO NOTHING;
