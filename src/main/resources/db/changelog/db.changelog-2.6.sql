-- =============================================
-- Seed data: 20 attributes + values (idempotent)
-- =============================================

-- 1. Color
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-color', 'Color', 'color', 'COLOR', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, color_hex, position, created_at) VALUES
('seed-av-color-black',   'seed-attr-color', 'Negro',    '#000000', 1, NOW()),
('seed-av-color-white',   'seed-attr-color', 'Blanco',   '#FFFFFF', 2, NOW()),
('seed-av-color-red',     'seed-attr-color', 'Rojo',     '#EF4444', 3, NOW()),
('seed-av-color-blue',    'seed-attr-color', 'Azul',     '#3B82F6', 4, NOW()),
('seed-av-color-green',   'seed-attr-color', 'Verde',    '#22C55E', 5, NOW()),
('seed-av-color-yellow',  'seed-attr-color', 'Amarillo', '#EAB308', 6, NOW()),
('seed-av-color-pink',    'seed-attr-color', 'Rosa',     '#EC4899', 7, NOW()),
('seed-av-color-gray',    'seed-attr-color', 'Gris',     '#6B7280', 8, NOW()),
('seed-av-color-navy',    'seed-attr-color', 'Marino',   '#1E3A5F', 9, NOW()),
('seed-av-color-brown',   'seed-attr-color', 'Marrón',   '#92400E', 10, NOW())
ON CONFLICT (id) DO NOTHING;

-- 2. Talla de ropa
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-clothing-size', 'Talla de ropa', 'talla-ropa', 'SIZE', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-cs-xs',  'seed-attr-clothing-size', 'XS',  1, NOW()),
('seed-av-cs-s',   'seed-attr-clothing-size', 'S',   2, NOW()),
('seed-av-cs-m',   'seed-attr-clothing-size', 'M',   3, NOW()),
('seed-av-cs-l',   'seed-attr-clothing-size', 'L',   4, NOW()),
('seed-av-cs-xl',  'seed-attr-clothing-size', 'XL',  5, NOW()),
('seed-av-cs-2xl', 'seed-attr-clothing-size', '2XL', 6, NOW()),
('seed-av-cs-3xl', 'seed-attr-clothing-size', '3XL', 7, NOW())
ON CONFLICT (id) DO NOTHING;

-- 3. Talla de calzado (EU)
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-shoe-size', 'Talla de calzado', 'talla-calzado', 'SIZE', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-ss-36', 'seed-attr-shoe-size', '36', 1, NOW()),
('seed-av-ss-37', 'seed-attr-shoe-size', '37', 2, NOW()),
('seed-av-ss-38', 'seed-attr-shoe-size', '38', 3, NOW()),
('seed-av-ss-39', 'seed-attr-shoe-size', '39', 4, NOW()),
('seed-av-ss-40', 'seed-attr-shoe-size', '40', 5, NOW()),
('seed-av-ss-41', 'seed-attr-shoe-size', '41', 6, NOW()),
('seed-av-ss-42', 'seed-attr-shoe-size', '42', 7, NOW()),
('seed-av-ss-43', 'seed-attr-shoe-size', '43', 8, NOW()),
('seed-av-ss-44', 'seed-attr-shoe-size', '44', 9, NOW()),
('seed-av-ss-45', 'seed-attr-shoe-size', '45', 10, NOW())
ON CONFLICT (id) DO NOTHING;

-- 4. Material
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-material', 'Material', 'material', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-mat-cotton',    'seed-attr-material', 'Algodón',     1, NOW()),
('seed-av-mat-polyester', 'seed-attr-material', 'Poliéster',   2, NOW()),
('seed-av-mat-leather',   'seed-attr-material', 'Cuero',       3, NOW()),
('seed-av-mat-nylon',     'seed-attr-material', 'Nylon',       4, NOW()),
('seed-av-mat-metal',     'seed-attr-material', 'Metal',       5, NOW()),
('seed-av-mat-wood',      'seed-attr-material', 'Madera',      6, NOW()),
('seed-av-mat-plastic',   'seed-attr-material', 'Plástico',    7, NOW()),
('seed-av-mat-glass',     'seed-attr-material', 'Vidrio',      8, NOW())
ON CONFLICT (id) DO NOTHING;

-- 5. Capacidad de almacenamiento
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-storage', 'Almacenamiento', 'almacenamiento', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-sto-32gb',  'seed-attr-storage', '32 GB',  1, NOW()),
('seed-av-sto-64gb',  'seed-attr-storage', '64 GB',  2, NOW()),
('seed-av-sto-128gb', 'seed-attr-storage', '128 GB', 3, NOW()),
('seed-av-sto-256gb', 'seed-attr-storage', '256 GB', 4, NOW()),
('seed-av-sto-512gb', 'seed-attr-storage', '512 GB', 5, NOW()),
('seed-av-sto-1tb',   'seed-attr-storage', '1 TB',   6, NOW()),
('seed-av-sto-2tb',   'seed-attr-storage', '2 TB',   7, NOW())
ON CONFLICT (id) DO NOTHING;

-- 6. Memoria RAM
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-ram', 'Memoria RAM', 'memoria-ram', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-ram-4gb',  'seed-attr-ram', '4 GB',  1, NOW()),
('seed-av-ram-8gb',  'seed-attr-ram', '8 GB',  2, NOW()),
('seed-av-ram-16gb', 'seed-attr-ram', '16 GB', 3, NOW()),
('seed-av-ram-32gb', 'seed-attr-ram', '32 GB', 4, NOW()),
('seed-av-ram-64gb', 'seed-attr-ram', '64 GB', 5, NOW())
ON CONFLICT (id) DO NOTHING;

-- 7. Tamaño de pantalla
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-screen-size', 'Tamaño de pantalla', 'tamano-pantalla', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-scr-5',   'seed-attr-screen-size', '5"',    1, NOW()),
('seed-av-scr-6',   'seed-attr-screen-size', '6.1"',  2, NOW()),
('seed-av-scr-6-7', 'seed-attr-screen-size', '6.7"',  3, NOW()),
('seed-av-scr-13',  'seed-attr-screen-size', '13"',   4, NOW()),
('seed-av-scr-14',  'seed-attr-screen-size', '14"',   5, NOW()),
('seed-av-scr-15',  'seed-attr-screen-size', '15.6"', 6, NOW()),
('seed-av-scr-17',  'seed-attr-screen-size', '17"',   7, NOW()),
('seed-av-scr-27',  'seed-attr-screen-size', '27"',   8, NOW()),
('seed-av-scr-32',  'seed-attr-screen-size', '32"',   9, NOW()),
('seed-av-scr-55',  'seed-attr-screen-size', '55"',   10, NOW()),
('seed-av-scr-65',  'seed-attr-screen-size', '65"',   11, NOW())
ON CONFLICT (id) DO NOTHING;

-- 8. Conectividad
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-connectivity', 'Conectividad', 'conectividad', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-conn-wifi',  'seed-attr-connectivity', 'Wi-Fi',      1, NOW()),
('seed-av-conn-bt',    'seed-attr-connectivity', 'Bluetooth',  2, NOW()),
('seed-av-conn-usbc',  'seed-attr-connectivity', 'USB-C',      3, NOW()),
('seed-av-conn-usba',  'seed-attr-connectivity', 'USB-A',      4, NOW()),
('seed-av-conn-hdmi',  'seed-attr-connectivity', 'HDMI',       5, NOW()),
('seed-av-conn-nfc',   'seed-attr-connectivity', 'NFC',        6, NOW()),
('seed-av-conn-5g',    'seed-attr-connectivity', '5G',         7, NOW()),
('seed-av-conn-aux',   'seed-attr-connectivity', 'Jack 3.5mm', 8, NOW())
ON CONFLICT (id) DO NOTHING;

-- 9. Voltaje
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-voltage', 'Voltaje', 'voltaje', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-volt-110', 'seed-attr-voltage', '110V', 1, NOW()),
('seed-av-volt-220', 'seed-attr-voltage', '220V', 2, NOW()),
('seed-av-volt-duo', 'seed-attr-voltage', '110V-220V', 3, NOW())
ON CONFLICT (id) DO NOTHING;

-- 10. Peso
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-weight', 'Peso', 'peso', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-wt-100g',  'seed-attr-weight', '100 g',   1, NOW()),
('seed-av-wt-250g',  'seed-attr-weight', '250 g',   2, NOW()),
('seed-av-wt-500g',  'seed-attr-weight', '500 g',   3, NOW()),
('seed-av-wt-1kg',   'seed-attr-weight', '1 kg',    4, NOW()),
('seed-av-wt-2kg',   'seed-attr-weight', '2 kg',    5, NOW()),
('seed-av-wt-5kg',   'seed-attr-weight', '5 kg',    6, NOW()),
('seed-av-wt-10kg',  'seed-attr-weight', '10 kg',   7, NOW())
ON CONFLICT (id) DO NOTHING;

-- 11. Resolución de pantalla
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-resolution', 'Resolución', 'resolucion', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-res-hd',   'seed-attr-resolution', 'HD (720p)',    1, NOW()),
('seed-av-res-fhd',  'seed-attr-resolution', 'Full HD (1080p)', 2, NOW()),
('seed-av-res-2k',   'seed-attr-resolution', '2K (1440p)',   3, NOW()),
('seed-av-res-4k',   'seed-attr-resolution', '4K (2160p)',   4, NOW()),
('seed-av-res-8k',   'seed-attr-resolution', '8K (4320p)',   5, NOW())
ON CONFLICT (id) DO NOTHING;

-- 12. Sistema operativo
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-os', 'Sistema operativo', 'sistema-operativo', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-os-windows', 'seed-attr-os', 'Windows',  1, NOW()),
('seed-av-os-macos',   'seed-attr-os', 'macOS',    2, NOW()),
('seed-av-os-linux',   'seed-attr-os', 'Linux',    3, NOW()),
('seed-av-os-android', 'seed-attr-os', 'Android',  4, NOW()),
('seed-av-os-ios',     'seed-attr-os', 'iOS',      5, NOW()),
('seed-av-os-chromeos','seed-attr-os', 'ChromeOS', 6, NOW())
ON CONFLICT (id) DO NOTHING;

-- 13. Tipo de procesador
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-processor', 'Procesador', 'procesador', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-proc-i5',    'seed-attr-processor', 'Intel Core i5',      1, NOW()),
('seed-av-proc-i7',    'seed-attr-processor', 'Intel Core i7',      2, NOW()),
('seed-av-proc-i9',    'seed-attr-processor', 'Intel Core i9',      3, NOW()),
('seed-av-proc-r5',    'seed-attr-processor', 'AMD Ryzen 5',        4, NOW()),
('seed-av-proc-r7',    'seed-attr-processor', 'AMD Ryzen 7',        5, NOW()),
('seed-av-proc-r9',    'seed-attr-processor', 'AMD Ryzen 9',        6, NOW()),
('seed-av-proc-m2',    'seed-attr-processor', 'Apple M2',           7, NOW()),
('seed-av-proc-m3',    'seed-attr-processor', 'Apple M3',           8, NOW()),
('seed-av-proc-snap',  'seed-attr-processor', 'Snapdragon 8 Gen 3', 9, NOW())
ON CONFLICT (id) DO NOTHING;

-- 14. Capacidad de batería
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-battery', 'Batería', 'bateria', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-bat-3000', 'seed-attr-battery', '3000 mAh', 1, NOW()),
('seed-av-bat-4000', 'seed-attr-battery', '4000 mAh', 2, NOW()),
('seed-av-bat-5000', 'seed-attr-battery', '5000 mAh', 3, NOW()),
('seed-av-bat-6000', 'seed-attr-battery', '6000 mAh', 4, NOW()),
('seed-av-bat-10k',  'seed-attr-battery', '10000 mAh', 5, NOW()),
('seed-av-bat-20k',  'seed-attr-battery', '20000 mAh', 6, NOW())
ON CONFLICT (id) DO NOTHING;

-- 15. Eficiencia energética
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-energy', 'Eficiencia energética', 'eficiencia-energetica', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-en-a3', 'seed-attr-energy', 'A+++', 1, NOW()),
('seed-av-en-a2', 'seed-attr-energy', 'A++',  2, NOW()),
('seed-av-en-a1', 'seed-attr-energy', 'A+',   3, NOW()),
('seed-av-en-a',  'seed-attr-energy', 'A',    4, NOW()),
('seed-av-en-b',  'seed-attr-energy', 'B',    5, NOW()),
('seed-av-en-c',  'seed-attr-energy', 'C',    6, NOW())
ON CONFLICT (id) DO NOTHING;

-- 16. Tipo de cámara
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-camera', 'Cámara', 'camara', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-cam-12',  'seed-attr-camera', '12 MP',  1, NOW()),
('seed-av-cam-48',  'seed-attr-camera', '48 MP',  2, NOW()),
('seed-av-cam-50',  'seed-attr-camera', '50 MP',  3, NOW()),
('seed-av-cam-108', 'seed-attr-camera', '108 MP', 4, NOW()),
('seed-av-cam-200', 'seed-attr-camera', '200 MP', 5, NOW())
ON CONFLICT (id) DO NOTHING;

-- 17. Tipo de teclado
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-keyboard', 'Tipo de teclado', 'tipo-teclado', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-kb-mech',  'seed-attr-keyboard', 'Mecánico',    1, NOW()),
('seed-av-kb-membr', 'seed-attr-keyboard', 'Membrana',    2, NOW()),
('seed-av-kb-scis',  'seed-attr-keyboard', 'Tijera',      3, NOW()),
('seed-av-kb-chic',  'seed-attr-keyboard', 'Chiclet',     4, NOW())
ON CONFLICT (id) DO NOTHING;

-- 18. Género
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-gender', 'Género', 'genero', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-gen-m', 'seed-attr-gender', 'Hombre',   1, NOW()),
('seed-av-gen-f', 'seed-attr-gender', 'Mujer',    2, NOW()),
('seed-av-gen-u', 'seed-attr-gender', 'Unisex',   3, NOW()),
('seed-av-gen-k', 'seed-attr-gender', 'Infantil', 4, NOW())
ON CONFLICT (id) DO NOTHING;

-- 19. Estilo
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-style', 'Estilo', 'estilo', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-sty-casual',  'seed-attr-style', 'Casual',    1, NOW()),
('seed-av-sty-formal',  'seed-attr-style', 'Formal',    2, NOW()),
('seed-av-sty-sport',   'seed-attr-style', 'Deportivo', 3, NOW()),
('seed-av-sty-urban',   'seed-attr-style', 'Urbano',    4, NOW()),
('seed-av-sty-classic', 'seed-attr-style', 'Clásico',   5, NOW())
ON CONFLICT (id) DO NOTHING;

-- 20. Certificación
INSERT INTO attributes (id, name, slug, type, created_at) VALUES
('seed-attr-certification', 'Certificación', 'certificacion', 'SELECT', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO attribute_values (id, attribute_id, value, position, created_at) VALUES
('seed-av-cert-ip67',  'seed-attr-certification', 'IP67',          1, NOW()),
('seed-av-cert-ip68',  'seed-attr-certification', 'IP68',          2, NOW()),
('seed-av-cert-milstd','seed-attr-certification', 'MIL-STD-810G',  3, NOW()),
('seed-av-cert-ce',    'seed-attr-certification', 'CE',            4, NOW()),
('seed-av-cert-fcc',   'seed-attr-certification', 'FCC',           5, NOW()),
('seed-av-cert-rohs',  'seed-attr-certification', 'RoHS',          6, NOW())
ON CONFLICT (id) DO NOTHING;
