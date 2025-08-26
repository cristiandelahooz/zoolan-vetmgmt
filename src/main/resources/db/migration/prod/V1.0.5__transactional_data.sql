--   ─────▀▄▀─────▄─────▄
--   ──▄███████▄──▀██▄██▀
--   ▄█████▀█████▄──▄█
--   ███████▀████████▀
--   ─▄▄▄▄▄▄███████▀

-- =================================================================================================
--  ZOOLAN VETMGMT - TRANSACTIONAL DATA MIGRATION
--  Version: V1.0.5__transactional_data.sql
--  Dependencies: V1.0.4__master_data.sql
--  Description: Business transaction data including medical histories, appointments, 
--               consultations, invoices, payments, and waiting room entries
-- =================================================================================================

-- Migration validation
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM flyway_schema_history WHERE version = '1.0.4' AND success = true) THEN
        RAISE EXCEPTION 'Master data migration (V1.0.4) must complete successfully before running this migration';
    END IF;
END $$;

-- =================================================================================================
--  MEDICAL HISTORIES - Pet medical records
--  Dependencies: pets table
-- =================================================================================================
INSERT INTO medical_histories (active, created_at, id, pet, updated_at, allergies, chronic_conditions,
                               medications, notes, surgeries, vaccinations)
VALUES 
    (true, '2025-08-26 12:12:33.452646', 1, 1, '2025-08-26 12:12:33.452646', 'Ninguna', 'Artritis', 'Ninguno',
     'Historial médico generado automáticamente', 'Ninguna', 'Necesita refuerzo'),
    (true, '2025-08-26 12:12:33.452646', 2, 2, '2025-08-26 12:12:33.452646', 'Polvo', 'Ninguna', 'Desparasitante',
     'Historial médico generado automáticamente', 'Ninguna', 'Al día'),
    (true, '2025-08-26 12:12:33.452646', 3, 3, '2025-08-26 12:12:33.452646', 'Polen', 'Ninguna',
     'Suplementos articulares', 'Historial médico generado automáticamente', 'Ninguna', 'Necesita refuerzo'),
    (true, '2025-08-26 12:12:33.452646', 4, 4, '2025-08-26 12:12:33.452646', 'Ninguna', 'Ninguna',
     'Tratamiento antipulgas', 'Historial médico generado automáticamente', 'Ninguna', 'Al día'),
    (true, '2025-08-26 12:12:33.452646', 5, 5, '2025-08-26 12:12:33.452646', 'Polvo', 'Ninguna', 'Ninguno',
     'Historial médico generado automáticamente', 'Esterilización', 'Necesita refuerzo'),
    (true, '2025-08-26 12:12:33.452646', 6, 6, '2025-08-26 12:12:33.452646', 'Polen', 'Displasia de cadera',
     'Desparasitante', 'Historial médico generado automáticamente', 'Ninguna', 'Al día'),
    (true, '2025-08-26 12:12:33.452646', 7, 7, '2025-08-26 12:12:33.452646', 'Ninguna', 'Artritis',
     'Suplementos articulares', 'Historial médico generado automáticamente', 'Ninguna', 'Necesita refuerzo'),
    (true, '2025-08-26 12:12:33.452646', 8, 8, '2025-08-26 12:12:33.452646', 'Polvo', 'Ninguna',
     'Tratamiento antipulgas', 'Historial médico generado automáticamente', 'Ninguna', 'Al día'),
    (true, '2025-08-26 12:12:33.452646', 9, 9, '2025-08-26 12:12:33.452646', 'Polen', 'Ninguna', 'Ninguno',
     'Historial médico generado automáticamente', 'Ninguna', 'Necesita refuerzo'),
    (true, '2025-08-26 12:12:33.452646', 10, 10, '2025-08-26 12:12:33.452646', 'Ninguna', 'Ninguna',
     'Desparasitante', 'Historial médico generado automáticamente', 'Esterilización', 'Al día'),
    (true, '2025-08-26 12:12:33.452646', 11, 11, '2025-08-26 12:12:33.452646', 'Polvo', 'Ninguna',
     'Suplementos articulares', 'Historial médico generado automáticamente', 'Ninguna', 'Necesita refuerzo'),
    (true, '2025-08-26 12:12:33.452646', 12, 12, '2025-08-26 12:12:33.452646', 'Polen', 'Displasia de cadera',
     'Tratamiento antipulgas', 'Historial médico generado automáticamente', 'Ninguna', 'Al día'),
    (true, '2025-08-26 12:12:33.452646', 13, 13, '2025-08-26 12:12:33.452646', 'Ninguna', 'Artritis', 'Ninguno',
     'Historial médico generado automáticamente', 'Ninguna', 'Necesita refuerzo'),
    (true, '2025-08-26 12:12:33.452646', 14, 14, '2025-08-26 12:12:33.452646', 'Polvo', 'Ninguna', 'Desparasitante',
     'Historial médico generado automáticamente', 'Ninguna', 'Al día'),
    (true, '2025-08-26 12:12:33.452646', 15, 15, '2025-08-26 12:12:33.452646', 'Polen', 'Ninguna',
     'Suplementos articulares', 'Historial médico generado automáticamente', 'Esterilización', 'Necesita refuerzo');

-- =================================================================================================
--  APPOINTMENTS - Scheduled veterinary appointments
--  Dependencies: client, employee, pets tables
-- =================================================================================================
INSERT INTO appointments (client_id, created_date, employee_id, end_appointment_date, id, last_modified_date,
                          pet_id, start_appointment_date, guest_client_phone, guest_client_email,
                          guest_client_name, reason, notes, created_by, guest_client_pet_breed,
                          guest_client_pet_type, last_modified_by, offering_type, status)
VALUES 
    -- Regular client appointments
    (14, '2025-08-26 16:12:33.452646+00', 2, '2025-08-27 12:42:33.452646', 1, '2025-08-26 16:12:33.452646+00', 1,
     '2025-08-27 12:12:33.452646', NULL, NULL, NULL, 'Vacunas de refuerzo', 'Cita programada', 'vet.martinez', NULL,
     NULL, 'vet.martinez', 'MEDICAL', 'EN_PROGRESO'),
    (15, '2025-08-26 16:12:33.452646+00', 2, '2025-08-28 12:42:33.452646', 2, '2025-08-26 16:12:33.452646+00', 2,
     '2025-08-28 12:12:33.452646', NULL, NULL, NULL, 'Baño y corte', 'Cita programada', 'vet.martinez', NULL, NULL,
     'vet.martinez', 'MEDICAL', 'COMPLETADA'),
    (16, '2025-08-26 16:12:33.452646+00', 2, '2025-08-29 12:42:33.452646', 3, '2025-08-26 16:12:33.452646+00', 3,
     '2025-08-29 12:12:33.452646', NULL, NULL, NULL, 'Urgencia médica', 'Cita programada', 'vet.martinez', NULL,
     NULL, 'vet.martinez', 'MEDICAL', 'PROGRAMADA'),
    (17, '2025-08-26 16:12:33.452646+00', 2, '2025-08-30 12:42:33.452646', 4, '2025-08-26 16:12:33.452646+00', 4,
     '2025-08-30 12:12:33.452646', NULL, NULL, NULL, 'Chequeo anual', 'Cita programada', 'vet.martinez', NULL, NULL,
     'vet.martinez', 'GROOMING', 'EN_PROGRESO'),
    (18, '2025-08-26 16:12:33.452646+00', 2, '2025-08-31 12:42:33.452646', 5, '2025-08-26 16:12:33.452646+00', 5,
     '2025-08-31 12:12:33.452646', NULL, NULL, NULL, 'Vacunas de refuerzo', 'Cita programada', 'vet.martinez', NULL,
     NULL, 'vet.martinez', 'MEDICAL', 'COMPLETADA'),
    (19, '2025-08-26 16:12:33.452646+00', 2, '2025-09-01 12:42:33.452646', 6, '2025-08-26 16:12:33.452646+00', 6,
     '2025-09-01 12:12:33.452646', NULL, NULL, NULL, 'Baño y corte', 'Cita programada', 'vet.martinez', NULL, NULL,
     'vet.martinez', 'MEDICAL', 'PROGRAMADA'),
    (20, '2025-08-26 16:12:33.452646+00', 2, '2025-09-02 12:42:33.452646', 7, '2025-08-26 16:12:33.452646+00', 7,
     '2025-09-02 12:12:33.452646', NULL, NULL, NULL, 'Urgencia médica', 'Cita programada', 'vet.martinez', NULL,
     NULL, 'vet.martinez', 'MEDICAL', 'EN_PROGRESO'),
    (21, '2025-08-26 16:12:33.452646+00', 2, '2025-09-03 12:42:33.452646', 8, '2025-08-26 16:12:33.452646+00', 8,
     '2025-09-03 12:12:33.452646', NULL, NULL, NULL, 'Chequeo anual', 'Cita programada', 'vet.martinez', NULL, NULL,
     'vet.martinez', 'GROOMING', 'COMPLETADA'),
    (22, '2025-08-26 16:12:33.452646+00', 2, '2025-09-04 12:42:33.452646', 9, '2025-08-26 16:12:33.452646+00', 9,
     '2025-09-04 12:12:33.452646', NULL, NULL, NULL, 'Vacunas de refuerzo', 'Cita programada', 'vet.martinez', NULL,
     NULL, 'vet.martinez', 'MEDICAL', 'PROGRAMADA'),
    (23, '2025-08-26 16:12:33.452646+00', 2, '2025-09-05 12:42:33.452646', 10, '2025-08-26 16:12:33.452646+00', 10,
     '2025-09-05 12:12:33.452646', NULL, NULL, NULL, 'Baño y corte', 'Cita programada', 'vet.martinez', NULL, NULL,
     'vet.martinez', 'MEDICAL', 'EN_PROGRESO'),

    -- Additional system appointments
    (14, '2025-08-26 16:13:27.929458+00', NULL, '2025-08-26 14:00:00', 11, '2025-08-26 16:13:27.929458+00', 1,
     '2025-08-26 13:00:00', NULL, NULL, NULL, 'Chequeo General', 'Todo bajo control', 'wornux', NULL, NULL, 'wornux',
     'MEDICAL', 'PROGRAMADA'),
    (14, '2025-08-26 16:16:29.339622+00', NULL, '2025-08-27 10:00:00', 13, '2025-08-26 16:16:29.339622+00', 1,
     '2025-08-27 08:00:00', NULL, NULL, NULL, 'Radiografia', 'Radiografia a Milo', 'wornux', NULL, NULL, 'wornux',
     'MEDICAL', 'PROGRAMADA'),
    (15, '2025-08-26 16:17:13.09865+00', NULL, '2025-08-26 17:00:00', 14, '2025-08-26 16:17:13.09865+00', 2,
     '2025-08-26 16:00:00', NULL, NULL, NULL, 'Chequeo General', '', 'wornux', NULL, NULL, 'wornux', 'MEDICAL',
     'PROGRAMADA'),
    (14, '2025-08-26 16:20:27.198751+00', NULL, '2025-08-28 09:00:00', 17, '2025-08-26 16:20:27.198751+00', 1,
     '2025-08-28 08:00:00', NULL, NULL, NULL, 'Chequeo general', '', 'wornux', NULL, NULL, 'wornux', 'MEDICAL',
     'PROGRAMADA'),
    (15, '2025-08-26 16:21:00.590361+00', NULL, '2025-08-28 12:00:00', 18, '2025-08-26 16:21:00.590361+00', 2,
     '2025-08-28 11:00:00', NULL, NULL, NULL, 'Rayos X', '', 'wornux', NULL, NULL, 'wornux', 'MEDICAL',
     'PROGRAMADA'),
    (15, '2025-08-26 16:22:33.329873+00', NULL, '2025-08-29 14:00:00', 20, '2025-08-26 16:22:33.329873+00', 2,
     '2025-08-29 13:00:00', NULL, NULL, NULL, 'Chequeo General', '', 'wornux', NULL, NULL, 'wornux', 'MEDICAL',
     'PROGRAMADA'),

    -- Guest client appointments
    (NULL, '2025-08-26 16:15:38.157874+00', NULL, '2025-08-26 15:00:00', 12, '2025-08-26 16:15:38.157874+00', NULL,
     '2025-08-26 14:00:00', '8291231324', 'a@example.com', 'Cristian', 'Cortar unas', 'Corte de unas trimestral',
     'wornux', 'Golden Retriever', 'PERRO', 'wornux', 'GROOMING', 'PROGRAMADA'),
    (NULL, '2025-08-26 16:18:10.643687+00', NULL, '2025-08-27 13:00:00', 15, '2025-08-26 16:18:10.643687+00', NULL,
     '2025-08-27 10:00:00', '8291231234', 'pedro@a.coom', 'Pedro', 'Corte de pelo', '', 'wornux', 'Chow Chow',
     'PERRO', 'wornux', 'GROOMING', 'PROGRAMADA'),
    (NULL, '2025-08-26 16:19:55.204852+00', NULL, '2025-08-27 14:00:00', 16, '2025-08-26 16:19:55.204852+00', NULL,
     '2025-08-27 12:00:00', '8291231234', 'abc@abe.com', 'Cristian', 'Banado perro grande', '', 'wornux',
     'Chihuahua', 'PERRO', 'wornux', 'GROOMING', 'PROGRAMADA'),
    (NULL, '2025-08-26 16:21:41.980863+00', NULL, '2025-08-28 13:00:00', 19, '2025-08-26 16:21:41.980863+00', NULL,
     '2025-08-28 11:00:00', '8291231234', 'abd@as.com', 'Juan', 'Banado perro mediano', '', 'wornux', 'Salchicha',
     'PERRO', 'wornux', 'GROOMING', 'PROGRAMADA');

-- =================================================================================================
--  REVISION TABLE - Audit revision tracking  
--  Dependencies: None (foundational for audit logs)
-- =================================================================================================
INSERT INTO revision (id, timestamp, ip_address, modifier_user)
VALUES 
    (1, 1756224807964, NULL, 'wornux'),
    (2, 1756224938191, NULL, 'wornux'),
    (3, 1756224989346, NULL, 'wornux'),
    (4, 1756225033104, NULL, 'wornux'),
    (5, 1756225090650, NULL, 'wornux'),
    (6, 1756225195210, NULL, 'wornux'),
    (7, 1756225227212, NULL, 'wornux'),
    (8, 1756225237645, NULL, 'wornux'),
    (9, 1756225261229, NULL, 'wornux'),
    (10, 1756225353333, NULL, 'wornux');

-- =================================================================================================
--  WAITING ROOM - Patient queue management
--  Dependencies: client, pets, employee tables
-- =================================================================================================
INSERT INTO waiting_room (arrival_time, assigned_groomer, assigned_veterinarian, client, completed_at,
                          consultation_started_at, id, pet, notes, priority, reason_for_visit, status, type)
VALUES 
    ('2025-08-26 13:00:00', NULL, 2, 14, NULL, NULL, 1, 1, 'Esperando consulta general', 'NORMAL', 'Chequeo rutinario', 'ESPERANDO', 'MEDICA'),
    ('2025-08-26 14:30:00', 3, NULL, 15, NULL, NULL, 2, 2, 'Necesita baño completo', 'NORMAL', 'Servicio de grooming', 'ESPERANDO', 'GROOMING'),
    ('2025-08-26 15:00:00', NULL, 4, 16, NULL, NULL, 3, 3, 'Emergencia - revisar herida', 'URGENTE', 'Herida en pata', 'EN_PROCESO', 'MEDICA'),
    ('2025-08-26 16:00:00', 10, NULL, 17, '2025-08-26 17:00:00', NULL, 4, 4, 'Servicio completado', 'NORMAL', 'Corte de pelo', 'COMPLETADO', 'GROOMING'),
    ('2025-08-26 16:30:00', NULL, 6, 18, NULL, '2025-08-26 16:45:00', 5, 5, 'Consulta en progreso', 'NORMAL', 'Vacunación', 'EN_PROCESO', 'MEDICA'),
    ('2025-08-26 17:00:00', NULL, NULL, 19, NULL, NULL, 6, 6, 'Esperando asignación de veterinario', 'EMERGENCIA', 'Dificultad respiratoria', 'ESPERANDO', 'MEDICA'),
    ('2025-08-26 17:15:00', 11, NULL, 20, NULL, NULL, 7, 7, 'En lista para grooming', 'NORMAL', 'Baño y secado', 'ESPERANDO', 'GROOMING');

-- Note: Additional transactional data (consultations, grooming_sessions, invoices, payments) 
-- can be added here following the same pattern with proper dependency management.
-- For production deployment, include only essential seed data to avoid performance issues.