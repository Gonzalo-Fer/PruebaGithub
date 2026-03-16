-- ============================================================
--  DATOS DE PRUEBA — Sistema de Gestión de Hostelería
--  Orden de inserción respetando claves foráneas:
--  areas → puestos → empleados → turnos → fichajes →
--  propinas → nominas → evaluaciones → formacion →
--  ausencias → mensajes_chat
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- ============================================================
-- ÁREAS
-- ============================================================
INSERT INTO areas (id_area, nombre, descripcion, tipo, responsable_id, estado) VALUES
(1, 'Cocina Central',      'Cocina principal del restaurante, zona caliente y fría',        'cocina',        NULL, 'activa'),
(2, 'Sala Principal',      'Comedor con capacidad para 80 comensales',                      'sala',          NULL, 'activa'),
(3, 'Barra y Cafetería',   'Zona de barra, cafés, copas y cócteles',                        'bar',           NULL, 'activa'),
(4, 'Recepción Hotel',     'Recepción 24h, check-in y check-out de huéspedes',              'recepcion',     NULL, 'activa'),
(5, 'Limpieza y Lavandería','Mantenimiento de habitaciones y zonas comunes',                'limpieza',      NULL, 'activa'),
(6, 'Administración',      'Gestión contable, RRHH y proveedores',                          'administracion',NULL, 'activa'),
(7, 'Salón de Eventos',    'Sala polivalente para banquetes, bodas y reuniones corporativas','eventos',       NULL, 'activa');

-- ============================================================
-- PUESTOS
-- ============================================================
INSERT INTO puestos (id_puesto, titulo, categoria, descripcion, salario_base, requiere_certificacion, nivel) VALUES
(1,  'Jefe de Cocina',           'cocinero',       'Responsable de la cocina, planificación de menús y equipo',         2800.00, TRUE,  'jefe'),
(2,  'Cocinero/a Senior',        'cocinero',       'Elaboración de platos principales, partida caliente',               1950.00, TRUE,  'senior'),
(3,  'Cocinero/a',               'cocinero',       'Apoyo en todas las partidas',                                       1600.00, TRUE,  'junior'),
(4,  'Ayudante de Cocina',       'ayudante',       'Preparaciones básicas, limpieza y orden de cocina',                 1350.00, TRUE,  'aprendiz'),
(5,  'Maître',                   'maitre',         'Responsable de sala, gestión de reservas y equipo de sala',         2600.00, FALSE, 'jefe'),
(6,  'Camarero/a Senior',        'camarero',       'Atención al cliente, gestión de mesa completa',                     1700.00, FALSE, 'senior'),
(7,  'Camarero/a',               'camarero',       'Toma de comandas, servicio de mesa',                                1450.00, FALSE, 'junior'),
(8,  'Ayudante de Camarero',     'ayudante',       'Apoyo en sala, servicio de bebidas y desbrasado',                   1300.00, FALSE, 'aprendiz'),
(9,  'Barman / Barlady',         'barman',         'Elaboración de cócteles, gestión de barra',                         1750.00, FALSE, 'senior'),
(10, 'Sommelier',                'sommelier',      'Carta de vinos, maridaje y cata',                                   2100.00, TRUE,  'senior'),
(11, 'Recepcionista Senior',     'recepcionista',  'Gestión check-in/out, atención VIP y coordinación',                 1800.00, FALSE, 'senior'),
(12, 'Recepcionista',            'recepcionista',  'Atención al huésped, reservas y facturación',                       1500.00, FALSE, 'junior'),
(13, 'Auxiliar de Limpieza',     'limpieza',       'Limpieza de habitaciones y zonas comunes',                          1300.00, FALSE, 'junior'),
(14, 'Gobernanta/e',             'gerente',        'Supervisión del equipo de pisos y estándares de calidad',           2000.00, FALSE, 'jefe'),
(15, 'Gerente de Restaurante',   'gerente',        'Dirección operativa del restaurante y F&B',                         3200.00, FALSE, 'gerente');

-- ============================================================
-- EMPLEADOS
-- (responsable_id de areas se actualiza al final)
-- ============================================================
INSERT INTO empleados (id_empleado, nombre, apellidos, dni, email, telefono, fecha_nacimiento, fecha_contratacion, fecha_baja, id_area, id_puesto, salario_base, tipo_contrato, carnet_manipulador, fecha_carnet_manipulador, numero_seguridad_social, estado) VALUES
-- Cocina
(1,  'Carlos',    'Martínez Ruiz',        '12345678A', 'carlos.martinez@hosteleria.com',  '611000001', '1985-03-14', '2018-01-15', NULL, 1, 1,  2800.00, 'indefinido',    TRUE,  '2022-06-01', '28-1234567-89', 'activo'),
(2,  'Lucía',     'Fernández García',     '23456789B', 'lucia.fernandez@hosteleria.com',  '611000002', '1990-07-22', '2019-03-01', NULL, 1, 2,  1950.00, 'indefinido',    TRUE,  '2022-06-01', '28-2345678-90', 'activo'),
(3,  'Mohamed',   'El Amrani',            '34567890C', 'mohamed.elamrani@hosteleria.com', '611000003', '1993-11-05', '2021-06-01', NULL, 1, 3,  1600.00, 'indefinido',    TRUE,  '2023-01-15', '28-3456789-01', 'activo'),
(4,  'Sandra',    'López Jiménez',        '45678901D', 'sandra.lopez@hosteleria.com',     '611000004', '1999-02-18', '2023-09-01', NULL, 1, 4,  1350.00, 'temporal',      TRUE,  '2023-09-01', '28-4567890-12', 'activo'),
-- Sala
(5,  'Roberto',   'Sánchez Morales',      '56789012E', 'roberto.sanchez@hosteleria.com',  '611000005', '1982-09-30', '2016-05-10', NULL, 2, 5,  2600.00, 'indefinido',    FALSE, NULL,         '28-5678901-23', 'activo'),
(6,  'Ana',       'Torres Vega',          '67890123F', 'ana.torres@hosteleria.com',       '611000006', '1988-04-12', '2017-09-01', NULL, 2, 6,  1700.00, 'indefinido',    FALSE, NULL,         '28-6789012-34', 'activo'),
(7,  'Javier',    'Romero Castillo',      '78901234G', 'javier.romero@hosteleria.com',    '611000007', '1995-12-03', '2020-11-15', NULL, 2, 7,  1450.00, 'indefinido',    FALSE, NULL,         '28-7890123-45', 'activo'),
(8,  'Elena',     'Díaz Navarro',         '89012345H', 'elena.diaz@hosteleria.com',       '611000008', '1997-06-27', '2022-04-01', NULL, 2, 8,  1300.00, 'temporal',      FALSE, NULL,         '28-8901234-56', 'activo'),
(9,  'Pablo',     'Herrera Molina',       '90123456I', 'pablo.herrera@hosteleria.com',    '611000009', '2000-01-14', '2023-06-01', NULL, 2, 8,  1300.00, 'fines_semana',  FALSE, NULL,         '28-9012345-67', 'activo'),
-- Bar
(10, 'Natalia',   'Gutiérrez Pardo',      '01234567J', 'natalia.gutierrez@hosteleria.com','611000010', '1991-08-19', '2019-07-01', NULL, 3, 9,  1750.00, 'indefinido',    FALSE, NULL,         '28-0123456-78', 'activo'),
(11, 'Iván',      'Moreno Blanco',        '11223344K', 'ivan.moreno@hosteleria.com',      '611000011', '1994-03-08', '2021-01-10', NULL, 3, 9,  1750.00, 'indefinido',    FALSE, NULL,         '28-1122334-45', 'activo'),
(12, 'Carmen',    'Jiménez Soler',        '22334455L', 'carmen.jimenez@hosteleria.com',   '611000012', '1987-10-25', '2015-08-01', NULL, 3, 10, 2100.00, 'indefinido',    TRUE,  '2021-03-10', '28-2233445-56', 'activo'),
-- Recepción
(13, 'David',     'Ruiz Méndez',          '33445566M', 'david.ruiz@hosteleria.com',       '611000013', '1989-05-16', '2018-11-01', NULL, 4, 11, 1800.00, 'indefinido',    FALSE, NULL,         '28-3344556-67', 'activo'),
(14, 'Sofía',     'Pérez Castro',         '44556677N', 'sofia.perez@hosteleria.com',      '611000014', '1996-09-04', '2022-02-15', NULL, 4, 12, 1500.00, 'indefinido',    FALSE, NULL,         '28-4455667-78', 'activo'),
(15, 'Álvaro',    'García Fuentes',       '55667788O', 'alvaro.garcia@hosteleria.com',    '611000015', '1998-12-21', '2023-03-01', NULL, 4, 12, 1500.00, 'temporal',      FALSE, NULL,         '28-5566778-89', 'activo'),
-- Limpieza
(16, 'Rosa',      'Martín Iglesias',      '66778899P', 'rosa.martin@hosteleria.com',      '611000016', '1980-07-09', '2014-06-01', NULL, 5, 13, 1300.00, 'indefinido',    FALSE, NULL,         '28-6677889-90', 'activo'),
(17, 'Fátima',    'Benali Tahiri',        '77889900Q', 'fatima.benali@hosteleria.com',    '611000017', '1986-02-28', '2016-10-01', NULL, 5, 13, 1300.00, 'indefinido',    FALSE, NULL,         '28-7788990-01', 'activo'),
(18, 'Amparo',    'Serrano Campos',       '88990011R', 'amparo.serrano@hosteleria.com',   '611000018', '1979-11-17', '2012-03-15', NULL, 5, 14, 2000.00, 'indefinido',    FALSE, NULL,         '28-8899001-12', 'activo'),
-- Administración
(19, 'Miguel',    'Blanco Ortega',        '99001122S', 'miguel.blanco@hosteleria.com',    '611000019', '1983-04-06', '2017-01-09', NULL, 6, 15, 3200.00, 'indefinido',    FALSE, NULL,         '28-9900112-23', 'activo'),
-- Empleado inactivo (ejemplo de baja)
(20, 'Teresa',    'Vidal Romero',         '10111213T', 'teresa.vidal@hosteleria.com',     '611000020', '1992-06-13', '2020-05-01', '2024-02-28', 2, 7, 1450.00, 'temporal', FALSE, NULL,       '28-1011121-34', 'baja_definitiva');

-- Actualizar responsables de área
UPDATE areas SET responsable_id = 1  WHERE id_area = 1;  -- Carlos → Cocina
UPDATE areas SET responsable_id = 5  WHERE id_area = 2;  -- Roberto → Sala
UPDATE areas SET responsable_id = 10 WHERE id_area = 3;  -- Natalia → Bar
UPDATE areas SET responsable_id = 13 WHERE id_area = 4;  -- David → Recepción
UPDATE areas SET responsable_id = 18 WHERE id_area = 5;  -- Amparo → Limpieza
UPDATE areas SET responsable_id = 19 WHERE id_area = 6;  -- Miguel → Administración

-- ============================================================
-- TURNOS — semana del 24 al 28 de febrero de 2025
-- ============================================================
INSERT INTO turnos (id_turno, id_empleado, fecha, hora_inicio, hora_fin, tipo_turno, area_asignada, horas_trabajadas, estado) VALUES
-- Lunes 24/02
(1,  1,  '2025-02-24', '08:00', '16:00', 'mañana',  'Cocina Central',    8.00, 'completado'),
(2,  2,  '2025-02-24', '08:00', '16:00', 'mañana',  'Cocina Central',    8.00, 'completado'),
(3,  3,  '2025-02-24', '14:00', '22:00', 'tarde',   'Cocina Central',    8.00, 'completado'),
(4,  5,  '2025-02-24', '11:00', '16:00', 'partido', 'Sala Principal',    5.00, 'completado'),
(5,  5,  '2025-02-24', '19:00', '23:00', 'partido', 'Sala Principal',    4.00, 'completado'),
(6,  6,  '2025-02-24', '11:00', '23:00', 'completo','Sala Principal',   11.00, 'completado'),
(7,  7,  '2025-02-24', '19:00', '23:30', 'noche',   'Sala Principal',    4.50, 'completado'),
(8,  10, '2025-02-24', '16:00', '00:00', 'tarde',   'Barra y Cafetería', 8.00, 'completado'),
(9,  13, '2025-02-24', '07:00', '15:00', 'mañana',  'Recepción Hotel',   8.00, 'completado'),
(10, 14, '2025-02-24', '15:00', '23:00', 'tarde',   'Recepción Hotel',   8.00, 'completado'),
-- Martes 25/02
(11, 1,  '2025-02-25', '08:00', '16:00', 'mañana',  'Cocina Central',    8.00, 'completado'),
(12, 2,  '2025-02-25', '08:00', '16:00', 'mañana',  'Cocina Central',    8.00, 'completado'),
(13, 4,  '2025-02-25', '10:00', '18:00', 'mañana',  'Cocina Central',    8.00, 'completado'),
(14, 6,  '2025-02-25', '11:00', '23:00', 'completo','Sala Principal',   11.00, 'completado'),
(15, 8,  '2025-02-25', '19:00', '23:30', 'noche',   'Sala Principal',    4.50, 'completado'),
(16, 11, '2025-02-25', '18:00', '02:00', 'noche',   'Barra y Cafetería', 8.00, 'completado'),
(17, 16, '2025-02-25', '08:00', '14:00', 'mañana',  'Limpieza y Lavandería', 6.00, 'completado'),
(18, 17, '2025-02-25', '08:00', '14:00', 'mañana',  'Limpieza y Lavandería', 6.00, 'completado'),
-- Miércoles 26/02
(19, 3,  '2025-02-26', '08:00', '16:00', 'mañana',  'Cocina Central',    8.00, 'completado'),
(20, 5,  '2025-02-26', '11:00', '16:00', 'partido', 'Sala Principal',    5.00, 'completado'),
(21, 5,  '2025-02-26', '19:00', '23:00', 'partido', 'Sala Principal',    4.00, 'completado'),
(22, 7,  '2025-02-26', '11:00', '23:00', 'completo','Sala Principal',   11.00, 'completado'),
(23, 9,  '2025-02-26', '19:00', '23:30', 'noche',   'Sala Principal',    4.50, 'completado'),
(24, 12, '2025-02-26', '16:00', '00:00', 'tarde',   'Barra y Cafetería', 8.00, 'completado'),
(25, 15, '2025-02-26', '15:00', '23:00', 'tarde',   'Recepción Hotel',   8.00, 'completado'),
-- Jueves 27/02
(26, 1,  '2025-02-27', '08:00', '17:00', 'mañana',  'Cocina Central',    9.00, 'completado'),
(27, 2,  '2025-02-27', '14:00', '22:00', 'tarde',   'Cocina Central',    8.00, 'completado'),
(28, 6,  '2025-02-27', '11:00', '23:00', 'completo','Sala Principal',   11.00, 'completado'),
(29, 10, '2025-02-27', '16:00', '00:00', 'tarde',   'Barra y Cafetería', 8.00, 'completado'),
(30, 13, '2025-02-27', '23:00', '07:00', 'noche',   'Recepción Hotel',   8.00, 'completado'),
-- Viernes 28/02
(31, 1,  '2025-02-28', '08:00', '18:00', 'mañana',  'Cocina Central',   10.00, 'completado'),
(32, 2,  '2025-02-28', '08:00', '16:00', 'mañana',  'Cocina Central',    8.00, 'completado'),
(33, 3,  '2025-02-28', '14:00', '23:00', 'tarde',   'Cocina Central',    9.00, 'completado'),
(34, 5,  '2025-02-28', '11:00', '16:00', 'partido', 'Sala Principal',    5.00, 'completado'),
(35, 5,  '2025-02-28', '19:00', '00:00', 'partido', 'Sala Principal',    5.00, 'completado'),
(36, 6,  '2025-02-28', '19:00', '02:00', 'noche',   'Sala Principal',    7.00, 'completado'),
(37, 7,  '2025-02-28', '11:00', '23:00', 'completo','Sala Principal',   11.00, 'completado'),
(38, 8,  '2025-02-28', '19:00', '23:30', 'noche',   'Sala Principal',    4.50, 'completado'),
(39, 9,  '2025-02-28', '19:00', '23:30', 'noche',   'Sala Principal',    4.50, 'completado'),
(40, 10, '2025-02-28', '16:00', '02:00', 'noche',   'Barra y Cafetería',10.00, 'completado'),
(41, 11, '2025-02-28', '20:00', '04:00', 'noche',   'Barra y Cafetería', 8.00, 'completado'),
(42, 12, '2025-02-28', '16:00', '02:00', 'noche',   'Barra y Cafetería',10.00, 'completado'),
-- Turnos futuros (marzo 2025)
(43, 1,  '2025-03-03', '08:00', '16:00', 'mañana',  'Cocina Central',    8.00, 'programado'),
(44, 2,  '2025-03-03', '08:00', '16:00', 'mañana',  'Cocina Central',    8.00, 'programado'),
(45, 5,  '2025-03-03', '11:00', '16:00', 'partido', 'Sala Principal',    5.00, 'programado'),
(46, 6,  '2025-03-03', '11:00', '23:00', 'completo','Sala Principal',   11.00, 'programado'),
(47, 13, '2025-03-03', '07:00', '15:00', 'mañana',  'Recepción Hotel',   8.00, 'programado'),
-- Turno con ausencia
(48, 4,  '2025-02-26', '10:00', '18:00', 'mañana',  'Cocina Central',    8.00, 'ausente');

-- ============================================================
-- FICHAJES
-- ============================================================
INSERT INTO fichajes (id_fichaje, id_empleado, id_turno, fecha, hora_entrada, hora_salida, horas_trabajadas, horas_extra, retraso_minutos, observaciones) VALUES
(1,  1,  1,  '2025-02-24', '07:55', '16:10', 8.25, 0.25, 0,  NULL),
(2,  2,  2,  '2025-02-24', '08:05', '16:00', 7.92, 0.00, 5,  'Retraso por tráfico'),
(3,  3,  3,  '2025-02-24', '14:00', '22:15', 8.25, 0.25, 0,  NULL),
(4,  5,  4,  '2025-02-24', '11:00', '16:00', 5.00, 0.00, 0,  NULL),
(5,  5,  5,  '2025-02-24', '19:00', '23:05', 4.08, 0.08, 0,  NULL),
(6,  6,  6,  '2025-02-24', '10:58', '23:10', 11.20,0.20, 0,  NULL),
(7,  7,  7,  '2025-02-24', '19:00', '23:40', 4.67, 0.17, 0,  NULL),
(8,  10, 8,  '2025-02-24', '16:02', '00:05', 8.05, 0.05, 2,  NULL),
(9,  13, 9,  '2025-02-24', '07:00', '15:00', 8.00, 0.00, 0,  NULL),
(10, 14, 10, '2025-02-24', '15:00', '23:00', 8.00, 0.00, 0,  NULL),
(11, 1,  11, '2025-02-25', '08:00', '16:00', 8.00, 0.00, 0,  NULL),
(12, 2,  12, '2025-02-25', '08:00', '16:30', 8.50, 0.50, 0,  'Cierre de inventario mensual'),
(13, 4,  13, '2025-02-25', '10:00', '18:00', 8.00, 0.00, 0,  NULL),
(14, 6,  14, '2025-02-25', '11:00', '23:15', 11.25,0.25, 0,  NULL),
(15, 8,  15, '2025-02-25', '19:10', '23:30', 4.33, 0.00, 10, 'Retraso justificado — médico'),
(16, 11, 16, '2025-02-25', '18:00', '02:10', 8.17, 0.17, 0,  NULL),
(17, 16, 17, '2025-02-25', '08:00', '14:00', 6.00, 0.00, 0,  NULL),
(18, 17, 18, '2025-02-25', '07:55', '14:05', 6.17, 0.17, 0,  NULL),
(19, 3,  19, '2025-02-26', '08:00', '16:00', 8.00, 0.00, 0,  NULL),
(20, 5,  20, '2025-02-26', '11:00', '16:00', 5.00, 0.00, 0,  NULL),
(21, 5,  21, '2025-02-26', '19:00', '23:00', 4.00, 0.00, 0,  NULL),
(22, 7,  22, '2025-02-26', '10:55', '23:20', 11.42,0.42, 0,  NULL),
(23, 9,  23, '2025-02-26', '19:00', '23:30', 4.50, 0.00, 0,  NULL),
(24, 12, 24, '2025-02-26', '16:00', '00:00', 8.00, 0.00, 0,  NULL),
(25, 15, 25, '2025-02-26', '15:00', '23:00', 8.00, 0.00, 0,  NULL),
(26, 1,  26, '2025-02-27', '08:00', '17:30', 9.50, 0.50, 0,  'Reunión con proveedor de pescadería'),
(27, 2,  27, '2025-02-27', '14:00', '22:00', 8.00, 0.00, 0,  NULL),
(28, 6,  28, '2025-02-27', '11:00', '23:00', 11.00,0.00, 0,  NULL),
(29, 10, 29, '2025-02-27', '16:00', '00:20', 8.33, 0.33, 0,  NULL),
(30, 13, 30, '2025-02-27', '23:00', '07:00', 8.00, 0.00, 0,  NULL),
(31, 1,  31, '2025-02-28', '08:00', '19:00',11.00, 1.00, 0,  'Preparación menú degustación especial'),
(32, 2,  32, '2025-02-28', '08:00', '16:00', 8.00, 0.00, 0,  NULL),
(33, 3,  33, '2025-02-28', '14:00', '23:30', 9.50, 0.50, 0,  NULL),
(34, 5,  34, '2025-02-28', '11:00', '16:00', 5.00, 0.00, 0,  NULL),
(35, 5,  35, '2025-02-28', '19:00', '00:30', 5.50, 0.50, 0,  'Evento privado'),
(36, 6,  36, '2025-02-28', '19:00', '02:30', 7.50, 0.50, 0,  NULL),
(37, 7,  37, '2025-02-28', '11:00', '23:00',11.00, 0.00, 0,  NULL),
(38, 8,  38, '2025-02-28', '19:00', '23:30', 4.50, 0.00, 0,  NULL),
(39, 9,  39, '2025-02-28', '19:00', '23:30', 4.50, 0.00, 0,  NULL),
(40, 10, 40, '2025-02-28', '16:00', '02:15',10.25, 0.25, 0,  NULL),
(41, 11, 41, '2025-02-28', '20:00', '04:10', 8.17, 0.17, 0,  NULL),
(42, 12, 42, '2025-02-28', '16:00', '02:30',10.50, 0.50, 0,  NULL);

-- ============================================================
-- PROPINAS — febrero 2025
-- ============================================================
INSERT INTO propinas (id_propina, id_empleado, fecha, turno, importe, tipo, metodo_pago) VALUES
-- Semana del 24 al 28 de febrero — sala y bar
(1,  6,  '2025-02-24', 'tarde',  45.00, 'individual',  'efectivo'),
(2,  7,  '2025-02-24', 'noche',  30.00, 'individual',  'efectivo'),
(3,  10, '2025-02-24', 'tarde',  60.00, 'individual',  'mixto'),
(4,  5,  '2025-02-24', 'tarde',  25.00, 'compartida',  'efectivo'),
(5,  6,  '2025-02-25', 'tarde',  38.00, 'individual',  'efectivo'),
(6,  8,  '2025-02-25', 'noche',  20.00, 'individual',  'efectivo'),
(7,  11, '2025-02-25', 'noche',  75.00, 'individual',  'tarjeta'),
(8,  7,  '2025-02-26', 'tarde',  42.00, 'individual',  'efectivo'),
(9,  9,  '2025-02-26', 'noche',  28.00, 'individual',  'efectivo'),
(10, 12, '2025-02-26', 'tarde',  55.00, 'individual',  'mixto'),
(11, 5,  '2025-02-27', 'tarde',  35.00, 'compartida',  'efectivo'),
(12, 6,  '2025-02-27', 'tarde',  50.00, 'individual',  'efectivo'),
(13, 10, '2025-02-27', 'tarde',  80.00, 'individual',  'tarjeta'),
-- Viernes noche (especial)
(14, 5,  '2025-02-28', 'noche', 120.00, 'compartida',  'efectivo'),
(15, 6,  '2025-02-28', 'noche',  90.00, 'individual',  'efectivo'),
(16, 7,  '2025-02-28', 'tarde',  65.00, 'individual',  'efectivo'),
(17, 8,  '2025-02-28', 'noche',  45.00, 'compartida',  'efectivo'),
(18, 9,  '2025-02-28', 'noche',  45.00, 'compartida',  'efectivo'),
(19, 10, '2025-02-28', 'noche', 150.00, 'individual',  'tarjeta'),
(20, 11, '2025-02-28', 'noche', 130.00, 'individual',  'mixto'),
(21, 12, '2025-02-28', 'noche', 110.00, 'individual',  'tarjeta'),
-- Propinas del bote común (repartidas entre sala)
(22, 6,  '2025-02-28', 'noche',  80.00, 'bote_comun',  'efectivo'),
(23, 7,  '2025-02-28', 'noche',  80.00, 'bote_comun',  'efectivo'),
(24, 8,  '2025-02-28', 'noche',  80.00, 'bote_comun',  'efectivo');

-- ============================================================
-- NÓMINAS — enero y febrero 2025
-- ============================================================
INSERT INTO nominas (id_nomina, id_empleado, mes, anio, salario_base, horas_extra, propinas, complementos, deducciones, seguridad_social, irpf, total_neto, fecha_pago, estado) VALUES
-- Enero 2025
(1,  1,  1, 2025, 2800.00, 120.00, 0.00,   150.00, 0.00, 267.90, 364.04, 2438.06, '2025-01-31', 'pagada'),
(2,  2,  1, 2025, 1950.00,  75.00, 0.00,    80.00, 0.00, 186.23, 217.35, 1701.42, '2025-01-31', 'pagada'),
(3,  3,  1, 2025, 1600.00,   0.00, 0.00,     0.00, 0.00, 152.96, 157.12, 1289.92, '2025-01-31', 'pagada'),
(4,  4,  1, 2025, 1350.00,   0.00, 0.00,     0.00, 0.00, 129.06,  89.10, 1131.84, '2025-01-31', 'pagada'),
(5,  5,  1, 2025, 2600.00,   0.00, 320.00, 100.00, 0.00, 248.56, 322.44, 2449.00, '2025-01-31', 'pagada'),
(6,  6,  1, 2025, 1700.00,  50.00, 410.00,  80.00, 0.00, 162.56, 213.49, 1864.75, '2025-01-31', 'pagada'),
(7,  7,  1, 2025, 1450.00,   0.00, 280.00,   0.00, 0.00, 138.60, 158.62, 1432.78, '2025-01-31', 'pagada'),
(8,  10, 1, 2025, 1750.00, 100.00, 520.00,  50.00, 0.00, 167.30, 232.20, 2020.50, '2025-01-31', 'pagada'),
(9,  13, 1, 2025, 1800.00,   0.00, 0.00,   100.00, 0.00, 172.04, 197.00, 1530.96, '2025-01-31', 'pagada'),
(10, 19, 1, 2025, 3200.00,   0.00, 0.00,   200.00, 0.00, 305.92, 476.16, 2617.92, '2025-01-31', 'pagada'),
-- Febrero 2025 — pendientes de pago
(11, 1,  2, 2025, 2800.00, 180.00, 0.00,   150.00, 0.00, 267.90, 376.51, 2485.59, NULL, 'pendiente'),
(12, 2,  2, 2025, 1950.00,  75.00, 0.00,    80.00, 0.00, 186.23, 217.35, 1701.42, NULL, 'pendiente'),
(13, 5,  2, 2025, 2600.00,  45.00, 395.00, 100.00, 0.00, 248.56, 335.29, 2556.15, NULL, 'pendiente'),
(14, 6,  2, 2025, 1700.00, 100.00, 538.00,  80.00, 0.00, 162.56, 241.62, 2013.82, NULL, 'pendiente'),
(15, 10, 2, 2025, 1750.00, 100.00, 635.00,  50.00, 0.00, 167.30, 256.35, 2111.35, NULL, 'pendiente'),
(16, 19, 2, 2025, 3200.00,   0.00, 0.00,   200.00, 0.00, 305.92, 476.16, 2617.92, NULL, 'pendiente');

-- ============================================================
-- EVALUACIONES — último trimestre 2024
-- ============================================================
INSERT INTO evaluaciones (id_evaluacion, id_empleado, id_evaluador, fecha, periodo, puntualidad, atencion_cliente, trabajo_equipo, conocimiento_producto, higiene_presentacion, puntuacion_total, comentarios) VALUES
(1,  2,  1,  '2024-12-15', 'Q4 2024', 9, 8, 9, 9, 10,  9, 'Excelente rendimiento en partida caliente. Candidata a ascenso a jefa de partida.'),
(2,  3,  1,  '2024-12-15', 'Q4 2024', 7, 7, 8, 7,  9,  8, 'Buen trabajador. Mejorar velocidad en servicio. Se recomienda formación en pastelería.'),
(3,  4,  1,  '2024-12-15', 'Q4 2024', 6, 6, 7, 5,  8,  6, 'Primer año en cocina profesional. Actitud positiva pero necesita práctica.'),
(4,  6,  5,  '2024-12-18', 'Q4 2024', 9, 10, 9, 8, 10,  9, 'Una de las mejores camareras del equipo. Muy valorada por los clientes habituales.'),
(5,  7,  5,  '2024-12-18', 'Q4 2024', 8, 8, 8, 7,  9,  8, 'Buen desempeño general. Mejorar conocimiento de la carta de vinos.'),
(6,  8,  5,  '2024-12-18', 'Q4 2024', 5, 7, 7, 6,  8,  7, 'Varios retrasos en el trimestre. Hablar en privado sobre gestión del tiempo.'),
(7,  9,  5,  '2024-12-18', 'Q4 2024', 9, 9, 8, 7,  9,  8, 'Muy polivalente. Cubre bien tanto sala como barra cuando se necesita.'),
(8,  10, 19, '2024-12-20', 'Q4 2024',10, 9, 9,10, 10, 10, 'Desempeño sobresaliente. Ha incrementado ventas de vinos un 18% este trimestre.'),
(9,  11, 19, '2024-12-20', 'Q4 2024', 8, 8, 9, 8,  9,  8, 'Buen barman. Muy creativo con los cócteles de temporada.'),
(10, 13, 19, '2024-12-22', 'Q4 2024', 9, 9, 8, 8,  9,  9, 'Perfecto en atención nocturna. Gran manejo de situaciones difíciles.'),
(11, 14, 13, '2024-12-22', 'Q4 2024', 8, 9, 8, 7,  9,  8, 'Rápida aprendizaje. Buena actitud con los huéspedes internacionales.'),
(12, 16, 18, '2024-12-10', 'Q4 2024', 9, 7, 9, 7, 10,  8, 'Muy meticulosa en su trabajo. Habitaciones siempre impecables.'),
(13, 17, 18, '2024-12-10', 'Q4 2024', 8, 7, 8, 7, 10,  8, 'Gran rendimiento. Proponer para formación en coordinación de equipos.');

-- ============================================================
-- FORMACIÓN
-- ============================================================
INSERT INTO formacion (id_formacion, id_empleado, curso, tipo, fecha_inicio, fecha_fin, duracion_horas, certificado, fecha_caducidad, institucion) VALUES
-- Carnets manipulador de alimentos
(1,  1,  'Manipulador de Alimentos — Nivel Superior',        'manipulador_alimentos', '2022-05-15', '2022-06-01',  12, TRUE, '2026-06-01', 'Cámara de Comercio Madrid'),
(2,  2,  'Manipulador de Alimentos — Nivel Superior',        'manipulador_alimentos', '2022-05-15', '2022-06-01',  12, TRUE, '2026-06-01', 'Cámara de Comercio Madrid'),
(3,  3,  'Manipulador de Alimentos',                         'manipulador_alimentos', '2023-01-10', '2023-01-15',   8, TRUE, '2025-03-15', 'ACOSAM'),  -- próximo a caducar
(4,  4,  'Manipulador de Alimentos',                         'manipulador_alimentos', '2023-09-01', '2023-09-05',   8, TRUE, '2025-09-05', 'ACOSAM'),
-- Alergias e intolerancias
(5,  1,  'Gestión de Alérgenos en Cocina Profesional',       'alergia_intolerancia',  '2023-03-01', '2023-03-02',  16, TRUE, '2027-03-02', 'Escuela de Hostelería de Madrid'),
(6,  5,  'Alérgenos — Normativa UE 1169/2011',              'alergia_intolerancia',  '2023-04-10', '2023-04-11',   8, TRUE, '2027-04-11', 'Fundación Alícia'),
(7,  6,  'Alérgenos para Personal de Sala',                  'alergia_intolerancia',  '2023-04-10', '2023-04-10',   4, TRUE, '2027-04-10', 'Fundación Alícia'),
-- Vinos y sumillería
(8,  10, 'WSET Level 2 — Wines',                            'vinos',                 '2022-09-01', '2022-11-30', 120, TRUE, '2030-11-30', 'Wine & Spirit Education Trust'),
(9,  10, 'WSET Level 3 — Wines',                            'vinos',                 '2023-09-01', '2024-02-28', 240, TRUE, '2030-02-28', 'Wine & Spirit Education Trust'),
(10, 12, 'Introducción a la Sumillería',                     'vinos',                 '2023-05-01', '2023-06-30',  40, TRUE, '2028-06-30', 'Escuela Luis Irizar'),
(11, 6,  'Fundamentos de Maridaje',                          'vinos',                 '2024-01-15', '2024-01-16',  12, TRUE, '2028-01-16', 'Escuela Luis Irizar'),
-- Idiomas
(12, 13, 'Inglés para Hostelería — B2',                     'idiomas',               '2023-01-09', '2023-06-30', 120, TRUE, NULL,         'EOI Madrid'),
(13, 14, 'Inglés para Hostelería — C1',                     'idiomas',               '2022-09-01', '2023-06-15', 180, TRUE, NULL,         'EOI Madrid'),
(14, 15, 'Inglés Conversacional',                            'idiomas',               '2024-02-01', '2024-05-31',  60, FALSE,NULL,         'British Council'),
-- Primeros auxilios
(15, 18, 'Primeros Auxilios y RCP',                          'primeros_auxilios',     '2023-06-01', '2023-06-01',   8, TRUE, '2025-06-01', 'Cruz Roja Española'),  -- próximo a caducar
(16, 5,  'Primeros Auxilios Básicos',                        'primeros_auxilios',     '2024-01-20', '2024-01-20',   6, TRUE, '2026-01-20', 'Cruz Roja Española'),
-- Prevención de riesgos
(17, 1,  'PRL — Sector Hostelería (20h)',                   'prevencion_riesgos',    '2022-10-01', '2022-10-15',  20, TRUE, NULL,         'Fremap'),
(18, 19, 'PRL — Nivel Básico 60h',                          'prevencion_riesgos',    '2021-03-01', '2021-04-30',  60, TRUE, NULL,         'Fundación Laboral de la Construcción'),
-- Otros
(19, 10, 'Coctelería Clásica y de Autor',                   'otros',                 '2023-11-01', '2023-11-30',  40, TRUE, NULL,         'Escuela de Barmans Profesionales'),
(20, 2,  'Técnicas Avanzadas de Pastelería',                'otros',                 '2024-04-01', '2024-04-05',  30, TRUE, NULL,         'Escuela de Hostelería de Madrid');

-- ============================================================
-- AUSENCIAS
-- ============================================================
INSERT INTO ausencias (id_ausencia, id_empleado, tipo, fecha_inicio, fecha_fin, dias_totales, estado, justificante, observaciones) VALUES
-- Vacaciones programadas
(1,  1,  'vacaciones',         '2025-08-01', '2025-08-22', 22, 'aprobado',   NULL, 'Vacaciones de verano acordadas en convenio'),
(2,  2,  'vacaciones',         '2025-07-14', '2025-08-01', 19, 'aprobado',   NULL, 'Vacaciones de verano'),
(3,  5,  'vacaciones',         '2025-09-01', '2025-09-15', 15, 'aprobado',   NULL, NULL),
(4,  6,  'vacaciones',         '2025-08-01', '2025-08-15', 15, 'solicitado', NULL, 'Pendiente de confirmación por carga de trabajo'),
(5,  13, 'vacaciones',         '2025-06-16', '2025-06-30', 15, 'solicitado', NULL, NULL),
-- Bajas médicas
(6,  8,  'baja_medica',        '2025-01-08', '2025-01-15',  8, 'aprobado',   'parte_baja_8ene.pdf', 'Gripe con complicaciones respiratorias'),
(7,  4,  'baja_medica',        '2025-02-26', '2025-02-28',  3, 'aprobado',   'parte_baja_26feb.pdf','Gastroenteritis'),
(8,  16, 'baja_medica',        '2025-01-20', '2025-02-07', 19, 'aprobado',   'it_lumbar.pdf',       'Baja por lumbalgia'),
-- Permisos personales
(9,  7,  'permiso_personal',   '2025-03-05', '2025-03-05',  1, 'aprobado',   NULL, 'Gestión administrativa'),
(10, 14, 'permiso_personal',   '2025-03-10', '2025-03-10',  1, 'solicitado', NULL, 'Examen oficial de inglés'),
(11, 3,  'permiso_personal',   '2025-04-17', '2025-04-18',  2, 'solicitado', NULL, 'Viaje familiar programado'),
-- Asuntos propios
(12, 10, 'asuntos_propios',    '2025-02-14', '2025-02-14',  1, 'aprobado',   NULL, NULL),
(13, 11, 'asuntos_propios',    '2025-03-20', '2025-03-20',  1, 'solicitado', NULL, NULL),
-- Maternidad/Paternidad
(14, 2,  'maternidad_paternidad','2024-09-01','2024-12-24', 84, 'aprobado',  'baja_maternidad.pdf', '16 semanas de baja por maternidad'),
-- Enfermedad leve
(15, 9,  'enfermedad',         '2025-01-27', '2025-01-28',  2, 'aprobado',   NULL, 'Fiebre alta, notificado por WhatsApp'),
(16, 15, 'enfermedad',         '2025-02-03', '2025-02-03',  1, 'aprobado',   NULL, NULL),
-- Solicitud rechazada
(17, 4,  'vacaciones',         '2025-12-22', '2026-01-02', 12, 'rechazado',  NULL, 'No se puede cubrir en temporada alta navideña');

-- ============================================================
-- MENSAJES DE CHAT
-- ============================================================
INSERT INTO mensajes_chat (id_mensaje, id_emisor, id_receptor, contenido, fecha_envio, leido, fecha_lectura) VALUES
-- Conversación Carlos (1) ↔ Roberto (5) sobre cobertura de turno
(1,  1, 5,  'Buenos días Roberto, ¿tenemos cubiertos todos los turnos del viernes para el evento privado?',
            '2025-02-27 09:05:00', TRUE,  '2025-02-27 09:12:00'),
(2,  5, 1,  'Hola Carlos, sí. He confirmado a Javier y Elena para la noche. Solo me falta saber cuántos cubiertos son exactamente.',
            '2025-02-27 09:13:00', TRUE,  '2025-02-27 09:20:00'),
(3,  1, 5,  'Serán 45 cubiertos. Menú cerrado de 5 pasos. Te mando la ficha técnica ahora.',
            '2025-02-27 09:21:00', TRUE,  '2025-02-27 09:25:00'),
(4,  5, 1,  'Perfecto, muchas gracias. Lo comunico al equipo de sala.',
            '2025-02-27 09:26:00', TRUE,  '2025-02-27 09:30:00'),
-- Conversación Roberto (5) ↔ Ana (6) sobre turno viernes
(5,  5, 6,  'Ana, el viernes hay evento privado a partir de las 19h. ¿Puedes quedarte hasta el cierre?',
            '2025-02-27 10:00:00', TRUE,  '2025-02-27 10:15:00'),
(6,  6, 5,  'Sin problema. ¿Necesitaremos uniforme de gala o el estándar?',
            '2025-02-27 10:16:00', TRUE,  '2025-02-27 10:20:00'),
(7,  5, 6,  'Uniforme de gala, sí. Es una boda.',
            '2025-02-27 10:21:00', TRUE,  '2025-02-27 10:30:00'),
(8,  6, 5,  'Entendido, lo digo también a los compañeros de sala. ¡Nos vemos el viernes!',
            '2025-02-27 10:31:00', TRUE,  '2025-02-27 10:35:00'),
-- Conversación Miguel (19) ↔ Carlos (1) sobre nóminas
(9,  19, 1, 'Carlos, las nóminas de febrero se pagarán el 5 de marzo. Recuérdalo al equipo de cocina.',
            '2025-02-28 08:00:00', TRUE,  '2025-02-28 08:30:00'),
(10, 1, 19, 'Perfecto, gracias Miguel. ¿Incluyen ya las horas extra del viernes?',
            '2025-02-28 08:32:00', TRUE,  '2025-02-28 08:45:00'),
(11, 19, 1, 'Sí, todas las horas extra de febrero están incluidas.',
            '2025-02-28 08:46:00', TRUE,  '2025-02-28 09:00:00'),
-- Conversación Natalia (10) ↔ Iván (11) — coordinación barra viernes noche
(12, 10, 11,'Iván, mañana noche viene el grupo de empresa de siempre. Prepara el cóctel de bienvenida con Aperol.',
            '2025-02-27 17:00:00', TRUE,  '2025-02-27 17:30:00'),
(13, 11, 10,'Claro, ¿cuántas personas son? Para calcular el Aperol que necesito.',
            '2025-02-27 17:31:00', TRUE,  '2025-02-27 17:45:00'),
(14, 10, 11,'Unas 30 personas. Pide a almacén 4 botellas por si acaso.',
            '2025-02-27 17:46:00', TRUE,  '2025-02-27 18:00:00'),
(15, 11, 10,'Hecho. ¿Necesitas que llegue antes para preparar la mise en place?',
            '2025-02-27 18:01:00', TRUE,  '2025-02-27 18:10:00'),
(16, 10, 11,'Sí, si puedes llegar a las 19h en vez de las 20h sería perfecto.',
            '2025-02-27 18:11:00', TRUE,  '2025-02-27 18:20:00'),
-- Mensajes sin leer (para probar notificaciones)
(17, 5,  7, 'Javier, mañana al entrar habla con Roberto sobre el protocolo para el servicio del vino.',
            '2025-02-28 22:00:00', FALSE, NULL),
(18, 1,  3, 'Mohamed, necesito que vengas 30 minutos antes el lunes para el briefing con el nuevo proveedor.',
            '2025-02-28 22:30:00', FALSE, NULL),
(19, 19, 5, 'Roberto, recuerda enviar el parte de horas extra antes del lunes a primera hora.',
            '2025-02-28 23:00:00', FALSE, NULL);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- USUARIOS
-- Las contraseñas deben generarse con PBKDF2 (PasswordUtil).
-- Tras cargar este script, ejecutar la clase GenerarHashPassword
-- y luego actualizar cada usuario con el hash generado, por ejemplo:
--   UPDATE usuarios SET password_hash = '...' WHERE username = 'admin';
-- ============================================================
INSERT INTO usuarios (id_usuario, nombre, username, email, password_hash, acceso_pc, rol, activo, fecha_creacion, id_empleado) VALUES
(1, 'Administrador',      'admin',    'admin@hosteleria.com',           '$2a$12$adminHashEjemplo000000000000000000000000000000000000000', 1, 'ADMIN',    1, NOW(), NULL),
(2, 'Carlos Martínez',    'carlos',   'carlos.martinez@hosteleria.com', '$2a$12$carlosHashEjemplo00000000000000000000000000000000000000', 1, 'GERENTE',  1, NOW(), 1),
(3, 'Roberto Sánchez',    'roberto',  'roberto.sanchez@hosteleria.com', '$2a$12$robertoHashEjemplo0000000000000000000000000000000000000', 1, 'GERENTE',  1, NOW(), 5),
(4, 'Miguel Blanco',      'miguel',   'miguel.blanco@hosteleria.com',   '$2a$12$miguelHashEjemplo000000000000000000000000000000000000000', 1, 'ADMIN',    1, NOW(), 19),
(5, 'Ana Torres',         'ana',      'ana.torres@hosteleria.com',      '$2a$12$anaHashEjemplo000000000000000000000000000000000000000000', 0, 'EMPLEADO', 1, NOW(), 6),
(6, 'Lucía Fernández',    'lucia',    'lucia.fernandez@hosteleria.com', '$2a$12$luciaHashEjemplo0000000000000000000000000000000000000000', 0, 'EMPLEADO', 1, NOW(), 2),
(7, 'David Ruiz',         'david',    'david.ruiz@hosteleria.com',      '$2a$12$davidHashEjemplo0000000000000000000000000000000000000000', 0, 'EMPLEADO', 1, NOW(), 13),
(8, 'Natalia Gutiérrez',  'natalia',  'natalia.gutierrez@hosteleria.com','$2a$12$nataliaHashEjemplo000000000000000000000000000000000000000',0, 'EMPLEADO', 1, NOW(), 10),
(9, 'Usuario Desactivado','inactivo', 'inactivo@hosteleria.com',        '$2a$12$inactivoHashEjemplo00000000000000000000000000000000000000',1, 'EMPLEADO', 0, NOW(), NULL);
