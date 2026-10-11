-- ============================================================
-- DATOS DE PRESENTACIÓN - SigmaWeb
-- Tenant: Empresa1 (id=1)
-- ============================================================
-- CONTRASEÑAS:
--   mant     -> mant123
--   prod     -> prod123
--   tec1..5  -> tec123
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 0. ACTIVOS DE PLANTA (29 ACTIVOS)
-- ============================================================
INSERT INTO activo (id, tenant_id, nombre, nombre_camel_case, codigo, descripcion, estado, promedio_movil) VALUES
(1, 1, 'Robot flexibles', 'robotFlexibles', 'ACT-001', 'Robot de envasado flexible y paletizado', 'operativa', '1 mes'),
(2, 1, 'Dársena de líquidos', 'darsenaDeLiquidos', 'ACT-002', 'Dársena de recepción y bombeo de líquidos', 'operativa', '1 mes'),
(3, 1, 'Elaboración HC', 'elaboracionHc', 'ACT-003', 'Línea de elaboración de productos Home Care', 'operativa', '1 mes'),
(4, 1, 'Elaboración pelo', 'elaboracionPelo', 'ACT-004', 'Línea de elaboración de cosmética capilar', 'operativa', '1 mes'),
(5, 1, 'GLP', 'glp', 'ACT-005', 'Planta y distribución de Gas Licuado de Petróleo', 'operativa', '1 mes'),
(6, 1, 'Aero 9', 'aero9', 'ACT-006', 'Línea de llenado de aerosoles 9', 'operativa', '1 mes'),
(7, 1, 'Aero 8', 'aero8', 'ACT-007', 'Línea de llenado de aerosoles 8', 'operativa', '1 mes'),
(8, 1, 'Aero 6', 'aero6', 'ACT-008', 'Línea de llenado de aerosoles 6', 'operativa', '1 mes'),
(9, 1, 'Aero 5', 'aero5', 'ACT-009', 'Línea de llenado de aerosoles 5', 'operativa', '1 mes'),
(10, 1, 'Aero 4', 'aero4', 'ACT-010', 'Línea de llenado de aerosoles 4', 'operativa', '1 mes'),
(11, 1, 'PC8', 'pc8', 'ACT-011', 'Línea Personal Care 8', 'operativa', '1 mes'),
(12, 1, 'Planta piloto', 'plantaPiloto', 'ACT-012', 'Planta piloto de desarrollo y pruebas', 'operativa', '1 mes'),
(13, 1, 'HC12', 'hc12', 'ACT-013', 'Reactor de elaboración HC12', 'operativa', '1 mes'),
(14, 1, 'HC11', 'hc11', 'ACT-014', 'Reactor de elaboración HC11', 'operativa', '1 mes'),
(15, 1, 'HC10', 'hc10', 'ACT-015', 'Reactor de elaboración HC10', 'operativa', '1 mes'),
(16, 1, 'HC9', 'hc9', 'ACT-016', 'Reactor de elaboración HC9', 'operativa', '1 mes'),
(17, 1, 'HC8', 'hc8', 'ACT-017', 'Reactor de elaboración HC8', 'operativa', '1 mes'),
(18, 1, 'HC7', 'hc7', 'ACT-018', 'Reactor de elaboración HC7', 'operativa', '1 mes'),
(19, 1, 'PC6', 'pc6', 'ACT-019', 'Línea Personal Care 6', 'operativa', '1 mes'),
(20, 1, 'PC3', 'pc3', 'ACT-020', 'Línea Personal Care 3', 'operativa', '1 mes'),
(21, 1, 'Robot pelo', 'robotPelo', 'ACT-021', 'Robot de empaque productos de pelo', 'operativa', '1 mes'),
(22, 1, 'Robot botella', 'robotBotella', 'ACT-022', 'Robot de soplado y posicionador de botellas', 'operativa', '1 mes'),
(23, 1, 'PC2', 'pc2', 'ACT-023', 'Línea Personal Care 2', 'operativa', '1 mes'),
(24, 1, 'PC1', 'pc1', 'ACT-024', 'Línea Personal Care 1', 'operativa', '1 mes'),
(25, 1, 'HC5', 'hc5', 'ACT-025', 'Reactor de elaboración HC5', 'operativa', '1 mes'),
(26, 1, 'HC4', 'hc4', 'ACT-026', 'Reactor de elaboración HC4', 'operativa', '1 mes'),
(27, 1, 'HC3', 'hc3', 'ACT-027', 'Reactor de elaboración HC3', 'operativa', '1 mes'),
(28, 1, 'HC2', 'hc2', 'ACT-028', 'Reactor de elaboración HC2', 'operativa', '1 mes'),
(29, 1, 'HC1', 'hc1', 'ACT-029', 'Reactor de elaboración HC1', 'operativa', '1 mes')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), nombre_camel_case=VALUES(nombre_camel_case);

-- ============================================================
-- 1. USUARIOS
-- ============================================================

-- Usuario Mantenimiento
INSERT INTO usuario (username, password, password_claro, estado, tenant_id)
VALUES ('mant', '$2a$10$Om/u2RTyrHpFO4U.Zdrp0umZ3rynGOL/dE.vQ0K5MQpo4yNGZQCQe', 'mant123', 'activo', 1);

SET @id_mant = LAST_INSERT_ID();

INSERT INTO rol (nombre, id_usuario, tenant_id) VALUES ('ROLE_ADMIN', @id_mant, 1);
INSERT INTO rol (nombre, id_usuario, tenant_id) VALUES ('ROLE_MANT',  @id_mant, 1);

-- Usuario Produccion
INSERT INTO usuario (username, password, password_claro, estado, tenant_id)
VALUES ('prod', '$2a$10$Ul/FCvI4gLb1jbrn5DjcyuA4pTj6ZZLKHN77hfXOEYHtiti/yXtgy', 'prod123', 'activo', 1);

SET @id_prod = LAST_INSERT_ID();

INSERT INTO rol (nombre, id_usuario, tenant_id) VALUES ('ROLE_PROD', @id_prod, 1);

-- ============================================================
-- 2. USUARIOS TECNICOS (5)
-- ============================================================

INSERT INTO usuario (username, password, password_claro, estado, tenant_id)
VALUES ('tec1', '$2a$10$AcX4JwvSsWAchVBxLbHAOOxOrDWQ8MY2GD28tXG/SDq2vR5bux8im', 'tec123', 'activo', 1);
SET @uid_tec1 = LAST_INSERT_ID();
INSERT INTO rol (nombre, id_usuario, tenant_id) VALUES ('ROLE_TECNICO', @uid_tec1, 1);

INSERT INTO usuario (username, password, password_claro, estado, tenant_id)
VALUES ('tec2', '$2a$10$AcX4JwvSsWAchVBxLbHAOOxOrDWQ8MY2GD28tXG/SDq2vR5bux8im', 'tec123', 'activo', 1);
SET @uid_tec2 = LAST_INSERT_ID();
INSERT INTO rol (nombre, id_usuario, tenant_id) VALUES ('ROLE_TECNICO', @uid_tec2, 1);

INSERT INTO usuario (username, password, password_claro, estado, tenant_id)
VALUES ('tec3', '$2a$10$AcX4JwvSsWAchVBxLbHAOOxOrDWQ8MY2GD28tXG/SDq2vR5bux8im', 'tec123', 'activo', 1);
SET @uid_tec3 = LAST_INSERT_ID();
INSERT INTO rol (nombre, id_usuario, tenant_id) VALUES ('ROLE_TECNICO', @uid_tec3, 1);

INSERT INTO usuario (username, password, password_claro, estado, tenant_id)
VALUES ('tec4', '$2a$10$AcX4JwvSsWAchVBxLbHAOOxOrDWQ8MY2GD28tXG/SDq2vR5bux8im', 'tec123', 'activo', 1);
SET @uid_tec4 = LAST_INSERT_ID();
INSERT INTO rol (nombre, id_usuario, tenant_id) VALUES ('ROLE_TECNICO', @uid_tec4, 1);

INSERT INTO usuario (username, password, password_claro, estado, tenant_id)
VALUES ('tec5', '$2a$10$AcX4JwvSsWAchVBxLbHAOOxOrDWQ8MY2GD28tXG/SDq2vR5bux8im', 'tec123', 'activo', 1);
SET @uid_tec5 = LAST_INSERT_ID();
INSERT INTO rol (nombre, id_usuario, tenant_id) VALUES ('ROLE_TECNICO', @uid_tec5, 1);

-- ============================================================
-- 3. TECNICOS (datos personales y profesionales)
-- ============================================================

INSERT INTO tecnico (
    usuario, tenant_id, nombre, apellido, fecha_nacimiento, telefono, email, direccion,
    formacion_academica, conocimientos_varios, experiencia_laboral, pasatiempos,
    acerca_de_mi, estado, legajo, especialidad, fecha_ingreso_planta,
    habilidades, proyectos_destacados, notas_adicionales,
    satisfaccion_cliente_interno, predisposicion_para_la_tarea, responsabilidad,
    cumplimiento_normas_seguridad, nivel_de_conocimiento, trato_recibido_por_cliente,
    prolijidad, puntualidad, eficiencia, calidad_del_trabajo, comunicacion,
    trabajo_en_equipo, resolucion_de_problemas, creatividadeinnovacion,
    iniciativa, autogestion, formacion_continua,
    cantidad_preventivos, cantidad_informes, ultima_actualizacion
) VALUES (
    @uid_tec1, 1, 'Carlos', 'Mendoza', '1985-03-12', '351-4451122', 'cmendoza@empresa.com', 'Av. Colon 1234, Cordoba',
    'Tecnico Electromecanico IPEM 251. Curso PLC Siemens S7-300. Curso Neumatica Industrial.',
    'PLC Siemens, Allen Bradley. Variadores ABB. Lectura de planos electricos.',
    '2008-2015: Tecnico en Planta Automotriz Cordoba. 2015-presente: Empresa1.',
    'Futbol, fotografia y trekking.',
    'Tecnico electromecanico con mas de 15 anos de experiencia en mantenimiento industrial.',
    'activo', 'LEG-001', 'Electromecanica', '2015-06-01',
    'PLC, variadores, instrumentacion, soldadura MIG',
    'Automatizacion linea de envasado 2022. Reduccion de paradas no planificadas 18%.',
    'Disponible para guardia nocturna.',
    '9', '9', '10', '10', '9', '8', '9', '10', '9', '9', '8', '9', '9', '7', '8', '9', '8',
    '45', '12', NOW()
);
SET @id_tec1 = LAST_INSERT_ID();

INSERT INTO tecnico (
    usuario, tenant_id, nombre, apellido, fecha_nacimiento, telefono, email, direccion,
    formacion_academica, conocimientos_varios, experiencia_laboral, pasatiempos,
    acerca_de_mi, estado, legajo, especialidad, fecha_ingreso_planta,
    habilidades, proyectos_destacados, notas_adicionales,
    satisfaccion_cliente_interno, predisposicion_para_la_tarea, responsabilidad,
    cumplimiento_normas_seguridad, nivel_de_conocimiento, trato_recibido_por_cliente,
    prolijidad, puntualidad, eficiencia, calidad_del_trabajo, comunicacion,
    trabajo_en_equipo, resolucion_de_problemas, creatividadeinnovacion,
    iniciativa, autogestion, formacion_continua,
    cantidad_preventivos, cantidad_informes, ultima_actualizacion
) VALUES (
    @uid_tec2, 1, 'Lucas', 'Ferreyra', '1990-07-22', '351-5563344', 'lferreyra@empresa.com', 'Bv. San Juan 890, Cordoba',
    'Tecnico en Electronica EPET 2. Diplomatura en Robotica Industrial (UTN).',
    'Programacion robots KUKA e IRB ABB. Mantenimiento de servos. Diagnostico por osciloscopio.',
    '2013-2018: Mantenimiento en plasticos inyectados. 2018-presente: Empresa1, area robots.',
    'Ajedrez, programacion Arduino, ciclismo.',
    'Especialista en robotica industrial con certificacion KUKA. Apasionado por la automatizacion.',
    'activo', 'LEG-002', 'Robotica', '2018-03-15',
    'KUKA KRC4, ABB IRC5, electronica analogica y digital, vision artificial basica',
    'Puesta en marcha Robot flexibles 2019. Integracion vision artificial 2023.',
    'Candidato para jefe de turno.',
    '10', '10', '9', '10', '10', '9', '9', '9', '10', '10', '9', '10', '10', '9', '10', '9', '10',
    '38', '10', NOW()
);
SET @id_tec2 = LAST_INSERT_ID();

INSERT INTO tecnico (
    usuario, tenant_id, nombre, apellido, fecha_nacimiento, telefono, email, direccion,
    formacion_academica, conocimientos_varios, experiencia_laboral, pasatiempos,
    acerca_de_mi, estado, legajo, especialidad, fecha_ingreso_planta,
    habilidades, proyectos_destacados, notas_adicionales,
    satisfaccion_cliente_interno, predisposicion_para_la_tarea, responsabilidad,
    cumplimiento_normas_seguridad, nivel_de_conocimiento, trato_recibido_por_cliente,
    prolijidad, puntualidad, eficiencia, calidad_del_trabajo, comunicacion,
    trabajo_en_equipo, resolucion_de_problemas, creatividadeinnovacion,
    iniciativa, autogestion, formacion_continua,
    cantidad_preventivos, cantidad_informes, ultima_actualizacion
) VALUES (
    @uid_tec3, 1, 'Maria', 'Gonzalez', '1988-11-05', '351-4427788', 'mgonzalez@empresa.com', 'Calle Rioja 456, Cordoba',
    'Tecnica Quimica Industrial IPET 70. Curso Analisis de Aceites. Seguridad e Higiene Industrial.',
    'Analisis de fallas hidraulicas y neumaticas. Lubricacion industrial. EPP.',
    '2010-2017: Laboratorio control de calidad planta quimica. 2017-presente: Empresa1.',
    'Jardin, cocina, yoga.',
    'Tecnica con foco en mantenimiento preventivo y analisis de aceites.',
    'activo', 'LEG-003', 'Hidraulica y Neumatica', '2017-09-01',
    'Hidraulica, neumatica, lubricacion, analisis de vibraciones basico',
    'Implementacion plan de lubricacion 2020. Reduccion consumo de aceite 22%.',
    'Referente en temas de seguridad del area.',
    '9', '9', '10', '10', '8', '10', '10', '10', '9', '10', '10', '10', '8', '8', '9', '9', '9',
    '52', '8', NOW()
);
SET @id_tec3 = LAST_INSERT_ID();

INSERT INTO tecnico (
    usuario, tenant_id, nombre, apellido, fecha_nacimiento, telefono, email, direccion,
    formacion_academica, conocimientos_varios, experiencia_laboral, pasatiempos,
    acerca_de_mi, estado, legajo, especialidad, fecha_ingreso_planta,
    habilidades, proyectos_destacados, notas_adicionales,
    satisfaccion_cliente_interno, predisposicion_para_la_tarea, responsabilidad,
    cumplimiento_normas_seguridad, nivel_de_conocimiento, trato_recibido_por_cliente,
    prolijidad, puntualidad, eficiencia, calidad_del_trabajo, comunicacion,
    trabajo_en_equipo, resolucion_de_problemas, creatividadeinnovacion,
    iniciativa, autogestion, formacion_continua,
    cantidad_preventivos, cantidad_informes, ultima_actualizacion
) VALUES (
    @uid_tec4, 1, 'Rodrigo', 'Sanchez', '1992-04-18', '351-4489900', 'rsanchez@empresa.com', 'Av. Chacabuco 321, Cordoba',
    'Tecnico Mecanico IPEM 38. Curso de Metrologia y Ajuste. Soldadura TIG.',
    'Torno, fresadora, rectificadora. Soldadura TIG y MIG. Medicion con calibres y micrometros.',
    '2015-2020: Mecanica de precision en taller metalurgico. 2020-presente: Empresa1.',
    'Rugby, lectura tecnica, modelismo.',
    'Mecanico de precision con experiencia en mecanizado y ajuste.',
    'activo', 'LEG-004', 'Mecanica de Precision', '2020-02-01',
    'Mecanizado, soldadura TIG/MIG, analisis de fallas mecanicas',
    'Recuperacion arbol de transmision linea HC 2021. Reduccion tiempo de reparacion 30%.',
    'En proceso de certificacion ISO 9001.',
    '8', '9', '9', '10', '8', '9', '9', '8', '8', '9', '8', '8', '9', '8', '8', '8', '8',
    '28', '7', NOW()
);
SET @id_tec4 = LAST_INSERT_ID();

INSERT INTO tecnico (
    usuario, tenant_id, nombre, apellido, fecha_nacimiento, telefono, email, direccion,
    formacion_academica, conocimientos_varios, experiencia_laboral, pasatiempos,
    acerca_de_mi, estado, legajo, especialidad, fecha_ingreso_planta,
    habilidades, proyectos_destacados, notas_adicionales,
    satisfaccion_cliente_interno, predisposicion_para_la_tarea, responsabilidad,
    cumplimiento_normas_seguridad, nivel_de_conocimiento, trato_recibido_por_cliente,
    prolijidad, puntualidad, eficiencia, calidad_del_trabajo, comunicacion,
    trabajo_en_equipo, resolucion_de_problemas, creatividadeinnovacion,
    iniciativa, autogestion, formacion_continua,
    cantidad_preventivos, cantidad_informes, ultima_actualizacion
) VALUES (
    @uid_tec5, 1, 'Valentina', 'Torres', '1995-09-30', '351-4412233', 'vtorres@empresa.com', 'Calle Belgrano 789, Cordoba',
    'Ing. Electronica (en curso, 4 anio) UTN Cordoba. Cursos de SCADA, WinCC y redes industriales.',
    'SCADA WinCC, redes Profibus y Profinet, instrumentacion y sensores industriales.',
    '2022-presente: Empresa1, primer trabajo. Practicas profesionales en fabrica de motores.',
    'Musica, programacion Python, senderismo.',
    'Tecnica junior con gran capacidad de aprendizaje. Especialista emergente en redes industriales y SCADA.',
    'activo', 'LEG-005', 'Instrumentacion y SCADA', '2022-07-01',
    'SCADA, instrumentacion, redes industriales, Python',
    'Configuracion HMI sala de control 2023. Integracion sensores IoT proyecto piloto.',
    'Gran potencial de crecimiento. Recomendada para capacitacion avanzada.',
    '9', '10', '9', '9', '9', '10', '9', '10', '9', '9', '10', '10', '9', '10', '10', '9', '10',
    '18', '6', NOW()
);
SET @id_tec5 = LAST_INSERT_ID();

-- ============================================================
-- 4. TAREAS (DETENCIONES) - 10 por activo (activos 1..8)
-- ============================================================

-- Activo 1: Robot flexibles
INSERT INTO tareas (tenant_id, activo, descripcion, categoria_tecnica, solicita, estado, afecta_produccion, departamento_responsable, momento_detencion, momento_asignacion, momento_liberacion, momento_cierre) VALUES
(1, 1, 'Falla en servo motor eje 4. El robot detiene ciclo con alarma E1045.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-01-10 08:30:00', '2026-01-10 09:00:00', '2026-01-10 11:30:00', '2026-01-10 12:00:00'),
(1, 1, 'Perdida de presion en pinza neumatica. No agarra pieza correctamente.', 'Neumatica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-01-22 14:00:00', '2026-01-22 14:30:00', '2026-01-22 16:00:00', '2026-01-22 16:30:00'),
(1, 1, 'Alarma de sobretemperatura en controlador KUKA. Parada de seguridad.', 'Electrica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-02-05 10:15:00', '2026-02-05 10:45:00', '2026-02-05 13:00:00', '2026-02-05 13:30:00'),
(1, 1, 'Rotura de sensor de posicion eje 1. Robot fuera de ciclo.', 'Electronica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-02-18 07:50:00', '2026-02-18 08:20:00', '2026-02-18 10:00:00', '2026-02-18 10:30:00'),
(1, 1, 'Cable de encoder danado por rozamiento. Perdida de posicion.', 'Electrica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-03-03 15:20:00', '2026-03-03 15:50:00', '2026-03-03 17:30:00', '2026-03-03 18:00:00'),
(1, 1, 'Falla en comunicacion EtherCAT con drive eje 6.', 'Electronica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-03-20 09:40:00', '2026-03-20 10:10:00', '2026-03-20 12:30:00', '2026-03-20 13:00:00'),
(1, 1, 'Desgaste excesivo en reduccion eje 2. Juego mecanico fuera de tolerancia.', 'Mecanica', 'mant', 'cerrada', 'no', 'Mantenimiento', '2026-04-08 11:00:00', '2026-04-08 11:30:00', '2026-04-09 16:00:00', '2026-04-09 16:30:00'),
(1, 1, 'Perdida de vacio en ventosa de agarre. Pieza cae durante transferencia.', 'Neumatica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-04-25 13:30:00', '2026-04-25 14:00:00', '2026-04-25 15:30:00', '2026-04-25 16:00:00'),
(1, 1, 'Error de calibracion post-mantenimiento. Robot golpea posicion de carga.', 'Electronica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-05-12 08:00:00', '2026-05-12 08:30:00', '2026-05-12 10:00:00', '2026-05-12 10:30:00'),
(1, 1, 'Interruptor de seguridad activado sin causa aparente. Revision de cadena.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-06-01 16:45:00', '2026-06-01 17:00:00', '2026-06-01 18:30:00', '2026-06-01 19:00:00');

-- Activo 2: Darsena de liquidos
INSERT INTO tareas (tenant_id, activo, descripcion, categoria_tecnica, solicita, estado, afecta_produccion, departamento_responsable, momento_detencion, momento_asignacion, momento_liberacion, momento_cierre) VALUES
(1, 2, 'Valvula de control de flujo atascada en posicion cerrada. Sin paso de producto.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-01-15 07:30:00', '2026-01-15 08:00:00', '2026-01-15 10:00:00', '2026-01-15 10:30:00'),
(1, 2, 'Sensor de nivel ultrasonico fuera de rango. Lectura erronea en tanque 3.', 'Electronica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-01-28 11:00:00', '2026-01-28 11:30:00', '2026-01-28 13:00:00', '2026-01-28 13:30:00'),
(1, 2, 'Bomba centrifuga #2 con ruido inusual. Cavitacion detectada.', 'Mecanica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-02-10 09:15:00', '2026-02-10 09:45:00', '2026-02-10 14:00:00', '2026-02-10 14:30:00'),
(1, 2, 'Perdida en junta de brida linea 5. Derrame de producto.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-02-24 14:20:00', '2026-02-24 14:50:00', '2026-02-24 17:00:00', '2026-02-24 17:30:00'),
(1, 2, 'Variador de frecuencia bomba #1 en falla F005 (sobrecorriente).', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-03-08 08:50:00', '2026-03-08 09:20:00', '2026-03-08 11:30:00', '2026-03-08 12:00:00'),
(1, 2, 'Actuador neumatico de valvula esclusa sin retorno. Valvula bloqueada abierta.', 'Neumatica', 'mant', 'cerrada', 'no', 'Mantenimiento', '2026-03-25 15:30:00', '2026-03-25 16:00:00', '2026-03-25 17:30:00', '2026-03-25 18:00:00'),
(1, 2, 'Motor bomba #3 con temperatura elevada. Proteccion termica activa.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-04-12 10:00:00', '2026-04-12 10:30:00', '2026-04-12 13:00:00', '2026-04-12 13:30:00'),
(1, 2, 'Filtro de linea obstruido. Caida de presion excesiva aguas abajo.', 'Hidraulica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-04-29 12:00:00', '2026-04-29 12:30:00', '2026-04-29 14:00:00', '2026-04-29 14:30:00'),
(1, 2, 'Sensor de presion diferencial mal calibrado. Alarma falsa de nivel.', 'Electronica', 'mant', 'cerrada', 'no', 'Mantenimiento', '2026-05-16 09:30:00', '2026-05-16 10:00:00', '2026-05-16 11:30:00', '2026-05-16 12:00:00'),
(1, 2, 'Fuga en sello mecanico bomba #2 tras 8000hs de operacion.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-06-05 07:00:00', '2026-06-05 07:30:00', '2026-06-05 12:00:00', '2026-06-05 12:30:00');

-- Activo 3: Elaboracion HC
INSERT INTO tareas (tenant_id, activo, descripcion, categoria_tecnica, solicita, estado, afecta_produccion, departamento_responsable, momento_detencion, momento_asignacion, momento_liberacion, momento_cierre) VALUES
(1, 3, 'Rodamiento del mezclador con vibracion anormal. Falla inminente detectada.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-01-12 06:30:00', '2026-01-12 07:00:00', '2026-01-12 12:00:00', '2026-01-12 12:30:00'),
(1, 3, 'Resistencia de calefaccion del reactor 1 sin continuidad. Sin temperatura.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-01-26 08:00:00', '2026-01-26 08:30:00', '2026-01-26 11:00:00', '2026-01-26 11:30:00'),
(1, 3, 'Controlador de temperatura en falla. Desviacion de 15C del setpoint.', 'Electronica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-02-08 14:30:00', '2026-02-08 15:00:00', '2026-02-08 17:00:00', '2026-02-08 17:30:00'),
(1, 3, 'Variador de mezclador HC con alarma de comunicacion Profibus.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-02-22 10:00:00', '2026-02-22 10:30:00', '2026-02-22 13:00:00', '2026-02-22 13:30:00'),
(1, 3, 'Valvula de seguridad disparada por sobrepresion. Revision de proceso.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-03-06 09:15:00', '2026-03-06 09:45:00', '2026-03-06 14:00:00', '2026-03-06 14:30:00'),
(1, 3, 'Fuga en intercambiador de calor. Mezcla de fluidos detectada.', 'Mecanica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-03-22 07:30:00', '2026-03-22 08:00:00', '2026-03-23 12:00:00', '2026-03-23 12:30:00'),
(1, 3, 'Encoder de posicion agitador sin senal. Sistema en modo manual.', 'Electronica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-04-10 11:45:00', '2026-04-10 12:15:00', '2026-04-10 14:00:00', '2026-04-10 14:30:00'),
(1, 3, 'Correa trapezoidal del accionamiento principal con desgaste excesivo.', 'Mecanica', 'mant', 'cerrada', 'no', 'Mantenimiento', '2026-04-28 15:00:00', '2026-04-28 15:30:00', '2026-04-28 17:30:00', '2026-04-28 18:00:00'),
(1, 3, 'Sensor de pH fuera de calibracion. Alarma de calidad de proceso.', 'Electronica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-05-14 09:00:00', '2026-05-14 09:30:00', '2026-05-14 11:00:00', '2026-05-14 11:30:00'),
(1, 3, 'Rele termico del compresor de proceso activo. Motor parado por sobrecarga.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-06-03 08:30:00', '2026-06-03 09:00:00', '2026-06-03 11:00:00', '2026-06-03 11:30:00');

-- Activo 4: Elaboracion pelo
INSERT INTO tareas (tenant_id, activo, descripcion, categoria_tecnica, solicita, estado, afecta_produccion, departamento_responsable, momento_detencion, momento_asignacion, momento_liberacion, momento_cierre) VALUES
(1, 4, 'Cuchillas de corte con filo insuficiente. Producto fuera de especificacion.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-01-14 07:00:00', '2026-01-14 07:30:00', '2026-01-14 10:00:00', '2026-01-14 10:30:00'),
(1, 4, 'Motor de traccion sin arranque. Proteccion diferencial activa en tablero.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-01-30 08:00:00', '2026-01-30 08:30:00', '2026-01-30 11:00:00', '2026-01-30 11:30:00'),
(1, 4, 'Rodillo tensor sin tension. Pelo suelto en linea, atasco frecuente.', 'Mecanica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-02-15 13:00:00', '2026-02-15 13:30:00', '2026-02-15 15:30:00', '2026-02-15 16:00:00'),
(1, 4, 'Sensor de presencia sin deteccion. Linea corre sin material.', 'Electronica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-03-01 10:30:00', '2026-03-01 11:00:00', '2026-03-01 12:30:00', '2026-03-01 13:00:00'),
(1, 4, 'Desgaste en guias lineales. Producto con desvio de posicion.', 'Mecanica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-03-18 14:00:00', '2026-03-18 14:30:00', '2026-03-19 10:00:00', '2026-03-19 10:30:00'),
(1, 4, 'Falla en sistema de aspiracion. Acumulacion de material en cabina.', 'Neumatica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-04-04 09:00:00', '2026-04-04 09:30:00', '2026-04-04 12:00:00', '2026-04-04 12:30:00'),
(1, 4, 'Variador de velocidad con falla de temperatura interna. Ventilador obstruido.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-04-22 11:00:00', '2026-04-22 11:30:00', '2026-04-22 13:30:00', '2026-04-22 14:00:00'),
(1, 4, 'Rotura de eje portacuchillas. Parada total de linea.', 'Mecanica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-05-08 07:30:00', '2026-05-08 08:00:00', '2026-05-09 16:00:00', '2026-05-09 16:30:00'),
(1, 4, 'Cinta transportadora con desvio lateral. Riesgo de atasco.', 'Mecanica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-05-25 14:00:00', '2026-05-25 14:30:00', '2026-05-25 16:00:00', '2026-05-25 16:30:00'),
(1, 4, 'PLC en modo STOP por corte de alimentacion 24V. Falla en fuente.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-06-10 08:00:00', '2026-06-10 08:30:00', '2026-06-10 10:00:00', '2026-06-10 10:30:00');

-- Activo 5: GLP
INSERT INTO tareas (tenant_id, activo, descripcion, categoria_tecnica, solicita, estado, afecta_produccion, departamento_responsable, momento_detencion, momento_asignacion, momento_liberacion, momento_cierre) VALUES
(1, 5, 'Detector de gas con alarma. Presencia de mezcla GLP por encima del umbral.', 'Electronica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-01-08 06:00:00', '2026-01-08 06:30:00', '2026-01-08 09:00:00', '2026-01-08 09:30:00'),
(1, 5, 'Regulador de presion de 2 etapa con fluctuacion. Presion de salida inestable.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-01-24 10:00:00', '2026-01-24 10:30:00', '2026-01-24 13:00:00', '2026-01-24 13:30:00'),
(1, 5, 'Valvula solenoide de corte sin accionamiento. Sin paso de gas a proceso.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-02-07 08:30:00', '2026-02-07 09:00:00', '2026-02-07 10:30:00', '2026-02-07 11:00:00'),
(1, 5, 'Manometro de cabecera con aguja pegada. Indicacion erronea de presion.', 'Mecanica', 'mant', 'cerrada', 'no', 'Mantenimiento', '2026-02-20 11:00:00', '2026-02-20 11:30:00', '2026-02-20 13:00:00', '2026-02-20 13:30:00'),
(1, 5, 'Valvula de alivio disparada. Sobrepresion en linea de alimentacion.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-03-10 09:30:00', '2026-03-10 10:00:00', '2026-03-10 15:00:00', '2026-03-10 15:30:00'),
(1, 5, 'Fuga detectada en conexion flexible de acometida. Olor a gas en area.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-03-28 07:15:00', '2026-03-28 07:45:00', '2026-03-28 10:00:00', '2026-03-28 10:30:00'),
(1, 5, 'Sensor de temperatura de quemador sin senal. Control de combustion en falla.', 'Electronica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-04-15 14:00:00', '2026-04-15 14:30:00', '2026-04-15 16:30:00', '2026-04-15 17:00:00'),
(1, 5, 'Filtro de gas colmatado. Caida de presion diferencial elevada.', 'Mecanica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-05-02 09:00:00', '2026-05-02 09:30:00', '2026-05-02 11:00:00', '2026-05-02 11:30:00'),
(1, 5, 'Quemador sin ignicion. Electrodo de encendido danado.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-05-20 07:45:00', '2026-05-20 08:15:00', '2026-05-20 10:00:00', '2026-05-20 10:30:00'),
(1, 5, 'Modulo de control de combustion con falla de memoria. Reinicio requerido.', 'Electronica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-06-08 11:00:00', '2026-06-08 11:30:00', '2026-06-08 13:00:00', '2026-06-08 13:30:00');

-- Activo 6: Aero 9
INSERT INTO tareas (tenant_id, activo, descripcion, categoria_tecnica, solicita, estado, afecta_produccion, departamento_responsable, momento_detencion, momento_asignacion, momento_liberacion, momento_cierre) VALUES
(1, 6, 'Compresor principal con caida de presion de descarga. Filtro de aceite saturado.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-01-11 07:00:00', '2026-01-11 07:30:00', '2026-01-11 10:00:00', '2026-01-11 10:30:00'),
(1, 6, 'Secador frigorífico con alarma de temperatura alta. Punto de rocio elevado.', 'Electrica', 'mant', 'cerrada', 'no', 'Mantenimiento', '2026-01-27 09:30:00', '2026-01-27 10:00:00', '2026-01-27 12:30:00', '2026-01-27 13:00:00'),
(1, 6, 'Valvula check de descarga con fuga interna. Retorno de aire al compresor.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-02-12 14:00:00', '2026-02-12 14:30:00', '2026-02-12 17:00:00', '2026-02-12 17:30:00'),
(1, 6, 'Motor electrico del compresor sin arranque. Fusibles quemados en tablero.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-02-26 08:00:00', '2026-02-26 08:30:00', '2026-02-26 10:30:00', '2026-02-26 11:00:00'),
(1, 6, 'Rodamiento delantero con temperatura excesiva. Ruido de rodamiento.', 'Mecanica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-03-14 11:30:00', '2026-03-14 12:00:00', '2026-03-14 16:00:00', '2026-03-14 16:30:00'),
(1, 6, 'Sensor de presion de aceite con senal baja. Posible perdida de lubricacion.', 'Electronica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-04-01 09:00:00', '2026-04-01 09:30:00', '2026-04-01 11:30:00', '2026-04-01 12:00:00'),
(1, 6, 'Separador de aceite saturado. Consumo de aceite elevado en descarga.', 'Mecanica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-04-18 13:00:00', '2026-04-18 13:30:00', '2026-04-18 16:00:00', '2026-04-18 16:30:00'),
(1, 6, 'Valvula de regulacion de presion de red atascada en apertura parcial.', 'Mecanica', 'mant', 'cerrada', 'no', 'Mantenimiento', '2026-05-05 10:00:00', '2026-05-05 10:30:00', '2026-05-05 12:30:00', '2026-05-05 13:00:00'),
(1, 6, 'Condensador del secador frigorífico obstruido. Rendimiento reducido.', 'Mecanica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-05-22 08:30:00', '2026-05-22 09:00:00', '2026-05-22 11:00:00', '2026-05-22 11:30:00'),
(1, 6, 'Alarma de nivel de aceite bajo. Completar carga y revision de perdidas.', 'Mecanica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-06-09 14:00:00', '2026-06-09 14:30:00', '2026-06-09 16:00:00', '2026-06-09 16:30:00');

-- Activo 7: Aero 8
INSERT INTO tareas (tenant_id, activo, descripcion, categoria_tecnica, solicita, estado, afecta_produccion, departamento_responsable, momento_detencion, momento_asignacion, momento_liberacion, momento_cierre) VALUES
(1, 7, 'Tornillo helicoidal del compresor con desgaste prematuro. Vibracion elevada.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-01-09 07:30:00', '2026-01-09 08:00:00', '2026-01-10 12:00:00', '2026-01-10 12:30:00'),
(1, 7, 'Controlador de compresor con pantalla en blanco. Falla de alimentacion interna.', 'Electronica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-01-23 10:00:00', '2026-01-23 10:30:00', '2026-01-23 13:00:00', '2026-01-23 13:30:00'),
(1, 7, 'Termostato de aceite bloqueado en frio. Viscosidad elevada en arranque.', 'Mecanica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-02-06 08:00:00', '2026-02-06 08:30:00', '2026-02-06 11:00:00', '2026-02-06 11:30:00'),
(1, 7, 'Falla en sistema de arranque estrella-triangulo. Motor no alcanza velocidad.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-02-19 09:15:00', '2026-02-19 09:45:00', '2026-02-19 12:00:00', '2026-02-19 12:30:00'),
(1, 7, 'Elemento de filtro de aire de aspiracion colmatado. Temperatura excesiva.', 'Mecanica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-03-07 14:30:00', '2026-03-07 15:00:00', '2026-03-07 17:00:00', '2026-03-07 17:30:00'),
(1, 7, 'Valvula minima de presion defectuosa. Red sin presion al parar el equipo.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-03-24 11:00:00', '2026-03-24 11:30:00', '2026-03-24 14:00:00', '2026-03-24 14:30:00'),
(1, 7, 'Presostato diferencial de aceite activado. Posible taponamiento de filtro.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-04-11 08:45:00', '2026-04-11 09:15:00', '2026-04-11 11:30:00', '2026-04-11 12:00:00'),
(1, 7, 'Fuga en manguera flexible de aceite entre enfriador y tornillo.', 'Mecanica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-04-29 13:00:00', '2026-04-29 13:30:00', '2026-04-29 16:00:00', '2026-04-29 16:30:00'),
(1, 7, 'Interlock de puerta del gabinete activo. Fusible de 24V auxiliar quemado.', 'Electrica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-05-17 09:00:00', '2026-05-17 09:30:00', '2026-05-17 11:00:00', '2026-05-17 11:30:00'),
(1, 7, 'Enfriador de aceite con aletas obstruidas. Temperatura de descarga elevada.', 'Mecanica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-06-04 10:30:00', '2026-06-04 11:00:00', '2026-06-04 13:00:00', '2026-06-04 13:30:00');

-- Activo 8: Aero 6
INSERT INTO tareas (tenant_id, activo, descripcion, categoria_tecnica, solicita, estado, afecta_produccion, departamento_responsable, momento_detencion, momento_asignacion, momento_liberacion, momento_cierre) VALUES
(1, 8, 'Compresor de piston con perdida de compresion. Junta de culata gastada.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-01-16 07:00:00', '2026-01-16 07:30:00', '2026-01-17 12:00:00', '2026-01-17 12:30:00'),
(1, 8, 'Valvula de succion del 2 piston rota. Caudal reducido a la mitad.', 'Mecanica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-01-31 08:30:00', '2026-01-31 09:00:00', '2026-01-31 16:00:00', '2026-01-31 16:30:00'),
(1, 8, 'Presostato de alta presion activo. Sobrepresion en deposito.', 'Mecanica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-02-14 10:00:00', '2026-02-14 10:30:00', '2026-02-14 12:00:00', '2026-02-14 12:30:00'),
(1, 8, 'Motor sin arranque por baja tension. Problema en acometida electrica.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-03-02 09:00:00', '2026-03-02 09:30:00', '2026-03-02 11:30:00', '2026-03-02 12:00:00'),
(1, 8, 'Fuga en tuberia de interconexion entre etapas. Ruido de escape de aire.', 'Mecanica', 'mant', 'cerrada', 'no', 'Mantenimiento', '2026-03-19 14:00:00', '2026-03-19 14:30:00', '2026-03-19 16:30:00', '2026-03-19 17:00:00'),
(1, 8, 'Manometro de 2 etapa roto. Sin indicacion de presion intermedia.', 'Mecanica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-04-05 11:00:00', '2026-04-05 11:30:00', '2026-04-05 13:00:00', '2026-04-05 13:30:00'),
(1, 8, 'Correa de transmision con grietas. Riesgo de rotura inminente.', 'Mecanica', 'mant', 'cerrada', 'si', 'Mantenimiento', '2026-04-23 09:30:00', '2026-04-23 10:00:00', '2026-04-23 12:00:00', '2026-04-23 12:30:00'),
(1, 8, 'Fusible de motor quemado en tablero de distribucion. Sin alimentacion.', 'Electrica', 'prod', 'cerrada', 'si', 'Mantenimiento', '2026-05-10 08:00:00', '2026-05-10 08:30:00', '2026-05-10 09:30:00', '2026-05-10 10:00:00'),
(1, 8, 'Aceite lubricante degradado. Color oscuro, viscosidad fuera de rango.', 'Mecanica', 'prod', 'cerrada', 'no', 'Mantenimiento', '2026-05-27 10:00:00', '2026-05-27 10:30:00', '2026-05-27 12:30:00', '2026-05-27 13:00:00'),
(1, 8, 'Valvula de purga automatica del deposito sin funcionamiento. Acumulacion de agua.', 'Mecanica', 'mant', 'cerrada', 'no', 'Mantenimiento', '2026-06-12 14:30:00', '2026-06-12 15:00:00', '2026-06-12 16:30:00', '2026-06-12 17:00:00');

-- ============================================================
-- 5. ASIGNACIONES DE TAREAS A TECNICOS
-- ============================================================

INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec2 FROM tareas WHERE activo = 1 ORDER BY id LIMIT 5;
INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec1 FROM tareas WHERE activo = 1 ORDER BY id LIMIT 5 OFFSET 5;

INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec3 FROM tareas WHERE activo = 2 ORDER BY id LIMIT 5;
INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec4 FROM tareas WHERE activo = 2 ORDER BY id LIMIT 5 OFFSET 5;

INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec1 FROM tareas WHERE activo = 3 ORDER BY id LIMIT 5;
INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec5 FROM tareas WHERE activo = 3 ORDER BY id LIMIT 5 OFFSET 5;

INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec4 FROM tareas WHERE activo = 4 ORDER BY id LIMIT 5;
INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec3 FROM tareas WHERE activo = 4 ORDER BY id LIMIT 5 OFFSET 5;

INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec5 FROM tareas WHERE activo = 5 ORDER BY id LIMIT 5;
INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec2 FROM tareas WHERE activo = 5 ORDER BY id LIMIT 5 OFFSET 5;

INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec1 FROM tareas WHERE activo = 6 ORDER BY id LIMIT 5;
INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec3 FROM tareas WHERE activo = 6 ORDER BY id LIMIT 5 OFFSET 5;

INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec4 FROM tareas WHERE activo = 7 ORDER BY id LIMIT 5;
INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec5 FROM tareas WHERE activo = 7 ORDER BY id LIMIT 5 OFFSET 5;

INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec2 FROM tareas WHERE activo = 8 ORDER BY id LIMIT 5;
INSERT INTO asignacion (tenant_id, tarea_id, tecnico_id)
SELECT 1, id, @id_tec1 FROM tareas WHERE activo = 8 ORDER BY id LIMIT 5 OFFSET 5;

-- ============================================================
-- 6. PREVENTIVOS (5 por activo, activos 1..8)
-- ============================================================

INSERT INTO preventivo (tenant_id, activo, descripcion, detalle, categoria, estado, solicita, validacion_mantenimiento, frecuencia, fecha_de_creacion, fecha_realizado) VALUES
-- Activo 1
(1, 1, 'PM anual robot - revision general', 'Verificacion de todos los ejes, cambio de grasa en reductores, revision de cables y conectores, actualizacion de firmware.', 'Mantenimiento Preventivo', 'realizado', 'mant', 'aprobado', 'anual', '2026-01-05 08:00:00', '2026-01-08 17:00:00'),
(1, 1, 'Lubricacion semestral ejes 1-3', 'Aplicacion de grasa Kluber Isoflex NBU15 en puntos indicados en manual KUKA. Control de temperatura post-lubricacion.', 'Lubricacion', 'realizado', 'mant', 'aprobado', 'semestral', '2026-02-01 08:00:00', '2026-02-03 12:00:00'),
(1, 1, 'Revision sistema de seguridad', 'Prueba de parada de emergencia, verificacion de cortinas de luz, comprobacion de enclavamientos. Registro de tiempos de respuesta.', 'Seguridad', 'realizado', 'mant', 'aprobado', 'trimestral', '2026-03-15 08:00:00', '2026-03-15 14:00:00'),
(1, 1, 'Cambio filtros tablero electrico', 'Limpieza y reemplazo de filtros de ventilacion del armario de control KRC4. Revision de tarjetas de ejes.', 'Electrico', 'realizado', 'mant', 'aprobado', 'semestral', '2026-04-20 09:00:00', '2026-04-20 13:00:00'),
(1, 1, 'Calibracion y verificacion de TCP', 'Verificacion del punto central de la herramienta con esfera de calibracion. Ajuste si desvio mayor a 0.1mm.', 'Calibracion', 'pendiente', 'mant', NULL, 'trimestral', '2026-06-15 08:00:00', NULL),
-- Activo 2
(1, 2, 'PM bombas centrifugas - revision anual', 'Desmontaje y revision de impulsores, sellos mecanicos, rodamientos y alineacion.', 'Mantenimiento Preventivo', 'realizado', 'mant', 'aprobado', 'anual', '2026-01-10 08:00:00', '2026-01-15 17:00:00'),
(1, 2, 'Calibracion sensores de nivel', 'Calibracion de sensores ultrasonicos y de presion diferencial. Verificacion de senales en SCADA.', 'Calibracion', 'realizado', 'mant', 'aprobado', 'semestral', '2026-02-10 09:00:00', '2026-02-10 14:00:00'),
(1, 2, 'Cambio de filtros de linea', 'Reemplazo de elementos filtrantes en todos los filtros de canasta. Limpieza de carcasas.', 'Mecanico', 'realizado', 'mant', 'aprobado', 'trimestral', '2026-03-20 08:00:00', '2026-03-20 12:00:00'),
(1, 2, 'Revision valvulas de control', 'Verificacion de apertura y cierre de valvulas automaticas. Ajuste de posicionadores. Prueba de estanqueidad.', 'Mecanico', 'realizado', 'mant', 'aprobado', 'semestral', '2026-04-25 08:00:00', '2026-04-25 16:00:00'),
(1, 2, 'Inspeccion visual de canjerias y soportes', 'Revision de estado de canjerias, deteccion de corrosion, verificacion de soportes. Reporte fotografico.', 'Inspeccion', 'pendiente', 'mant', NULL, 'anual', '2026-06-20 08:00:00', NULL),
-- Activo 3
(1, 3, 'PM reactor - revision anual programada', 'Apertura e inspeccion interior del reactor. Limpieza de serpentin. Verificacion de agitador y sellos.', 'Mantenimiento Preventivo', 'realizado', 'mant', 'aprobado', 'anual', '2026-01-20 08:00:00', '2026-01-24 17:00:00'),
(1, 3, 'Cambio de aceite reductor agitador', 'Cambio de aceite sintetico Mobil SHC 630 en reductor de agitador. Analisis de aceite usado.', 'Lubricacion', 'realizado', 'mant', 'aprobado', 'semestral', '2026-02-15 09:00:00', '2026-02-15 12:00:00'),
(1, 3, 'Calibracion sondas de temperatura', 'Calibracion de Pt100 en reactor y lineas de proceso. Verificacion contra patron certificado.', 'Calibracion', 'realizado', 'mant', 'aprobado', 'trimestral', '2026-03-25 08:00:00', '2026-03-25 14:00:00'),
(1, 3, 'Revision sistema de calefaccion', 'Verificacion de resistencias electricas, reguladores y termostatos. Prueba de potencia.', 'Electrico', 'realizado', 'mant', 'aprobado', 'semestral', '2026-05-05 08:00:00', '2026-05-05 14:00:00'),
(1, 3, 'Inspeccion NDT intercambiador de calor', 'Ensayo por ultrasonido de espesor de pared del intercambiador. Deteccion de corrosion interna.', 'Inspeccion', 'pendiente', 'mant', NULL, 'anual', '2026-07-01 08:00:00', NULL),
-- Activo 4
(1, 4, 'PM anual linea de corte - revision completa', 'Revision de cuchillas, afilado o reemplazo. Control de guias, cintas y sistemas de traccion.', 'Mantenimiento Preventivo', 'realizado', 'mant', 'aprobado', 'anual', '2026-01-06 08:00:00', '2026-01-10 17:00:00'),
(1, 4, 'Lubricacion guias lineales y rodillos', 'Aplicacion de lubricante especifico en guias y rodillos segun instructivo. Verificacion de deslizamiento.', 'Lubricacion', 'realizado', 'mant', 'aprobado', 'mensual', '2026-02-05 08:00:00', '2026-02-05 11:00:00'),
(1, 4, 'Verificacion sistema de aspiracion', 'Control de presion de vacio, revision de filtros de bolsa, verificacion de motor del ventilador.', 'Mecanico', 'realizado', 'mant', 'aprobado', 'trimestral', '2026-03-10 09:00:00', '2026-03-10 13:00:00'),
(1, 4, 'Cambio correas de transmision', 'Reemplazo preventivo de correas antes de superar las 2000hs. Control de tension y alineacion de poleas.', 'Mecanico', 'realizado', 'mant', 'aprobado', 'semestral', '2026-04-15 08:00:00', '2026-04-15 14:00:00'),
(1, 4, 'Revision y limpieza variadores de frecuencia', 'Limpieza interna de variadores, revision de condensadores y bus de DC. Actualizacion de parametros.', 'Electrico', 'pendiente', 'mant', NULL, 'anual', '2026-06-25 08:00:00', NULL),
-- Activo 5
(1, 5, 'Prueba de estanqueidad anual - instalacion GLP', 'Prueba con nitrogeno a 1.5 bar por 30 minutos en toda la instalacion. Verificacion de cada junta.', 'Seguridad', 'realizado', 'mant', 'aprobado', 'anual', '2026-01-07 08:00:00', '2026-01-07 14:00:00'),
(1, 5, 'Revision y recambio de detectores de gas', 'Verificacion de detectores con gas patron. Reemplazo de sensores con mas de 3 anos de uso.', 'Seguridad', 'realizado', 'mant', 'aprobado', 'anual', '2026-02-12 09:00:00', '2026-02-12 13:00:00'),
(1, 5, 'PM reguladores y valvulas de seguridad', 'Revision de reguladores de 1 y 2 etapa, calibracion de valvulas de alivio, verificacion de corte automatico.', 'Mecanico', 'realizado', 'mant', 'aprobado', 'semestral', '2026-03-18 08:00:00', '2026-03-18 14:00:00'),
(1, 5, 'Limpieza y revision quemadores', 'Desmontaje de quemadores, limpieza de difusores, revision de electrodos de encendido e ionizacion.', 'Mecanico', 'realizado', 'mant', 'aprobado', 'trimestral', '2026-04-22 08:00:00', '2026-04-22 12:00:00'),
(1, 5, 'Verificacion sistema de control de combustion', 'Verificacion de secuencia de arranque, tiempos de pre-purga y post-purga, senales de llama.', 'Electronico', 'pendiente', 'mant', NULL, 'semestral', '2026-06-28 08:00:00', NULL),
-- Activo 6
(1, 6, 'PM anual compresor tornillo - cambio de aceite y filtros', 'Cambio de aceite sintetico, filtros de aceite, separador y filtro de aire. Revision completa segun plan OEM.', 'Mantenimiento Preventivo', 'realizado', 'mant', 'aprobado', 'anual', '2026-01-04 08:00:00', '2026-01-04 17:00:00'),
(1, 6, 'Revision y limpieza condensadores secador frigorífico', 'Limpieza de aletas del condensador. Verificacion de carga de refrigerante.', 'Mecanico', 'realizado', 'mant', 'aprobado', 'semestral', '2026-02-08 09:00:00', '2026-02-08 12:00:00'),
(1, 6, 'Calibracion presostatos y manometros', 'Verificacion y calibracion de presostatos de alta y baja, manometros de aceite y aire.', 'Calibracion', 'realizado', 'mant', 'aprobado', 'anual', '2026-03-16 09:00:00', '2026-03-16 13:00:00'),
(1, 6, 'Verificacion alineacion motor-compresor', 'Control de alineacion con reloj comparador y correccion si desvio mayor a 0.05mm.', 'Mecanico', 'realizado', 'mant', 'aprobado', 'anual', '2026-04-20 08:00:00', '2026-04-20 14:00:00'),
(1, 6, 'Analisis de aceite en servicio', 'Extraccion de muestra de aceite en operacion. Envio a laboratorio para analisis de viscosidad, metales y agua.', 'Inspeccion', 'pendiente', 'mant', NULL, 'semestral', '2026-07-05 08:00:00', NULL),
-- Activo 7
(1, 7, 'PM 4000hs compresor tornillo Aero 8', 'Cambio de aceite, filtros, correas y revision de valvulas segun programa de 4000hs del fabricante.', 'Mantenimiento Preventivo', 'realizado', 'mant', 'aprobado', 'semestral', '2026-01-03 08:00:00', '2026-01-03 17:00:00'),
(1, 7, 'Limpieza circuito de enfriamiento de aceite', 'Desmontaje y limpieza quimica del enfriador de aceite. Verificacion de caudal de agua de refrigeracion.', 'Mecanico', 'realizado', 'mant', 'aprobado', 'anual', '2026-02-18 09:00:00', '2026-02-18 15:00:00'),
(1, 7, 'Revision sistema de control Sigma Control 2', 'Actualizacion de firmware del controlador. Backup de parametros. Verificacion de entradas y salidas.', 'Electronico', 'realizado', 'mant', 'aprobado', 'anual', '2026-03-22 08:00:00', '2026-03-22 12:00:00'),
(1, 7, 'Control de vibraciones - analisis espectral', 'Medicion de vibraciones en puntos definidos con analizador SKF. Comparacion con valores base.', 'Inspeccion', 'realizado', 'mant', 'aprobado', 'trimestral', '2026-04-28 08:00:00', '2026-04-28 12:00:00'),
(1, 7, 'Cambio de correas de transmision Aero 8', 'Reemplazo preventivo de correas en V antes de 8000hs. Ajuste de tension con tensimetro.', 'Mecanico', 'pendiente', 'mant', NULL, 'anual', '2026-07-10 08:00:00', NULL),
-- Activo 8
(1, 8, 'PM anual compresor piston - revision completa', 'Cambio de anillos, juntas de culata, valvulas de succion y descarga, aceite y filtros de todos los pistones.', 'Mantenimiento Preventivo', 'realizado', 'mant', 'aprobado', 'anual', '2026-01-13 08:00:00', '2026-01-17 17:00:00'),
(1, 8, 'Cambio aceite y revision carter', 'Cambio de aceite mineral 68 en carter. Inspeccion de bielas, cigüenal y metales de apoyo.', 'Lubricacion', 'realizado', 'mant', 'aprobado', 'semestral', '2026-02-22 09:00:00', '2026-02-22 13:00:00'),
(1, 8, 'Revision valvulas de piston y culatas', 'Desmontaje, inspeccion y limpieza de valvulas de succion y descarga de 1 y 2 etapa.', 'Mecanico', 'realizado', 'mant', 'aprobado', 'semestral', '2026-04-01 08:00:00', '2026-04-01 16:00:00'),
(1, 8, 'Prueba de presion deposito de aire', 'Prueba hidrostatica del deposito a 1.3 veces la presion de trabajo. Verificacion de certificado IRAM.', 'Seguridad', 'realizado', 'mant', 'aprobado', 'anual', '2026-05-06 08:00:00', '2026-05-06 12:00:00'),
(1, 8, 'Cambio correa y alineacion poleas', 'Reemplazo de correa trapezoidal, verificacion de estado de poleas y alineacion con regla laser.', 'Mecanico', 'pendiente', 'mant', NULL, 'anual', '2026-07-15 08:00:00', NULL);

-- ============================================================
-- 7. ASIGNACIONES PREVENTIVOS A TECNICOS
-- ============================================================

INSERT INTO asignacion_preventivo (tenant_id, preventivo_id, tecnico_id)
SELECT 1, id, @id_tec2 FROM preventivo WHERE activo = 1;
INSERT INTO asignacion_preventivo (tenant_id, preventivo_id, tecnico_id)
SELECT 1, id, @id_tec3 FROM preventivo WHERE activo = 2;
INSERT INTO asignacion_preventivo (tenant_id, preventivo_id, tecnico_id)
SELECT 1, id, @id_tec1 FROM preventivo WHERE activo = 3;
INSERT INTO asignacion_preventivo (tenant_id, preventivo_id, tecnico_id)
SELECT 1, id, @id_tec4 FROM preventivo WHERE activo = 4;
INSERT INTO asignacion_preventivo (tenant_id, preventivo_id, tecnico_id)
SELECT 1, id, @id_tec5 FROM preventivo WHERE activo = 5;
INSERT INTO asignacion_preventivo (tenant_id, preventivo_id, tecnico_id)
SELECT 1, id, @id_tec1 FROM preventivo WHERE activo = 6;
INSERT INTO asignacion_preventivo (tenant_id, preventivo_id, tecnico_id)
SELECT 1, id, @id_tec4 FROM preventivo WHERE activo = 7;
INSERT INTO asignacion_preventivo (tenant_id, preventivo_id, tecnico_id)
SELECT 1, id, @id_tec3 FROM preventivo WHERE activo = 8;

-- ============================================================
-- 8. INFORMES TECNICOS (2-3 por tecnico = 13 informes)
-- ============================================================

-- tec1 (Carlos Mendoza) - 3 informes
INSERT INTO informe (tenant_id, fecha_de_creacion, descripcion, acciones, materiales, estado_final, causa_raiz, categoria_causa_raiz, pruebas, recomendaciones, seguimiento, estado_informe, revision) VALUES
(1, '2026-02-06 14:00:00',
 'Falla: alarma sobretemperatura controlador KUKA - Robot flexibles',
 'Se inspecciono el armario KRC4. Filtro de ventilacion obstruido y ventilador degradado. Se reemplazo ventilador y filtros. Se verifico temperatura en operacion durante 2 horas.',
 'Ventilador 24VDC (1 unidad), Filtros KRC4 (2 unidades)',
 'Temperatura normalizada en 45C. Robot operativo al 100%.',
 'Mantenimiento insuficiente de filtros. Intervalo de limpieza superado.',
 'Mantenimiento Preventivo Deficiente',
 'Prueba de ciclo completo por 30 minutos sin nuevas alarmas. Temperatura estable.',
 'Incluir limpieza de filtros KRC4 en plan de mantenimiento mensual.',
 'Verificar temperatura en proxima semana.',
 'cerrado', 'aprobado'),
(1, '2026-04-09 17:00:00',
 'Desgaste en reduccion eje 2 - Robot flexibles',
 'Cambio de reductor del eje 2. Desmontaje del brazo superior, extraccion del reductor Nabtesco RV-20N. Montaje con par de apriete segun especificacion 85 Nm. Carga de grasa segun procedimiento KUKA.',
 'Reductor Nabtesco RV-20N (1 unidad), Grasa Kluber Isoflex NBU15 (0.5kg)',
 'Juego en eje 2 dentro de tolerancia 0.02mm. Robot calibrado y operativo.',
 'Superacion de vida util del reductor. 28000 horas de operacion sin cambio.',
 'Fin de Vida Util',
 'Prueba de repeatibilidad con herramienta de calibracion. Desviacion menor a 0.05mm.',
 'Establecer intervalo de cambio de reductores cada 20000 horas de operacion.',
 'Monitorear temperatura de eje 2 durante primer mes.',
 'cerrado', 'aprobado'),
(1, '2026-05-13 10:30:00',
 'Error de calibracion post-mantenimiento - Robot flexibles',
 'Tras mantenimiento programado, robot presento error de posicion en punto de carga P01. Se realizo calibracion de todos los ejes mediante herramienta EMT. Se corrigio offset del eje 3 desplazamiento de 0.8 grados.',
 'Herramienta EMT KUKA (uso interno)',
 'Robot recalibrado. Error de posicion menor a 0.02mm en todos los puntos.',
 'Movimiento accidental del eje durante montaje por falta de bloqueo mecanico.',
 'Error Humano / Procedimiento',
 'Ciclo de produccion completo sin alarmas. 50 piezas evaluadas con calibre.',
 'Actualizar procedimiento para incluir bloqueo obligatorio de ejes.',
 'Auditoria de procedimiento a los 30 dias.',
 'cerrado', 'aprobado');

SET @inf_base_tec1 = LAST_INSERT_ID();
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec1, @id_tec1);
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec1 + 1, @id_tec1);
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec1 + 2, @id_tec1);

-- tec2 (Lucas Ferreyra) - 3 informes
INSERT INTO informe (tenant_id, fecha_de_creacion, descripcion, acciones, materiales, estado_final, causa_raiz, categoria_causa_raiz, pruebas, recomendaciones, seguimiento, estado_informe, revision) VALUES
(1, '2026-01-10 13:00:00',
 'Falla servo motor eje 4 - Robot flexibles',
 'Se diagnostico servo motor eje 4 con devanado abierto. Se reemplazo el servo motor KSD1-48 eje 4. Conexion de encoder y mastering del eje mediante herramienta KUKA EMT.',
 'Servo motor KSD1-48 eje 4 (1 unidad)',
 'Robot operativo. Ciclo de produccion restaurado.',
 'Desgaste electrico del devanado por ciclo termico repetido. 35000 horas en servicio.',
 'Fin de Vida Util',
 'Ciclo de test de 200 repeticiones. Corriente de motor dentro de parametros nominales.',
 'Evaluar reemplazo preventivo de servo motores por eje basado en horas de operacion.',
 'Monitorear corriente de todos los ejes en proxima semana.',
 'cerrado', 'aprobado'),
(1, '2026-03-21 13:30:00',
 'Falla comunicacion EtherCAT con drive eje 6',
 'Bus EtherCAT reporto perdida de comunicacion con drive eje 6. Se inspecciono conector RJ45: pin 3 con micro-rotura por fatiga. Se reemplazo conector y se redimensiono cable para evitar tension mecanica.',
 'Conector RJ45 apantallado (2 unidades), Cable EtherCAT CAT6 (0.5m)',
 'Comunicacion EtherCAT sin errores. Drive eje 6 operativo.',
 'Fatiga mecanica en conector por vibraciones sin sujecion adecuada del cable.',
 'Diseno / Instalacion Deficiente',
 'Test de comunicacion continua por 4 horas. Sin perdidas de paquetes.',
 'Revisar sujecion de todos los cables de bus en el armario KRC4.',
 'Verificar integridad de bus EtherCAT en proximo mantenimiento preventivo.',
 'cerrado', 'aprobado'),
(1, '2026-05-12 11:00:00',
 'Sensor de posicion eje 1 roto por impacto - Robot flexibles',
 'Encoder incremental del eje 1 con senal cuadratura erronea. Disco optico roto por impacto. Se reemplazo encoder y se realizo mastering completo.',
 'Encoder incremental KUKA eje 1 (1 unidad)',
 'Senal de encoder correcta. Robot con posicionamiento dentro de tolerancia.',
 'Colision menor no reportada que genero impacto en cuerpo del encoder.',
 'Error Humano / Operativo',
 'Mastering verificado con herramienta EMT. Test de repeatibilidad 0.03mm.',
 'Reforzar procedimiento de reporte de alarmas de colision aunque no sean graves.',
 'Seguimiento de alarmas de colision durante 2 semanas.',
 'cerrado', 'aprobado');

SET @inf_base_tec2 = LAST_INSERT_ID();
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec2, @id_tec2);
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec2 + 1, @id_tec2);
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec2 + 2, @id_tec2);

-- tec3 (Maria Gonzalez) - 2 informes
INSERT INTO informe (tenant_id, fecha_de_creacion, descripcion, acciones, materiales, estado_final, causa_raiz, categoria_causa_raiz, pruebas, recomendaciones, seguimiento, estado_informe, revision) VALUES
(1, '2026-02-10 15:00:00',
 'Cavitacion en bomba centrifuga #2 - Darsena de liquidos',
 'Se detecto cavitacion por ruido caracteristico y vibracion elevada. Valvula de succion con apertura insuficiente 30% en lugar del 100% requerido. Se abrio completamente la valvula y se purgo la linea.',
 'No se requirieron repuestos.',
 'Vibracion normalizada. Nivel de ruido reducido. Bomba operativa.',
 'Valvula de succion cerrada parcialmente por operador durante limpieza. No se repuso a posicion original.',
 'Error Humano / Operativo',
 'Medicion de vibracion post-intervencion: 2.3mm/s (antes: 8.1mm/s). Limite aceptable: 4.5mm/s.',
 'Instalar indicador de posicion visible en valvula de succion.',
 'Verificar valvulas de succion en proximo arranque de turno.',
 'cerrado', 'aprobado'),
(1, '2026-06-06 13:00:00',
 'Fuga sello mecanico bomba #2 - Darsena de liquidos',
 'Sello mecanico con fuga visible. Desmontaje de la bomba, extraccion del rotor y reemplazo del sello mecanico simple por uno de grafito/carburo de silicio.',
 'Sello mecanico simple DN50 (1 unidad), Juntas toricas (4 unidades)',
 'Sin fuga. Bomba en operacion normal.',
 'Fin de vida util del sello. 8200 horas de operacion (vida util estimada 8000hs).',
 'Fin de Vida Util',
 'Prueba de presion a 1.5 bar por 30 min sin fuga. Operacion continua 4 horas.',
 'Planificar cambio preventivo de sellos a las 7500 horas de operacion.',
 'Control visual de sellos en los proximos 7 dias.',
 'cerrado', 'aprobado');

SET @inf_base_tec3 = LAST_INSERT_ID();
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec3, @id_tec3);
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec3 + 1, @id_tec3);

-- tec4 (Rodrigo Sanchez) - 2 informes
INSERT INTO informe (tenant_id, fecha_de_creacion, descripcion, acciones, materiales, estado_final, causa_raiz, categoria_causa_raiz, pruebas, recomendaciones, seguimiento, estado_informe, revision) VALUES
(1, '2026-05-09 17:00:00',
 'Rotura eje portacuchillas - Elaboracion pelo',
 'Rotura por fatiga del eje portacuchillas principal. Se mecanizo eje de reemplazo en taller interno (acero SAE 4140). Montaje con ajuste H7/k6, apriete de chaveta y pin de seguridad.',
 'Acero SAE 4140 (barra 60mm x 300mm), Chaveta 12x8x60, Rodamientos 6210 (2 unidades)',
 'Eje montado dentro de tolerancias. Linea operativa.',
 'Fatiga por concentracion de tensiones en chavetero. Diseno original con radio de congosto insuficiente.',
 'Diseno / Falla Estructural',
 'Prueba de marcha en vacio 30 min. Vibracion 1.8mm/s. Prueba con carga nominal 1 hora sin novedad.',
 'Redisenar eje con radio de congosto minimo de 2mm en proxima parada programada.',
 'Monitoreo de vibracion semanal durante el primer mes.',
 'cerrado', 'aprobado'),
(1, '2026-01-15 11:00:00',
 'Valvula de control flujo atascada - Darsena de liquidos',
 'Valvula de mariposa DN100 bloqueada en posicion cerrada por acumulacion de solidos en sello. Desmontaje de actuador y cuerpo. Limpieza mecanica. Reemplazo de sello elastomerico EPDM danado.',
 'Sello EPDM DN100 (1 unidad), Kit de reparacion valvula mariposa.',
 'Valvula operando correctamente.',
 'Acumulacion de producto solidificado en sello por parada prolongada sin purga.',
 'Mantenimiento Preventivo Deficiente',
 'Ciclo completo de apertura/cierre con posicionador. Tiempo de respuesta dentro de especificacion.',
 'Incluir purga de valvulas en procedimiento de parada planificada.',
 'Verificar todas las valvulas de mariposa de la darsena en proximo preventivo.',
 'cerrado', 'aprobado');

SET @inf_base_tec4 = LAST_INSERT_ID();
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec4, @id_tec4);
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec4 + 1, @id_tec4);

-- tec5 (Valentina Torres) - 3 informes
INSERT INTO informe (tenant_id, fecha_de_creacion, descripcion, acciones, materiales, estado_final, causa_raiz, categoria_causa_raiz, pruebas, recomendaciones, seguimiento, estado_informe, revision) VALUES
(1, '2026-02-09 09:30:00',
 'Controlador temperatura en falla - Elaboracion HC',
 'Controlador PID de temperatura del reactor con desviacion de 15C. Parametro de ganancia integral Ti fuera de rango tras actualizacion de firmware. Se restauro configuracion desde backup y se reajustaron parametros PID mediante prueba de escalon.',
 'No se utilizaron repuestos.',
 'Control de temperatura estable. Desviacion maxima mas menos 0.5C del setpoint.',
 'Parametros PID sobrescritos por actualizacion de firmware sin respaldo previo.',
 'Error de Procedimiento / Software',
 'Prueba de escalon: respuesta en 4 min sin sobreoscilacion. Estabilidad verificada 2 horas.',
 'Siempre realizar backup de parametros antes de actualizar firmware.',
 'Monitorear tendencia de temperatura durante 72 horas.',
 'cerrado', 'aprobado'),
(1, '2026-03-08 12:30:00',
 'Variador de frecuencia falla F005 - Darsena de liquidos',
 'Variador ABB ACS880 de bomba #1 con falla F005 sobrecorriente en aceleracion. Se verifico curva de aceleracion: ramp-up de 0.5s insuficiente para motor de 22kW. Se reprogramo rampa a 3s y se ajusto limite de corriente al 110%.',
 'No se utilizaron repuestos.',
 'Variador operativo. Corriente de arranque dentro de parametros.',
 'Tiempo de rampa de aceleracion insuficiente. Corriente de pico supero limite del variador.',
 'Error de Parametrizacion',
 'Arranque monitoreado con pinza amperimetrica. Corriente maxima: 42A (nominal: 45A).',
 'Documentar parametros de variadores en ficha tecnica de cada equipo.',
 'Verificar parametros de todos los variadores de la darsena.',
 'cerrado', 'aprobado'),
(1, '2026-06-09 14:00:00',
 'Sensor de presion diferencial mal calibrado - Darsena de liquidos',
 'Sensor Endress+Hauser Deltabar M con offset de mas 120 mbar respecto al valor real. Se realizo calibracion a cero y span con manometro patron certificado. Se verifico senal 4-20mA en SCADA.',
 'No se utilizaron repuestos.',
 'Sensor calibrado. Senal correcta en SCADA.',
 'Deriva de zero por temperatura ambiente. Sensor expuesto a variaciones termicas.',
 'Condiciones Ambientales',
 'Comparacion con manometro patron en 3 puntos: 0%, 50% y 100%. Error menor a 0.1%.',
 'Instalar proteccion termica en sensor. Aumentar frecuencia de calibracion a semestral.',
 'Verificar calibracion a los 3 meses.',
 'cerrado', 'aprobado');

SET @inf_base_tec5 = LAST_INSERT_ID();
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec5, @id_tec5);
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec5 + 1, @id_tec5);
INSERT INTO asignacion_informe (tenant_id, informe_id, tecnico_id) VALUES (1, @inf_base_tec5 + 2, @id_tec5);

-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;
-- FIN DEL SCRIPT
-- ============================================================
