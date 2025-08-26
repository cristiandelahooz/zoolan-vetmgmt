--   ─────▀▄▀─────▄─────▄
--   ──▄███████▄──▀██▄██▀
--   ▄█████▀█████▄──▄█
--   ███████▀████████▀
--   ─▄▄▄▄▄▄███████▀

-- =================================================================================================
--  ZOOLAN VETMGMT - MASTER DATA MIGRATION
--  Version: V1.0.4__master_data.sql
--  Dependencies: V1.0.3__reference_data.sql
--  Description: Core business entities including users, employees, clients, pets, and products
-- =================================================================================================

-- Migration validation
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM flyway_schema_history WHERE version = '1.0.3' AND success = true) THEN
        RAISE EXCEPTION 'Reference data migration (V1.0.3) must complete successfully before running this migration';
    END IF;
END $$;

-- =================================================================================================
--  USERS - Base user information for all system users
--  Dependencies: None (foundational data)
--  Note: ID 1 (wornux admin) already exists from V0.0.1__init.sql
-- =================================================================================================
INSERT INTO users (active, birth_date, created_at, updated_at, user_id, username, reference_points, email,
                   first_name, last_name, municipality, nationality, password, phone_number, province, sector,
                   street_address, system_role)
VALUES 
    -- Employee users
    (true, '1985-01-15', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 2, 'vet.rodriguez',
     'Near the park', 'rodriguez@zoolan.com', 'Carlos', 'Rodriguez', 'Distrito Nacional', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8092223333', 'Santo Domingo', 'Naco',
     'Calle Principal 123', 'USER'),
    (true, '1995-01-15', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 3, 'groomer.pou',
     'Cerca de agora', 'pou@zoolan.com', 'Dayana', 'Pou', 'Distrito Nacional', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8090009999', 'Santo Domingo', 'Naco',
     'Calle Secundaria 456', 'USER'),
    (true, '1990-03-20', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 4, 'vet.martinez',
     'Next to the bank', 'martinez@zoolan.com', 'Ana', 'Martinez', 'Santiago de los Caballeros', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8095556666', 'Santiago', 'Centro',
     'Avenida Central 456', 'USER'),
    (true, '1993-07-01', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 5, 'recep.gonzalez',
     'Behind the church', 'gonzalez@zoolan.com', 'Maria', 'Gonzalez', 'Concepcion de La Vega', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8098889999', 'La Vega', 'La Esmeralda',
     'Callejon Sin Nombre 789', 'USER'),
    (true, '1988-11-10', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 6, 'vet.sanchez',
     'Near the beach', 'sanchez@zoolan.com', 'Pedro', 'Sanchez', 'San Felipe de Puerto Plata', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8091112222', 'Puerto Plata', 'Costambar',
     'Carretera Principal 101', 'USER'),
    (true, '1995-02-28', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 7, 'recep.diaz',
     'Near the school', 'diaz@zoolan.com', 'Laura', 'Diaz', 'San Cristobal', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8094445555', 'San Cristobal', 'Pueblo Nuevo',
     'Callejon de la Paz 202', 'USER'),
    (true, '1982-05-12', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 8, 'manager.gomez',
     'Frente al parque Mirador', 'gomez@zoolan.com', 'Roberto', 'Gomez', 'Distrito Nacional', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8091234567', 'Santo Domingo', 'Mirador Sur',
     'Avenida Mirador 101', 'MANAGER'),
    (true, '1987-09-23', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 9, 'manager.ramirez',
     'Cerca del supermercado', 'ramirez@zoolan.com', 'Sofia', 'Ramirez', 'Santiago de los Caballeros', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8097654321', 'Santiago', 'Villa Olga',
     'Calle 8 #45', 'MANAGER'),
    (true, '1992-11-30', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 10, 'groomer.mendez',
     'Al lado de la farmacia', 'mendez@zoolan.com', 'Luis', 'Mendez', 'Concepcion de La Vega', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8095551234', 'La Vega', 'Centro',
     'Calle Duarte 22', 'USER'),
    (true, '1995-04-18', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 11, 'groomer.fernandez',
     'Frente a la playa', 'fernandez@zoolan.com', 'Ana', 'Fernandez', 'San Felipe de Puerto Plata', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8094445678', 'Puerto Plata', 'Costambar',
     'Calle Principal 77', 'USER'),
    (true, '1989-08-14', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 12, 'lab.santos',
     'Cerca del hospital', 'santos@zoolan.com', 'Carlos', 'Santos', 'San Cristobal', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8093332222', 'San Cristobal', 'Centro',
     'Calle Independencia 10', 'USER'),
    (true, '1997-12-05', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 13, 'kennel.perez',
     'Detrás de la veterinaria', 'perez@zoolan.com', 'Juan', 'Perez', 'Bani', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8092221111', 'Peravia', 'El Fundo',
     'Callejón de la Paz 33', 'USER'),

    -- Client users
    (true, '1998-09-05', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 14, 'juan.perez',
     'Frente al supermercado', 'juan.perez@gmail.com', 'Juan', 'Perez', 'La Romana', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8297778888', 'La Romana', 'Villa Hermosa',
     'Avenida del Sol 303', 'USER'),
    (true, '1975-04-12', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 15, 'maria.rodriguez',
     'Al lado del río', 'maria.rodriguez@gmail.com', 'Maria', 'Rodriguez', 'San Francisco de Macoris', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8290001111', 'Duarte', 'Los Rieles',
     'Callejon de la Luna 404', 'USER'),
    (true, '1988-06-18', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 16, 'pedro.garcia',
     'Cerca de la finca', 'pedro.garcia@gmail.com', 'Pedro', 'Garcia', 'Moca', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8293334444', 'Espaillat', 'Salitre',
     'Carretera Vieja 505', 'USER'),
    (true, '1993-10-25', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 17, 'ana.martinez',
     'Cerca de la iglesia', 'ana.martinez@gmail.com', 'Ana', 'Martinez', 'Bani', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8296667777', 'Peravia', 'El Fundo',
     'Calle Principal 606', 'USER'),
    (true, '1980-12-30', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 18, 'carlos.lopez',
     'Al lado de la escuela', 'carlos.lopez@gmail.com', 'Carlos', 'Lopez', 'Azua de Compostela', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8299990000', 'Azua', 'La Bombita',
     'Avenida Central 707', 'USER'),
    (true, '1991-06-06', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 19, 'lucia.hernandez',
     'Cerca del Acropolis', 'lucia.hernandez@gmail.com', 'Lucia', 'Hernandez', 'Distrito Nacional', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8296666666', 'Santo Domingo', 'Piantini',
     'Calle Max Henriquez Ureña 45', 'USER'),
    (true, '1976-07-07', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 20, 'ramon.castillo',
     'Edificio Plaza Naco', 'ramon.castillo@gmail.com', 'Ramon', 'Castillo', 'Distrito Nacional', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8297777777', 'Santo Domingo', 'Ensanche Naco',
     'Calle Rafael Augusto Sanchez 21', 'USER'),
    (true, '1994-08-08', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 21, 'patricia.morales',
     'Cerca del parque', 'patricia.morales@gmail.com', 'Patricia', 'Morales', 'Santo Domingo Este', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8298888888', 'Santo Domingo', 'Alma Rosa',
     'Calle Primera 123', 'USER'),
    (true, '1981-09-09', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 22, 'fernando.gomez',
     'Plaza Internacional', 'fernando.gomez@gmail.com', 'Fernando', 'Gomez', 'Santiago de los Caballeros',
     'Dominican', '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8299999999', 'Santiago',
     'Los Jardines', 'Av. 27 de Febrero 89', 'USER'),
    (true, '1989-10-10', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 23, 'sandra.diaz',
     'Edificio Bella Vista Plaza', 'sandra.diaz@gmail.com', 'Sandra', 'Diaz', 'Distrito Nacional', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8290000000', 'Santo Domingo', 'Bella Vista',
     'Calle Dr. Delgado 156', 'USER'),

    -- Corporate client users
    (true, '1987-01-01', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 24, 'admin.petworld',
     'Torre Acrópolis, Piso 3', 'admin@petworld.com.do', 'Roberto', 'Fernandez', 'Distrito Nacional', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8091111111', 'Santo Domingo', 'Piantini',
     'Av. Winston Churchill 1099', 'USER'),
    (true, '1992-02-02', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 25, 'contacto.animalia',
     'Plaza Bella Terra', 'contacto@animalia.com.do', 'Carmen', 'Valdez', 'Santiago de los Caballeros', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8092222222', 'Santiago', 'Los Jardines',
     'Calle Del Sol 234', 'USER'),
    (true, '1979-03-03', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 26, 'gerente.mascotasfelices',
     'Megacentro, Local 12', 'gerente@mascotasfelices.do', 'Luis', 'Mendez', 'Santo Domingo Este', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8093333333', 'Santo Domingo', 'Ozama',
     'Av. San Vicente de Paul 45', 'USER'),
    (true, '1996-04-04', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 27, 'admin.refugiopatitas',
     'Cerca del parque Independencia', 'admin@refugiopatitas.org', 'Sofia', 'Ramirez', 'Distrito Nacional',
     'Dominican', '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8094444444', 'Santo Domingo',
     'Gazcue', 'Calle Mercedes 201', 'USER'),
    (true, '1983-05-05', '2025-08-26 12:12:33.452646', '2025-08-26 12:12:33.452646', 28, 'veterinaria.central',
     'Frente al hospital', 'info@vetcentral.com.do', 'Miguel', 'Torres', 'La Romana', 'Dominican',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS', '8095555555', 'La Romana', 'Centro',
     'Calle Duarte 78', 'USER');

-- =================================================================================================
--  EMPLOYEES - Employee-specific information
--  Dependencies: users table
-- =================================================================================================
INSERT INTO employee (available, hire_date, salary, employee_id, emergency_contact_name, emergency_contact_phone,
                      employee_role, gender)
VALUES 
    (true, '2022-01-15', 80000, 2, 'Maria Rodriguez', '8092223333', 'VETERINARIAN', 'MASCULINO'),
    (true, '2022-11-01', 40000, 3, 'Dayana Pou', '8090009999', 'GROOMER', 'FEMENINO'),
    (true, '2023-03-01', 75000, 4, 'Juan Martinez', '8095556666', 'VETERINARIAN', 'FEMENINO'),
    (true, '2023-06-20', 40000, 5, 'Pedro Gonzalez', '8098889999', 'RECEPTIONIST', 'FEMENINO'),
    (true, '2022-10-01', 78000, 6, 'Ana Sanchez', '8091112222', 'VETERINARIAN', 'MASCULINO'),
    (true, '2024-01-10', 42000, 7, 'Roberto Diaz', '8094445555', 'RECEPTIONIST', 'FEMENINO'),
    (true, '2020-05-01', 90000, 8, 'Laura Gomez', '8091234567', 'CLINIC_MANAGER', NULL),
    (true, '2021-07-15', 88000, 9, 'Carlos Ramirez', '8097654321', 'CLINIC_MANAGER', NULL),
    (true, '2023-02-10', 45000, 10, 'Sofia Mendez', '8095551234', 'GROOMER', NULL),
    (true, '2023-04-25', 46000, 11, 'Luis Fernandez', '8094445678', 'GROOMER', NULL),
    (true, '2022-11-30', 50000, 12, 'Carmen Santos', '8093332222', 'LAB_TECHNICIAN', NULL),
    (true, '2024-03-05', 35000, 13, 'Ana Perez', '8092221111', 'KENNEL_ASSISTANT', NULL);

-- =================================================================================================
--  EMPLOYEE WORK SCHEDULES - Employee working hours
--  Dependencies: employee table
-- =================================================================================================
INSERT INTO employee_work_schedule (end_time, is_off_day, start_time, employee_id, day_of_week)
VALUES 
    -- Veterinarian Carlos Rodriguez (employee_id: 2)
    ('17:00:00', false, '09:00:00', 2, 'MONDAY'),
    ('17:00:00', false, '09:00:00', 2, 'TUESDAY'),
    ('17:00:00', false, '09:00:00', 2, 'WEDNESDAY'),
    ('17:00:00', false, '09:00:00', 2, 'THURSDAY'),
    ('17:00:00', false, '09:00:00', 2, 'FRIDAY'),
    (NULL, true, NULL, 2, 'SATURDAY'),
    (NULL, true, NULL, 2, 'SUNDAY'),
    
    -- Groomer Dayana Pou (employee_id: 3)
    ('17:00:00', false, '09:00:00', 3, 'MONDAY'),
    ('17:00:00', false, '09:00:00', 3, 'TUESDAY'),
    ('17:00:00', false, '09:00:00', 3, 'WEDNESDAY'),
    ('17:00:00', false, '09:00:00', 3, 'THURSDAY'),
    ('17:00:00', false, '09:00:00', 3, 'FRIDAY'),
    (NULL, true, NULL, 3, 'SATURDAY'),
    (NULL, true, NULL, 3, 'SUNDAY'),
    
    -- Veterinarian Ana Martinez (employee_id: 4)
    ('16:00:00', false, '08:00:00', 4, 'MONDAY'),
    ('16:00:00', false, '08:00:00', 4, 'TUESDAY'),
    ('16:00:00', false, '08:00:00', 4, 'WEDNESDAY'),
    ('16:00:00', false, '08:00:00', 4, 'THURSDAY'),
    ('16:00:00', false, '08:00:00', 4, 'FRIDAY'),
    (NULL, true, NULL, 4, 'SATURDAY'),
    (NULL, true, NULL, 4, 'SUNDAY'),
    
    -- Receptionist Maria Gonzalez (employee_id: 5)
    ('17:00:00', false, '08:00:00', 5, 'MONDAY'),
    ('17:00:00', false, '08:00:00', 5, 'TUESDAY'),
    ('17:00:00', false, '08:00:00', 5, 'WEDNESDAY'),
    ('17:00:00', false, '08:00:00', 5, 'THURSDAY'),
    ('17:00:00', false, '08:00:00', 5, 'FRIDAY'),
    (NULL, true, NULL, 5, 'SATURDAY'),
    (NULL, true, NULL, 5, 'SUNDAY'),
    
    -- Veterinarian Pedro Sanchez (employee_id: 6)
    ('17:00:00', false, '09:00:00', 6, 'MONDAY'),
    ('17:00:00', false, '09:00:00', 6, 'TUESDAY'),
    ('17:00:00', false, '09:00:00', 6, 'WEDNESDAY'),
    ('17:00:00', false, '09:00:00', 6, 'THURSDAY'),
    ('17:00:00', false, '09:00:00', 6, 'FRIDAY'),
    (NULL, true, NULL, 6, 'SATURDAY'),
    (NULL, true, NULL, 6, 'SUNDAY'),
    
    -- Receptionist Laura Diaz (employee_id: 7)
    ('18:00:00', false, '09:00:00', 7, 'MONDAY'),
    ('18:00:00', false, '09:00:00', 7, 'TUESDAY'),
    ('18:00:00', false, '09:00:00', 7, 'WEDNESDAY'),
    ('18:00:00', false, '09:00:00', 7, 'THURSDAY'),
    ('18:00:00', false, '09:00:00', 7, 'FRIDAY'),
    (NULL, true, NULL, 7, 'SATURDAY'),
    (NULL, true, NULL, 7, 'SUNDAY');

-- =================================================================================================
--  CLIENTS - Client-specific information with payment terms and preferences
--  Dependencies: users table
-- =================================================================================================
INSERT INTO client (credit_limit, current_balance, payment_terms_days, verified, client_id, passport, cedula,
                    rnc, notes, company_name, emergency_contact_name, emergency_contact_number,
                    preferred_contact_method, rating, reference_source)
VALUES 
    -- Individual clients
    (1500, 0, 30, true, 14, NULL, '40212345678', NULL, 'Cliente frecuente, muy responsable', NULL, 'Juan Perez',
     '8297778888', 'WHATSAPP', 'MUY_BUENO', 'REFERIDO_CLIENTE'),
    (800, 0, 30, true, 15, NULL, '40234567890', NULL, 'Siempre puntual', NULL, 'Maria Rodriguez', '8290001111',
     'EMAIL', 'BUENO', 'GOOGLE'),
    (800, 0, 30, true, 16, NULL, '00112233445', NULL, 'Necesita recordatorios', NULL, 'Pedro Garcia', '8293334444',
     'PHONE_CALL', 'REGULAR', 'GOOGLE'),
    (1100, 0, 30, true, 17, NULL, '40298765432', NULL, 'Prefiere veterinario específico', NULL, 'Ana Martinez',
     '8296667777', 'WHATSAPP', 'MUY_BUENO', 'REFERIDO_CLIENTE'),
    (800, 0, 30, true, 18, NULL, '00123456789', NULL, 'Cliente regular', NULL, 'Carlos Lopez', '8299990000',
     'PHONE_CALL', 'BUENO', 'REDES_SOCIALES'),
    (800, 0, 30, true, 19, NULL, '40345678901', NULL, 'Cliente regular', NULL, 'Lucia Hernandez', '8296666666',
     'PHONE_CALL', 'BUENO', 'REDES_SOCIALES'),
    (800, 0, 30, true, 20, NULL, '00234567890', NULL, 'Cliente regular', NULL, 'Ramon Castillo', '8297777777',
     'PHONE_CALL', 'BUENO', 'REDES_SOCIALES'),
    (800, 0, 30, true, 21, NULL, '40456789012', NULL, 'Cliente regular', NULL, 'Patricia Morales', '8298888888',
     'PHONE_CALL', 'BUENO', 'REDES_SOCIALES'),
    (800, 0, 30, true, 22, NULL, '00345678901', NULL, 'Cliente regular', NULL, 'Fernando Gomez', '8299999999',
     'EMAIL', 'BUENO', 'REDES_SOCIALES'),
    (800, 0, 30, true, 23, NULL, '40567890123', NULL, 'Cliente regular', NULL, 'Sandra Diaz', '8290000000',
     'PHONE_CALL', 'BUENO', 'REDES_SOCIALES'),
    
    -- Corporate clients
    (5000, 0, 45, true, 24, NULL, NULL, '130123456', 'Tienda de mascotas - cliente corporativo importante',
     'Pet World S.R.L.', 'Roberto Fernandez', '8091111111', 'EMAIL', 'MUY_BUENO', 'RECOMENDACION_PROFESIONAL'),
    (3500, 500, 30, true, 25, NULL, NULL, '130234567', 'Cadena de tiendas de mascotas', 'Animalia Pet Shop',
     'Carmen Valdez', '8092222222', 'EMAIL', 'BUENO', 'PUBLICIDAD'),
    (4000, 0, 30, true, 26, NULL, NULL, '130345678', 'Centro de adopción y venta de mascotas',
     'Mascotas Felices SRL', 'Luis Mendez', '8093333333', 'EMAIL', 'MUY_BUENO', 'REFERIDO_CLIENTE'),
    (2000, 300, 60, true, 27, NULL, NULL, '101234567', 'ONG - descuento especial del 20%', 'Refugio Patitas Felices',
     'Sofia Ramirez', '8094444444', 'EMAIL', 'REGULAR', 'OTRO'),
    (3000, 0, 30, true, 28, NULL, NULL, '130456789', 'Clínica veterinaria asociada - referidos mutuos',
     'Veterinaria Central EIRL', 'Miguel Torres', '8095555555', 'EMAIL', 'BUENO', 'RECOMENDACION_PROFESIONAL');

-- =================================================================================================
--  PETS - Pet information
--  Dependencies: None (will be linked to owners via pet_owners table)
-- =================================================================================================
INSERT INTO pets (active, birth_date, id, breed, color, fur_type, gender, name, size, type)
VALUES 
    (true, '2020-05-10', 1, 'Yorkshire Terrier', 'Marrón', 'LARGO', 'MASCULINO', 'Milo', 'PEQUEÑO', 'PERRO'),
    (true, '2021-01-15', 2, 'Persa', 'Blanco', 'LARGO', 'FEMENINO', 'Lucy', 'MEDIANO', 'GATO'),
    (true, '2019-11-22', 3, 'Golden Retriever', 'Dorado', 'LARGO', 'MASCULINO', 'Max', 'GRANDE', 'PERRO'),
    (true, '2022-08-01', 4, 'Pastor Alemán', 'Negro y fuego', 'CORTO', 'FEMENINO', 'Bella', 'GRANDE', 'PERRO'),
    (true, '2018-03-12', 5, 'Bulldog Francés', 'Gris', 'CORTO', 'MASCULINO', 'Rocky', 'MEDIANO', 'PERRO'),
    (true, '2022-09-20', 6, 'Persa', 'Gris claro', 'LARGO', 'FEMENINO', 'Misty', 'MEDIANO', 'GATO'),
    (true, '2021-07-11', 7, 'Poodle', 'Blanco', 'LARGO', 'MASCULINO', 'Duke', 'MEDIANO', 'PERRO'),
    (true, '2023-01-05', 8, 'Persa', 'Beige', 'LARGO', 'FEMENINO', 'Coco', 'MEDIANO', 'GATO'),
    (true, '2020-02-28', 9, 'Rottweiler', 'Negro y fuego', 'CORTO', 'MASCULINO', 'Zeus', 'GRANDE', 'PERRO'),
    (true, '2022-11-15', 10, 'Yorkshire Terrier', 'Marrón', 'LARGO', 'FEMENINO', 'Sasha', 'PEQUEÑO', 'PERRO'),
    (true, '2021-03-20', 11, 'Labrador', 'Negro', 'CORTO', 'MASCULINO', 'Rex', 'GRANDE', 'PERRO'),
    (true, '2022-06-15', 12, 'Siamés', 'Crema', 'CORTO', 'FEMENINO', 'Luna', 'MEDIANO', 'GATO'),
    (true, '2020-09-10', 13, 'Beagle', 'Tricolor', 'CORTO', 'MASCULINO', 'Charlie', 'MEDIANO', 'PERRO'),
    (true, '2019-12-01', 14, 'Maine Coon', 'Atigrado', 'LARGO', 'MASCULINO', 'Simba', 'GRANDE', 'GATO'),
    (true, '2021-07-22', 15, 'Cocker Spaniel', 'Dorado', 'LARGO', 'FEMENINO', 'Daisy', 'MEDIANO', 'PERRO');

-- =================================================================================================
--  PET OWNERS - Relationship between clients and their pets
--  Dependencies: client table, pets table
-- =================================================================================================
INSERT INTO pet_owners (owners, pet_id)
VALUES 
    -- Individual pet owners
    (14, 1), (15, 2), (16, 3), (17, 4), (18, 5), (19, 6), (20, 7), (21, 8), (22, 9), (23, 10),
    
    -- Corporate clients with multiple pets
    (24, 15), (24, 14), (24, 13),
    (25, 14), (25, 13), (25, 12),
    (26, 13), (26, 12), (26, 11),
    (27, 12), (27, 11), (27, 10),
    (28, 11), (28, 10), (28, 9);

-- =================================================================================================
--  PRODUCTS - Inventory items for sale and internal use
--  Dependencies: suppliers table, warehouses table
-- =================================================================================================
INSERT INTO products (accounting_stock, active, available_stock, purchase_price, reorder_level, sales_price,
                      product_id, supplier, warehouse, category, description, name, unit, usage_type)
VALUES 
    -- Pet food products
    (100, true, 80, 1200.00, 20, 1500.00, 1, 1, 1, 'ALIMENTO', 'High-quality dry food for adult dogs',
     'Premium Dog Food', 'UNIDAD', 'VENTA'),
    (200, true, 180, 900.00, 30, 1200.00, 2, 1, 1, 'ALIMENTO', 'Nutritious wet food for cats',
     'High Protein Cat Food', 'UNIDAD', 'VENTA'),
    (300, true, 250, 350.00, 50, 500.00, 3, 2, 2, 'ALIMENTO', 'Natural training treats for dogs',
     'Healthy Dog Treats', 'DOCENA', 'VENTA'),
    (250, true, 200, 250.00, 40, 400.00, 4, 2, 2, 'ALIMENTO', 'Dental health treats for cats', 'Crunchy Cat Treats',
     'DOCENA', 'VENTA'),
     
    -- Hygiene products
    (150, true, 120, 500.00, 25, 800.00, 5, 3, 3, 'HIGIENE', 'Hypoallergenic shampoo for sensitive skin',
     'Gentle Dog Shampoo', 'CAJA', 'VENTA'),
    (180, true, 150, 400.00, 30, 700.00, 6, 3, 3, 'HIGIENE', 'Moisturizing shampoo for cats', 'Soothing Cat Shampoo',
     'CAJA', 'VENTA'),
    (400, true, 350, 200.00, 60, 300.00, 10, 5, 3, 'HIGIENE', 'Odor-control clumping litter', 'Clumping Cat Litter',
     'UNIDAD', 'VENTA'),
    (200, true, 170, 400.00, 35, 650.00, 14, 4, 3, 'HIGIENE', 'Tartar control chews for dogs', 'Dental Chews',
     'DOCENA', 'VENTA'),
     
    -- Accessories
    (120, true, 100, 350.00, 20, 600.00, 7, 4, 4, 'ACCESORIO', 'Adjustable nylon collar for medium dogs',
     'Durable Dog Collar', 'DOCENA', 'VENTA'),
    (130, true, 110, 300.00, 25, 550.00, 8, 4, 4, 'ACCESORIO', 'Decorative collar with bell for cats',
     'Stylish Cat Collar', 'DOCENA', 'VENTA'),
    (110, true, 90, 600.00, 15, 900.00, 9, 5, 4, 'ACCESORIO', 'Retractable leash for large dogs', 'Strong Dog Leash',
     'DOCENA', 'VENTA'),
    (25, true, 20, 2500.00, 5, 3500.00, 15, 5, 4, 'ACCESORIO', 'Airline-approved pet travel carrier', 'Pet Carrier',
     'CAJA', 'VENTA'),
     
    -- Medical products (internal use only)
    (50, true, 40, 1800.00, 10, 2500.00, 11, 1, 5, 'MEDICINA', 'Broad-spectrum antibiotics for pets',
     'Antibiotic Tablets', 'DOCENA', 'PRIVADO'),
    (75, true, 60, 1200.00, 15, 1800.00, 12, 2, 5, 'MEDICINA', 'Monthly flea prevention for dogs and cats',
     'Flea Treatment', 'DOCENA', 'PRIVADO'),
    (100, true, 80, 800.00, 20, 1200.00, 13, 3, 5, 'MEDICINA', 'Daily multivitamin supplements', 'Pet Vitamins',
     'DOCENA', 'PRIVADO');