-- =============================================
-- Seed data: 25 media assets (idempotent)
-- Uses Unsplash Source for demo images
-- =============================================

INSERT INTO media_assets (id, filename, original_name, mime_type, size_bytes, url, thumbnail_url, category, tags, alt, width, height, created_at)
VALUES
-- ── PRODUCT images ──────────────────────────────────────────────────────
('seed-media-001', 'auriculares-premium.jpg', 'auriculares-premium.jpg', 'image/jpeg', 245000,
 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80',
 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=200&q=60',
 'PRODUCT', '["auriculares","audio","electrónica"]', 'Auriculares premium inalámbricos', 1920, 1080, NOW()),

('seed-media-002', 'smartwatch-deportivo.jpg', 'smartwatch-deportivo.jpg', 'image/jpeg', 189000,
 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&q=80',
 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=200&q=60',
 'PRODUCT', '["reloj","smartwatch","wearable"]', 'Smartwatch deportivo elegante', 1920, 1280, NOW()),

('seed-media-003', 'zapatillas-running.jpg', 'zapatillas-running.jpg', 'image/jpeg', 298000,
 'https://images.unsplash.com/photo-1491553895911-0055eca6402d?w=800&q=80',
 'https://images.unsplash.com/photo-1491553895911-0055eca6402d?w=200&q=60',
 'PRODUCT', '["zapatos","deporte","calzado","running"]', 'Zapatillas de running profesionales', 1920, 1080, NOW()),

('seed-media-004', 'mochila-urbana.jpg', 'mochila-urbana.jpg', 'image/jpeg', 156000,
 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80',
 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=200&q=60',
 'PRODUCT', '["mochila","accesorios","viaje"]', 'Mochila urbana resistente al agua', 1600, 1200, NOW()),

('seed-media-005', 'camara-profesional.jpg', 'camara-profesional.jpg', 'image/jpeg', 312000,
 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&q=80',
 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=200&q=60',
 'PRODUCT', '["cámara","fotografía","electrónica"]', 'Cámara mirrorless profesional', 1920, 1280, NOW()),

('seed-media-006', 'laptop-ultrabook.jpg', 'laptop-ultrabook.jpg', 'image/jpeg', 275000,
 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&q=80',
 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=200&q=60',
 'PRODUCT', '["laptop","computadora","tecnología"]', 'Laptop ultrabook con pantalla táctil', 1920, 1080, NOW()),

('seed-media-007', 'teclado-mecanico.jpg', 'teclado-mecanico.jpg', 'image/jpeg', 198000,
 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&q=80',
 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=200&q=60',
 'PRODUCT', '["teclado","gaming","periféricos"]', 'Teclado mecánico RGB gaming', 1920, 1080, NOW()),

('seed-media-008', 'altavoz-bluetooth.jpg', 'altavoz-bluetooth.jpg', 'image/jpeg', 167000,
 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=800&q=80',
 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=200&q=60',
 'PRODUCT', '["altavoz","bluetooth","audio"]', 'Altavoz Bluetooth portátil', 1600, 1200, NOW()),

('seed-media-009', 'smartphone-flagship.jpg', 'smartphone-flagship.jpg', 'image/jpeg', 220000,
 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80',
 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=200&q=60',
 'PRODUCT', '["smartphone","móvil","tecnología"]', 'Smartphone flagship última generación', 1920, 1280, NOW()),

('seed-media-010', 'tablet-dibujo.jpg', 'tablet-dibujo.jpg', 'image/jpeg', 245000,
 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800&q=80',
 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=200&q=60',
 'PRODUCT', '["tablet","dibujo","diseño"]', 'Tablet para diseño y dibujo digital', 1920, 1080, NOW()),

('seed-media-011', 'gafas-sol.jpg', 'gafas-sol.jpg', 'image/jpeg', 134000,
 'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=800&q=80',
 'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=200&q=60',
 'PRODUCT', '["gafas","accesorios","moda"]', 'Gafas de sol polarizadas premium', 1920, 1280, NOW()),

('seed-media-012', 'perfume-lujo.jpg', 'perfume-lujo.jpg', 'image/jpeg', 178000,
 'https://images.unsplash.com/photo-1585386959984-a4155224a1ad?w=800&q=80',
 'https://images.unsplash.com/photo-1585386959984-a4155224a1ad?w=200&q=60',
 'PRODUCT', '["perfume","fragancia","belleza"]', 'Perfume de lujo unisex', 1920, 1280, NOW()),

('seed-media-013', 'aspiradora-robot.jpg', 'aspiradora-robot.jpg', 'image/jpeg', 201000,
 'https://images.unsplash.com/photo-1558618666-fcd25c85f82e?w=800&q=80',
 'https://images.unsplash.com/photo-1558618666-fcd25c85f82e?w=200&q=60',
 'PRODUCT', '["aspiradora","robot","hogar"]', 'Aspiradora robot inteligente con mapeo', 1600, 1200, NOW()),

('seed-media-014', 'silla-gaming.jpg', 'silla-gaming.jpg', 'image/jpeg', 267000,
 'https://images.unsplash.com/photo-1598550476439-6847785fcea6?w=800&q=80',
 'https://images.unsplash.com/photo-1598550476439-6847785fcea6?w=200&q=60',
 'PRODUCT', '["silla","gaming","ergonomía","oficina"]', 'Silla gaming ergonómica reclinable', 1920, 1080, NOW()),

('seed-media-015', 'cafetera-espresso.jpg', 'cafetera-espresso.jpg', 'image/jpeg', 189000,
 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=800&q=80',
 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=200&q=60',
 'PRODUCT', '["cafetera","cocina","electrodoméstico"]', 'Cafetera espresso automática', 1920, 1280, NOW()),

-- ── BRAND images ────────────────────────────────────────────────────────
('seed-media-016', 'brand-apple.jpg', 'brand-apple.jpg', 'image/jpeg', 45000,
 'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&q=80',
 'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=200&q=60',
 'BRAND', '["apple","marca","tecnología"]', 'Logo Apple en producto', 800, 800, NOW()),

('seed-media-017', 'brand-samsung.jpg', 'brand-samsung.jpg', 'image/jpeg', 42000,
 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=800&q=80',
 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=200&q=60',
 'BRAND', '["samsung","marca","electrónica"]', 'Producto Samsung Galaxy', 800, 800, NOW()),

('seed-media-018', 'brand-nike.jpg', 'brand-nike.jpg', 'image/jpeg', 38000,
 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80',
 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=200&q=60',
 'BRAND', '["nike","marca","deporte"]', 'Zapatilla Nike roja', 800, 800, NOW()),

('seed-media-019', 'brand-sony.jpg', 'brand-sony.jpg', 'image/jpeg', 41000,
 'https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=800&q=80',
 'https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=200&q=60',
 'BRAND', '["sony","marca","gaming","playstation"]', 'Consola Sony PlayStation', 800, 800, NOW()),

('seed-media-020', 'brand-dyson.jpg', 'brand-dyson.jpg', 'image/jpeg', 39000,
 'https://images.unsplash.com/photo-1527443060795-0402a218799d?w=800&q=80',
 'https://images.unsplash.com/photo-1527443060795-0402a218799d?w=200&q=60',
 'BRAND', '["dyson","marca","hogar"]', 'Ventilador Dyson sin aspas', 800, 800, NOW()),

-- ── SLIDE / BANNER images ───────────────────────────────────────────────
('seed-media-021', 'banner-tecnologia.jpg', 'banner-tecnologia.jpg', 'image/jpeg', 512000,
 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200&q=80',
 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=200&q=60',
 'SLIDE', '["banner","tecnología","promoción"]', 'Banner promoción tecnología', 2560, 1440, NOW()),

('seed-media-022', 'banner-deporte.jpg', 'banner-deporte.jpg', 'image/jpeg', 489000,
 'https://images.unsplash.com/photo-1461896836934-bd45ba688f1b?w=1200&q=80',
 'https://images.unsplash.com/photo-1461896836934-bd45ba688f1b?w=200&q=60',
 'SLIDE', '["banner","deporte","fitness"]', 'Banner colección deportiva', 2560, 1440, NOW()),

('seed-media-023', 'banner-hogar.jpg', 'banner-hogar.jpg', 'image/jpeg', 467000,
 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=1200&q=80',
 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=200&q=60',
 'SLIDE', '["banner","hogar","decoración"]', 'Banner ofertas hogar y cocina', 2560, 1440, NOW()),

-- ── GENERAL images ──────────────────────────────────────────────────────
('seed-media-024', 'envio-gratis.jpg', 'envio-gratis.jpg', 'image/jpeg', 98000,
 'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=800&q=80',
 'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=200&q=60',
 'GENERAL', '["envío","delivery","logística"]', 'Paquete de envío rápido', 1200, 800, NOW()),

('seed-media-025', 'atencion-cliente.jpg', 'atencion-cliente.jpg', 'image/jpeg', 112000,
 'https://images.unsplash.com/photo-1556745757-8d76bdb6984b?w=800&q=80',
 'https://images.unsplash.com/photo-1556745757-8d76bdb6984b?w=200&q=60',
 'GENERAL', '["soporte","atención","servicio"]', 'Atención al cliente profesional', 1200, 800, NOW())

ON CONFLICT (id) DO NOTHING;
