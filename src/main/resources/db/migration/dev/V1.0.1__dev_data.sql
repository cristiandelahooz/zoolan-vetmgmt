--   ─────▀▄▀─────▄─────▄
--   ──▄███████▄──▀██▄██▀
--   ▄█████▀█████▄──▄█
--   ███████▀████████▀
--   ─▄▄▄▄▄▄███████▀

-- =================================================================================================
--  SAMPLE DATA FOR DEVELOPMENT ENVIRONMENT
-- =================================================================================================
-- Note: All IDs are auto-generated via IDENTITY columns

-- =================================================================================================
--  USERS TABLE - All users (employees and clients)
-- =================================================================================================
INSERT INTO users (username, password, email, first_name, last_name,
                   phone_number, birth_date,
                   nationality, province, municipality, sector, street_address,
                   reference_points,
                   active, created_at,
                   updated_at, system_role)
VALUES
    -- Employees (Veterinarians and Staff) - 5 employees
    ('vet.rodriguez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'rodriguez@zoolan.com', 'Carlos', 'Rodriguez', '8092223333', '1985-01-15',
     'Dominican', 'Santo Domingo', 'Distrito Nacional', 'Naco',
     'Calle Principal 123',
     'Near the park',
     TRUE, NOW(), NOW(), 'USER'),
    ('vet.martinez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'martinez@zoolan.com', 'Ana', 'Martinez', '8095556666', '1990-03-20',
     'Dominican', 'Santiago', 'Santiago de los Caballeros',
     'Centro',
     'Avenida Central 456',
     'Next to the bank', TRUE, NOW(), NOW(), 'USER'),
    ('recep.gonzalez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'gonzalez@zoolan.com', 'Maria', 'Gonzalez', '8098889999', '1993-07-01',
     'Dominican', 'La Vega', 'Concepcion de La Vega',
     'La Esmeralda',
     'Callejon Sin Nombre 789',
     'Behind the church', TRUE, NOW(), NOW(), 'USER'),
    ('vet.sanchez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'sanchez@zoolan.com', 'Pedro', 'Sanchez', '8091112222', '1988-11-10',
     'Dominican', 'Puerto Plata', 'San Felipe de Puerto Plata',
     'Costambar',
     'Carretera Principal 101',
     'Near the beach', TRUE, NOW(), NOW(), 'USER'),
    ('recep.diaz',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'diaz@zoolan.com', 'Laura', 'Diaz', '8094445555',
     '1995-02-28', 'Dominican', 'San Cristobal', 'San Cristobal',
     'Pueblo Nuevo',
     'Callejon de la Paz 202', 'Near the school', TRUE, NOW(), NOW(), 'USER'),
    ('manager.gomez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'gomez@zoolan.com', 'Roberto', 'Gomez', '8091234567', '1982-05-12',
     'Dominican', 'Santo Domingo', 'Distrito Nacional', 'Mirador Sur',
     'Avenida Mirador 101',
     'Frente al parque Mirador', TRUE, NOW(), NOW(), 'MANAGER'),
    ('manager.ramirez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'ramirez@zoolan.com', 'Sofia', 'Ramirez', '8097654321', '1987-09-23',
     'Dominican', 'Santiago', 'Santiago de los Caballeros', 'Villa Olga',
     'Calle 8 #45',
     'Cerca del supermercado', TRUE, NOW(), NOW(), 'MANAGER'),
    ('groomer.mendez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'mendez@zoolan.com', 'Luis', 'Mendez', '8095551234', '1992-11-30',
     'Dominican', 'La Vega', 'Concepcion de La Vega', 'Centro',
     'Calle Duarte 22',
     'Al lado de la farmacia', TRUE, NOW(), NOW(), 'USER'),
    ('groomer.fernandez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'fernandez@zoolan.com', 'Ana', 'Fernandez', '8094445678', '1995-04-18',
     'Dominican', 'Puerto Plata', 'San Felipe de Puerto Plata', 'Costambar',
     'Calle Principal 77',
     'Frente a la playa', TRUE, NOW(), NOW(), 'USER'),
    ('lab.santos',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'santos@zoolan.com', 'Carlos', 'Santos', '8093332222', '1989-08-14',
     'Dominican', 'San Cristobal', 'San Cristobal', 'Centro',
     'Calle Independencia 10',
     'Cerca del hospital', TRUE, NOW(), NOW(), 'USER'),
    ('kennel.perez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'perez@zoolan.com', 'Juan', 'Perez', '8092221111', '1997-12-05',
     'Dominican', 'Peravia', 'Bani', 'El Fundo',
     'Callejón de la Paz 33',
     'Detrás de la veterinaria', TRUE, NOW(), NOW(), 'USER'),

    -- Individual Clients (Personal) - 10 individual clients
    ('juan.perez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'juan.perez@gmail.com', 'Juan', 'Perez', '8297778888', '1998-09-05',
     'Dominican', 'La Romana', 'La Romana', 'Villa Hermosa',
     'Avenida del Sol 303',
     'Frente al supermercado', TRUE, NOW(), NOW(), 'USER'),
    ('maria.rodriguez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'maria.rodriguez@gmail.com', 'Maria', 'Rodriguez', '8290001111',
     '1975-04-12', 'Dominican', 'Duarte',
     'San Francisco de Macoris', 'Los Rieles',
     'Callejon de la Luna 404', 'Al lado del río', TRUE, NOW(), NOW(), 'USER'),
    ('pedro.garcia',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'pedro.garcia@gmail.com', 'Pedro', 'Garcia', '8293334444',
     '1988-06-18', 'Dominican', 'Espaillat', 'Moca', 'Salitre',
     'Carretera Vieja 505',
     'Cerca de la finca',
     TRUE, NOW(), NOW(), 'USER'),
    ('ana.martinez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'ana.martinez@gmail.com', 'Ana', 'Martinez',
     '8296667777', '1993-10-25', 'Dominican', 'Peravia', 'Bani',
     'El Fundo',
     'Calle Principal 606',
     'Cerca de la iglesia', TRUE, NOW(), NOW(), 'USER'),
    ('carlos.lopez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'carlos.lopez@gmail.com', 'Carlos', 'Lopez',
     '8299990000', '1980-12-30', 'Dominican', 'Azua',
     'Azua de Compostela',
     'La Bombita',
     'Avenida Central 707', 'Al lado de la escuela', TRUE, NOW(), NOW(),
     'USER'),
    ('lucia.hernandez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'lucia.hernandez@gmail.com', 'Lucia', 'Hernandez',
     '8296666666', '1991-06-06', 'Dominican', 'Santo Domingo',
     'Distrito Nacional',
     'Piantini',
     'Calle Max Henriquez Ureña 45', 'Cerca del Acropolis', TRUE, NOW(), NOW(),
     'USER'),
    ('ramon.castillo',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'ramon.castillo@gmail.com', 'Ramon', 'Castillo', '8297777777',
     '1976-07-07', 'Dominican', 'Santo Domingo',
     'Distrito Nacional', 'Ensanche Naco',
     'Calle Rafael Augusto Sanchez 21', 'Edificio Plaza Naco', TRUE, NOW(),
     NOW(), 'USER'),
    ('patricia.morales',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'patricia.morales@gmail.com', 'Patricia', 'Morales',
     '8298888888', '1994-08-08', 'Dominican', 'Santo Domingo',
     'Santo Domingo Este',
     'Alma Rosa',
     'Calle Primera 123', 'Cerca del parque', TRUE, NOW(), NOW(), 'USER'),
    ('fernando.gomez',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'fernando.gomez@gmail.com', 'Fernando', 'Gomez', '8299999999',
     '1981-09-09',
     'Dominican', 'Santiago', 'Santiago de los Caballeros',
     'Los Jardines',
     'Av. 27 de Febrero 89', 'Plaza Internacional', TRUE, NOW(), NOW(), 'USER'),
    ('sandra.diaz',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'sandra.diaz@gmail.com', 'Sandra', 'Diaz', '8290000000',
     '1989-10-10', 'Dominican', 'Santo Domingo',
     'Distrito Nacional', 'Bella Vista',
     'Calle Dr. Delgado 156', 'Edificio Bella Vista Plaza', TRUE, NOW(), NOW(),
     'USER'),

    -- Company Clients (Empresariales) - Users who represent companies - 5 companies
    ('admin.petworld',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'admin@petworld.com.do', 'Roberto', 'Fernandez',
     '8091111111', '1987-01-01', 'Dominican', 'Santo Domingo',
     'Distrito Nacional',
     'Piantini',
     'Av. Winston Churchill 1099', 'Torre Acrópolis, Piso 3', TRUE, NOW(),
     NOW(), 'USER'),
    ('contacto.animalia',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'contacto@animalia.com.do', 'Carmen', 'Valdez',
     '8092222222', '1992-02-02', 'Dominican', 'Santiago',
     'Santiago de los Caballeros',
     'Los Jardines',
     'Calle Del Sol 234', 'Plaza Bella Terra', TRUE, NOW(), NOW(), 'USER'),
    ('gerente.mascotasfelices',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'gerente@mascotasfelices.do', 'Luis', 'Mendez',
     '8093333333', '1979-03-03', 'Dominican', 'Santo Domingo',
     'Santo Domingo Este',
     'Ozama',
     'Av. San Vicente de Paul 45', 'Megacentro, Local 12', TRUE, NOW(), NOW(),
     'USER'),
    ('admin.refugiopatitas',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'admin@refugiopatitas.org', 'Sofia', 'Ramirez', '8094444444',
     '1996-04-04', 'Dominican', 'Santo Domingo',
     'Distrito Nacional', 'Gazcue',
     'Calle Mercedes 201', 'Cerca del parque Independencia', TRUE, NOW(), NOW(),
     'USER'),
    ('veterinaria.central',
     '$2a$10$rb2YK7DlCgaGOukZSq2LrOTZifJW7AN0ARthRrQLitPaofkTDozbS',
     'info@vetcentral.com.do', 'Miguel', 'Torres', '8095555555',
     '1983-05-05', 'Dominican', 'La Romana', 'La Romana', 'Centro',
     'Calle Duarte 78', 'Frente al hospital', TRUE, NOW(), NOW(), 'USER');

-- =================================================================================================
--  EMPLOYEE TABLE - Link employees to their user accounts  
-- =================================================================================================
-- We need to get the generated user_ids for the employees
-- Using a subquery to match by username
INSERT INTO employee (employee_id, gender, employee_role, salary, hire_date, available,
                      emergency_contact_name, emergency_contact_phone)
SELECT u.user_id,
       CASE
           WHEN u.username = 'vet.rodriguez' THEN 'MASCULINO'
           WHEN u.username = 'vet.martinez' THEN 'FEMENINO'
           WHEN u.username = 'vet.sanchez' THEN 'MASCULINO'
           WHEN u.username = 'recep.gonzalez' THEN 'FEMENINO'
           WHEN u.username = 'recep.diaz' THEN 'FEMENINO'
           END,
       CASE
           WHEN u.username IN ('vet.rodriguez', 'vet.martinez', 'vet.sanchez')
               THEN 'VETERINARIAN'
           WHEN u.username IN ('recep.gonzalez', 'recep.diaz')
               THEN 'RECEPTIONIST'
           WHEN u.username IN ('manager.gomez', 'manager.ramirez')
               THEN 'CLINIC_MANAGER'
           WHEN u.username IN ('groomer.mendez', 'groomer.fernandez')
               THEN 'GROOMER'
           WHEN u.username IN ('lab.santos')
               THEN 'LAB_TECHNICIAN'
           WHEN u.username IN ('kennel.perez')
               THEN 'KENNEL_ASSISTANT'
           ELSE 'KENNEL_ASSISTANT'
           END,
       CASE
           WHEN u.username = 'vet.rodriguez' THEN 80000.00
           WHEN u.username = 'vet.martinez' THEN 75000.00
           WHEN u.username = 'vet.sanchez' THEN 78000.00
           WHEN u.username = 'recep.gonzalez' THEN 40000.00
           WHEN u.username = 'recep.diaz' THEN 42000.00
           WHEN u.username = 'manager.gomez' THEN 90000.00
           WHEN u.username = 'manager.ramirez' THEN 88000.00
           WHEN u.username = 'groomer.mendez' THEN 45000.00
           WHEN u.username = 'groomer.fernandez' THEN 46000.00
           WHEN u.username = 'lab.santos' THEN 50000.00
           WHEN u.username = 'kennel.perez' THEN 35000.00
           ELSE 35000.00
           END,
       CASE
           WHEN u.username = 'vet.rodriguez' THEN '2022-01-15'::date
           WHEN u.username = 'vet.martinez' THEN '2023-03-01'::date
           WHEN u.username = 'vet.sanchez' THEN '2022-10-01'::date
           WHEN u.username = 'recep.gonzalez' THEN '2023-06-20'::date
           WHEN u.username = 'recep.diaz' THEN '2024-01-10'::date
           WHEN u.username = 'manager.gomez' THEN '2020-05-01'::date
           WHEN u.username = 'manager.ramirez' THEN '2021-07-15'::date
           WHEN u.username = 'groomer.mendez' THEN '2023-02-10'::date
           WHEN u.username = 'groomer.fernandez' THEN '2023-04-25'::date
           WHEN u.username = 'lab.santos' THEN '2022-11-30'::date
           WHEN u.username = 'kennel.perez' THEN '2024-03-05'::date
           ELSE '2024-01-01'::date
           END,
       TRUE,
       CASE
           WHEN u.username = 'vet.rodriguez' THEN 'Maria Rodriguez'
           WHEN u.username = 'vet.martinez' THEN 'Juan Martinez'
           WHEN u.username = 'vet.sanchez' THEN 'Ana Sanchez'
           WHEN u.username = 'recep.gonzalez' THEN 'Pedro Gonzalez'
           WHEN u.username = 'recep.diaz' THEN 'Roberto Diaz'
           WHEN u.username = 'manager.gomez' THEN 'Laura Gomez'
           WHEN u.username = 'manager.ramirez' THEN 'Carlos Ramirez'
           WHEN u.username = 'groomer.mendez' THEN 'Sofia Mendez'
           WHEN u.username = 'groomer.fernandez' THEN 'Luis Fernandez'
           WHEN u.username = 'lab.santos' THEN 'Carmen Santos'
           WHEN u.username = 'kennel.perez' THEN 'Ana Perez'
           END,
       CASE
           WHEN u.username = 'vet.rodriguez' THEN '8092223333'
           WHEN u.username = 'vet.martinez' THEN '8095556666'
           WHEN u.username = 'vet.sanchez' THEN '8091112222'
           WHEN u.username = 'recep.gonzalez' THEN '8098889999'
           WHEN u.username = 'recep.diaz' THEN '8094445555'
           WHEN u.username = 'manager.gomez' THEN '8091234567'
           WHEN u.username = 'manager.ramirez' THEN '8097654321'
           WHEN u.username = 'groomer.mendez' THEN '8095551234'
           WHEN u.username = 'groomer.fernandez' THEN '8094445678'
           WHEN u.username = 'lab.santos' THEN '8093332222'
           WHEN u.username = 'kennel.perez' THEN '8092221111'
           END
FROM users u
WHERE u.username IN
      ('vet.rodriguez', 'vet.martinez', 'vet.sanchez', 'recep.gonzalez',
       'recep.diaz', 'manager.gomez', 'manager.ramirez', 'groomer.mendez',
       'groomer.fernandez', 'lab.santos', 'kennel.perez');

-- =================================================================================================
--  EMPLOYEE_WORK_SCHEDULE TABLE - Assign work schedules to employees
-- =================================================================================================
INSERT INTO employee_work_schedule (employee_id, day_of_week, start_time, end_time, is_off_day)
VALUES
    -- vet.rodriguez
    ((SELECT user_id FROM users WHERE username = 'vet.rodriguez'), 'MONDAY', '09:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.rodriguez'), 'TUESDAY', '09:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.rodriguez'), 'WEDNESDAY', '09:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.rodriguez'), 'THURSDAY', '09:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.rodriguez'), 'FRIDAY', '09:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.rodriguez'), 'SATURDAY', NULL, NULL, TRUE),
    ((SELECT user_id FROM users WHERE username = 'vet.rodriguez'), 'SUNDAY', NULL, NULL, TRUE),
    -- vet.martinez
    ((SELECT user_id FROM users WHERE username = 'vet.martinez'), 'MONDAY', '08:00:00', '16:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.martinez'), 'TUESDAY', '08:00:00', '16:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.martinez'), 'WEDNESDAY', '08:00:00', '16:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.martinez'), 'THURSDAY', '08:00:00', '16:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.martinez'), 'FRIDAY', '08:00:00', '16:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.martinez'), 'SATURDAY', NULL, NULL, TRUE),
    ((SELECT user_id FROM users WHERE username = 'vet.martinez'), 'SUNDAY', NULL, NULL, TRUE),
    -- vet.sanchez
    ((SELECT user_id FROM users WHERE username = 'vet.sanchez'), 'MONDAY', '09:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.sanchez'), 'TUESDAY', '09:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.sanchez'), 'WEDNESDAY', '09:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.sanchez'), 'THURSDAY', '09:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.sanchez'), 'FRIDAY', '09:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'vet.sanchez'), 'SATURDAY', NULL, NULL, TRUE),
    ((SELECT user_id FROM users WHERE username = 'vet.sanchez'), 'SUNDAY', NULL, NULL, TRUE),
    -- recep.gonzalez
    ((SELECT user_id FROM users WHERE username = 'recep.gonzalez'), 'MONDAY', '08:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'recep.gonzalez'), 'TUESDAY', '08:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'recep.gonzalez'), 'WEDNESDAY', '08:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'recep.gonzalez'), 'THURSDAY', '08:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'recep.gonzalez'), 'FRIDAY', '08:00:00', '17:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'recep.gonzalez'), 'SATURDAY', NULL, NULL, TRUE),
    ((SELECT user_id FROM users WHERE username = 'recep.gonzalez'), 'SUNDAY', NULL, NULL, TRUE),
    -- recep.diaz
    ((SELECT user_id FROM users WHERE username = 'recep.diaz'), 'MONDAY', '09:00:00', '18:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'recep.diaz'), 'TUESDAY', '09:00:00', '18:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'recep.diaz'), 'WEDNESDAY', '09:00:00', '18:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'recep.diaz'), 'THURSDAY', '09:00:00', '18:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'recep.diaz'), 'FRIDAY', '09:00:00', '18:00:00', FALSE),
    ((SELECT user_id FROM users WHERE username = 'recep.diaz'), 'SATURDAY', NULL, NULL, TRUE),
    ((SELECT user_id FROM users WHERE username = 'recep.diaz'), 'SUNDAY', NULL, NULL, TRUE);

-- =================================================================================================
--  CLIENT TABLE - Individual and Company Clients
-- =================================================================================================
-- Insert individual clients (without RNC)
INSERT INTO client (client_id, cedula, passport, rnc, company_name,
                    preferred_contact_method, emergency_contact_name,
                    emergency_contact_number, rating, credit_limit,
                    current_balance, payment_terms_days, notes,
                    reference_source, verified)
SELECT u.user_id,
       CASE
           WHEN u.username = 'juan.perez' THEN '40212345678'
           WHEN u.username = 'maria.rodriguez' THEN '40234567890'
           WHEN u.username = 'pedro.garcia' THEN '00112233445'
           WHEN u.username = 'ana.martinez' THEN '40298765432'
           WHEN u.username = 'carlos.lopez' THEN '00123456789'
           WHEN u.username = 'lucia.hernandez' THEN '40345678901'
           WHEN u.username = 'ramon.castillo' THEN '00234567890'
           WHEN u.username = 'patricia.morales' THEN '40456789012'
           WHEN u.username = 'fernando.gomez' THEN '00345678901'
           WHEN u.username = 'sandra.diaz' THEN '40567890123'
           END,
       NULL, -- passport
       NULL, -- rnc (NULL for individual clients)
       NULL, -- company_name (NULL for individual clients)
       CASE
           WHEN u.username IN ('juan.perez', 'ana.martinez') THEN 'WHATSAPP'
           WHEN u.username IN ('maria.rodriguez', 'fernando.gomez') THEN 'EMAIL'
           ELSE 'PHONE_CALL'
           END,
       CONCAT(u.first_name, ' ', u.last_name),
       u.phone_number,
       CASE
           WHEN u.username IN ('juan.perez', 'ana.martinez') THEN 'MUY_BUENO'
           WHEN u.username IN ('pedro.garcia') THEN 'REGULAR'
           ELSE 'BUENO'
           END,
       CASE
           WHEN u.username IN ('juan.perez') THEN 1500.00
           WHEN u.username IN ('ana.martinez') THEN 1100.00
           ELSE 800.00
           END,
       0.00, -- current_balance
       30,   -- payment_terms_days
       CASE
           WHEN u.username = 'juan.perez'
               THEN 'Cliente frecuente, muy responsable'
           WHEN u.username = 'maria.rodriguez' THEN 'Siempre puntual'
           WHEN u.username = 'pedro.garcia' THEN 'Necesita recordatorios'
           WHEN u.username = 'ana.martinez'
               THEN 'Prefiere veterinario específico'
           ELSE 'Cliente regular'
           END,
       CASE
           WHEN u.username IN ('juan.perez', 'ana.martinez')
               THEN 'REFERIDO_CLIENTE'
           WHEN u.username IN ('maria.rodriguez', 'pedro.garcia') THEN 'GOOGLE'
           ELSE 'REDES_SOCIALES'
           END,
       TRUE
FROM users u
WHERE u.username IN
      ('juan.perez', 'maria.rodriguez', 'pedro.garcia', 'ana.martinez',
       'carlos.lopez',
       'lucia.hernandez', 'ramon.castillo', 'patricia.morales',
       'fernando.gomez',
       'sandra.diaz');

-- Insert company clients (with RNC)
INSERT INTO client (client_id, cedula, passport, rnc, company_name,
                    preferred_contact_method, emergency_contact_name,
                    emergency_contact_number, rating, credit_limit,
                    current_balance, payment_terms_days, notes,
                    reference_source, verified)
SELECT u.user_id,
       NULL,    -- cedula (NULL for companies)
       NULL,    -- passport
       CASE
           WHEN u.username = 'admin.petworld' THEN '130123456'
           WHEN u.username = 'contacto.animalia' THEN '130234567'
           WHEN u.username = 'gerente.mascotasfelices' THEN '130345678'
           WHEN u.username = 'admin.refugiopatitas' THEN '101234567'
           WHEN u.username = 'veterinaria.central' THEN '130456789'
           END,
       CASE
           WHEN u.username = 'admin.petworld' THEN 'Pet World S.R.L.'
           WHEN u.username = 'contacto.animalia' THEN 'Animalia Pet Shop'
           WHEN u.username = 'gerente.mascotasfelices'
               THEN 'Mascotas Felices SRL'
           WHEN u.username = 'admin.refugiopatitas'
               THEN 'Refugio Patitas Felices'
           WHEN u.username = 'veterinaria.central'
               THEN 'Veterinaria Central EIRL'
           END,
       'EMAIL', -- All companies prefer email
       CONCAT(u.first_name, ' ', u.last_name),
       u.phone_number,
       CASE
           WHEN u.username IN ('admin.petworld', 'gerente.mascotasfelices')
               THEN 'MUY_BUENO'
           WHEN u.username = 'admin.refugiopatitas' THEN 'REGULAR'
           ELSE 'BUENO'
           END,
       CASE
           WHEN u.username = 'admin.petworld' THEN 5000.00
           WHEN u.username = 'contacto.animalia' THEN 3500.00
           WHEN u.username = 'gerente.mascotasfelices' THEN 4000.00
           WHEN u.username = 'admin.refugiopatitas' THEN 2000.00
           WHEN u.username = 'veterinaria.central' THEN 3000.00
           END,
       CASE
           WHEN u.username = 'contacto.animalia' THEN 500.00
           WHEN u.username = 'admin.refugiopatitas' THEN 300.00
           ELSE 0.00
           END,
       CASE
           WHEN u.username = 'admin.refugiopatitas' THEN 60
           WHEN u.username = 'admin.petworld' THEN 45
           ELSE 30
           END,
       CASE
           WHEN u.username = 'admin.petworld'
               THEN 'Tienda de mascotas - cliente corporativo importante'
           WHEN u.username = 'contacto.animalia'
               THEN 'Cadena de tiendas de mascotas'
           WHEN u.username = 'gerente.mascotasfelices'
               THEN 'Centro de adopción y venta de mascotas'
           WHEN u.username = 'admin.refugiopatitas'
               THEN 'ONG - descuento especial del 20%'
           WHEN u.username = 'veterinaria.central'
               THEN 'Clínica veterinaria asociada - referidos mutuos'
           END,
       CASE
           WHEN u.username IN ('admin.petworld', 'veterinaria.central')
               THEN 'RECOMENDACION_PROFESIONAL'
           WHEN u.username = 'contacto.animalia' THEN 'PUBLICIDAD'
           WHEN u.username = 'gerente.mascotasfelices' THEN 'REFERIDO_CLIENTE'
           ELSE 'OTRO'
           END,
       TRUE
FROM users u
WHERE u.username IN
      ('admin.petworld', 'contacto.animalia', 'gerente.mascotasfelices',
       'admin.refugiopatitas', 'veterinaria.central');

-- =================================================================================================
--  PETS TABLE
-- =================================================================================================
INSERT INTO pets (name, type, breed, birth_date, gender, active, color, size,
                  fur_type)
VALUES ('Milo', 'PERRO', 'Yorkshire Terrier', '2020-05-10', 'MASCULINO', TRUE,
        'Marrón', 'PEQUEÑO',
        'LARGO'),
       ('Lucy', 'GATO', 'Persa', '2021-01-15', 'FEMENINO', TRUE, 'Blanco',
        'MEDIANO', 'LARGO'),
       ('Max', 'PERRO', 'Golden Retriever', '2019-11-22', 'MASCULINO', TRUE,
        'Dorado', 'GRANDE',
        'LARGO'),
       ('Bella', 'PERRO', 'Pastor Alemán', '2022-08-01', 'FEMENINO', TRUE,
        'Negro y fuego',
        'GRANDE', 'CORTO'),
       ('Rocky', 'PERRO', 'Bulldog Francés', '2018-03-12', 'MASCULINO', TRUE,
        'Gris', 'MEDIANO',
        'CORTO'),
       ('Misty', 'GATO', 'Persa', '2022-09-20', 'FEMENINO', TRUE, 'Gris claro',
        'MEDIANO', 'LARGO'),
       ('Duke', 'PERRO', 'Poodle', '2021-07-11', 'MASCULINO', TRUE, 'Blanco',
        'MEDIANO', 'LARGO'),
       ('Coco', 'GATO', 'Persa', '2023-01-05', 'FEMENINO', TRUE, 'Beige',
        'MEDIANO', 'LARGO'),
       ('Zeus', 'PERRO', 'Rottweiler', '2020-02-28', 'MASCULINO', TRUE,
        'Negro y fuego', 'GRANDE',
        'CORTO'),
       ('Sasha', 'PERRO', 'Yorkshire Terrier', '2022-11-15', 'FEMENINO', TRUE,
        'Marrón', 'PEQUEÑO',
        'LARGO'),
       -- Pets for company clients
       ('Rex', 'PERRO', 'Labrador', '2021-03-20', 'MASCULINO', TRUE, 'Negro',
        'GRANDE', 'CORTO'),
       ('Luna', 'GATO', 'Siamés', '2022-06-15', 'FEMENINO', TRUE, 'Crema',
        'MEDIANO', 'CORTO'),
       ('Charlie', 'PERRO', 'Beagle', '2020-09-10', 'MASCULINO', TRUE,
        'Tricolor', 'MEDIANO',
        'CORTO'),
       ('Simba', 'GATO', 'Maine Coon', '2019-12-01', 'MASCULINO', TRUE,
        'Atigrado', 'GRANDE',
        'LARGO'),
       ('Daisy', 'PERRO', 'Cocker Spaniel', '2021-07-22', 'FEMENINO', TRUE,
        'Dorado', 'MEDIANO',
        'LARGO');

-- =================================================================================================
--  PET_OWNERS TABLE - Assign pets to clients
-- =================================================================================================
-- Assign pets to individual clients
INSERT INTO pet_owners (owners, pet_id)
SELECT c.client_id,
       p.id
FROM (SELECT client_id
      FROM client
      WHERE rnc IS NULL
      ORDER BY client_id
      LIMIT 10) c
         CROSS JOIN LATERAL
    (SELECT id
     FROM pets
     ORDER BY id
     LIMIT 1 OFFSET (
         (c.client_id - (SELECT MIN(client_id) FROM client WHERE rnc IS NULL)) %
         10)) p;

-- Assign pets to company clients (companies can have multiple pets)
INSERT INTO pet_owners (owners, pet_id)
SELECT c.client_id,
       p.id
FROM (SELECT client_id FROM client WHERE rnc IS NOT NULL) c
         CROSS JOIN LATERAL
    (SELECT id
     FROM pets
     ORDER BY id DESC
     LIMIT 3 OFFSET ((c.client_id - (SELECT MIN(client_id)
                                     FROM client
                                     WHERE rnc IS NOT NULL)) %
                     5)) p;

-- =================================================================================================
--  SUPPLIERS TABLE
-- =================================================================================================
INSERT INTO suppliers (rnc, company_name, contact_person, contact_phone,
                       contact_email,
                       province, municipality, sector, street_address, active)
VALUES ('12345678900', 'Pets & Suppliers Co.', 'John Doe', '8091234567',
        'johndoe@petsuppliers.com', 'Santo Domingo', 'Santo Domingo Este',
        'Los Mina', '123 Main St.', TRUE),
       ('98765432100', 'Pet Supplies Inc.', 'Jane Smith', '8099876543',
        'janesmith@petsupplies.com', 'Santiago', 'Santiago de los Caballeros',
        'Los Jardines', '456 Elm St.', TRUE),
       ('45678912300', 'Animal Care Supplies', 'Carlos Perez', '8094567890',
        'carlosperez@animalcare.com', 'La Romana', 'La Romana', 'Villa Verde',
        '789 Oak St.', TRUE),
       ('32165498700', 'Pet Food & More', 'Maria Lopez', '8093216543',
        'marialopez@petfood.com', 'Puerto Plata', 'Puerto Plata', 'El Pueblito',
        '321 Pine St.', TRUE),
       ('65432178900', 'Vet Supplies Dominican', 'Luis Garcia', '8096543210',
        'luisgarcia@vetsupplies.do', 'San Cristobal', 'San Cristobal',
        'Villa Altagracia', '654 Maple St.', TRUE);

-- =================================================================================================
--  WAREHOUSES TABLE
-- =================================================================================================
INSERT INTO warehouses (name, warehouse_type, status, available_for_sale)
VALUES ('Almacén Principal', 'PRINCIPAL', TRUE, TRUE),
       ('Almacén Secundario', 'SECUNDARIO', TRUE, FALSE),
       ('Almacén de Higiene', 'SECUNDARIO', TRUE, TRUE),
       ('Almacén de Accesorios', 'SECUNDARIO', TRUE, TRUE),
       ('Almacén Médico', 'SECUNDARIO', TRUE, FALSE);

-- =================================================================================================
--  PRODUCTS TABLE
-- =================================================================================================
INSERT INTO products (name, description, active, purchase_price, sales_price,
                      accounting_stock,
                      available_stock, reorder_level, unit, usage_type, supplier, category,
                      warehouse)
SELECT p.name,
       p.description,
       p.active,
       p.purchase_price,
       p.sales_price,
       p.accounting_stock,
       p.available_stock,
       p.reorder_level,
       p.unit,
       p.usage_type,
       s.supplier_id,
       p.category,
       w.id
FROM (VALUES ('Premium Dog Food', 'High-quality dry food for adult dogs', TRUE,
              1200.00, 1500.00,
              100, 80, 20, 'UNIDAD', 'VENTA', 1, 'ALIMENTO', 1),
             ('High Protein Cat Food', 'Nutritious wet food for cats', TRUE,
              900.00, 1200.00,
              200, 180, 30, 'UNIDAD', 'VENTA', 1, 'ALIMENTO', 1),
             ('Healthy Dog Treats', 'Natural training treats for dogs', TRUE,
              350.00, 500.00,
              300, 250, 50, 'DOCENA', 'VENTA', 2, 'ALIMENTO', 2),
             ('Crunchy Cat Treats', 'Dental health treats for cats', TRUE,
              250.00, 400.00,
              250, 200, 40, 'DOCENA', 'VENTA', 2, 'ALIMENTO', 2),
             ('Gentle Dog Shampoo', 'Hypoallergenic shampoo for sensitive skin', TRUE,
              500.00, 800.00,
              150, 120, 25, 'CAJA', 'VENTA', 3, 'HIGIENE', 3),
             ('Soothing Cat Shampoo', 'Moisturizing shampoo for cats', TRUE,
              400.00, 700.00,
              180, 150, 30, 'CAJA', 'VENTA', 3, 'HIGIENE', 3),
             ('Durable Dog Collar', 'Adjustable nylon collar for medium dogs', TRUE,
              350.00, 600.00,
              120, 100, 20, 'DOCENA', 'VENTA', 4, 'ACCESORIO', 4),
             ('Stylish Cat Collar', 'Decorative collar with bell for cats', TRUE,
              300.00, 550.00,
              130, 110, 25, 'DOCENA', 'VENTA', 4, 'ACCESORIO', 4),
             ('Strong Dog Leash', 'Retractable leash for large dogs', TRUE,
              600.00, 900.00,
              110, 90, 15, 'DOCENA', 'VENTA', 5, 'ACCESORIO', 4),
             ('Clumping Cat Litter', 'Odor-control clumping litter', TRUE,
              200.00, 300.00,
              400, 350, 60, 'UNIDAD', 'VENTA', 5, 'HIGIENE', 3),
             ('Antibiotic Tablets', 'Broad-spectrum antibiotics for pets', TRUE,
              1800.00, 2500.00,
              50, 40, 10, 'DOCENA', 'PRIVADO', 1, 'MEDICINA', 5),
             ('Flea Treatment', 'Monthly flea prevention for dogs and cats', TRUE,
              1200.00, 1800.00,
              75, 60, 15, 'DOCENA', 'PRIVADO', 2, 'MEDICINA', 5),
             ('Pet Vitamins', 'Daily multivitamin supplements', TRUE,
              800.00, 1200.00,
              100, 80, 20, 'DOCENA', 'PRIVADO', 3, 'MEDICINA', 5),
             ('Dental Chews', 'Tartar control chews for dogs', TRUE,
              400.00, 650.00,
              200, 170, 35, 'DOCENA', 'VENTA', 4, 'HIGIENE', 3),
             ('Pet Carrier', 'Airline-approved pet travel carrier', TRUE,
              2500.00, 3500.00,
              25, 20,
              5, 'CAJA', 'VENTA', 5, 'ACCESORIO', 4)) AS p(name, description, active,
                                                           purchase_price, sales_price,
                                                           accounting_stock, available_stock,
                                                           reorder_level,
                                                           unit, usage_type, supplier_idx, category, warehouse_idx)
         JOIN suppliers s ON s.supplier_id = (SELECT supplier_id
                                              FROM suppliers
                                              ORDER BY supplier_id
                                              LIMIT 1 OFFSET p.supplier_idx - 1)
         JOIN warehouses w
              ON w.id = (SELECT id
                         FROM warehouses
                         ORDER BY id
                         LIMIT 1 OFFSET p.warehouse_idx - 1);

-- =================================================================================================
--  MEDICAL HISTORIES TABLE
-- =================================================================================================
INSERT INTO medical_histories (pet, allergies, medications, vaccinations,
                               surgeries, chronic_conditions, notes, created_at,
                               updated_at, active)
SELECT p.id,
       CASE (p.id % 3)
           WHEN 0 THEN 'Polen'
           WHEN 1 THEN 'Ninguna'
           ELSE 'Polvo'
           END,
       CASE (p.id % 4)
           WHEN 0 THEN 'Tratamiento antipulgas'
           WHEN 1 THEN 'Ninguno'
           WHEN 2 THEN 'Desparasitante'
           ELSE 'Suplementos articulares'
           END,
       CASE (p.id % 2)
           WHEN 0 THEN 'Al día'
           ELSE 'Necesita refuerzo'
           END,
       CASE (p.id % 5)
           WHEN 0 THEN 'Esterilización'
           ELSE 'Ninguna'
           END,
       CASE (p.id % 6)
           WHEN 0 THEN 'Displasia de cadera'
           WHEN 1 THEN 'Artritis'
           ELSE 'Ninguna'
           END,
       'Historial médico generado automáticamente',
       NOW(),
       NOW(),
       TRUE
FROM pets p
LIMIT 15;

-- =================================================================================================
--  CONSULTATIONS TABLE
-- =================================================================================================
INSERT INTO consultations (notes, diagnosis, treatment, prescription,
                           consultation_date, pet, veterinarian,
                           medical_history, created_at, updated_at, active)
SELECT 'Consulta de rutina',
       CASE (ROW_NUMBER() OVER () % 5)
           WHEN 0 THEN 'Saludable'
           WHEN 1 THEN 'Gastroenteritis'
           WHEN 2 THEN 'Dermatitis alérgica'
           WHEN 3 THEN 'Infección respiratoria'
           ELSE 'Otitis externa'
           END,
       CASE (ROW_NUMBER() OVER () % 5)
           WHEN 0 THEN 'N/A'
           WHEN 1 THEN 'Fluidoterapia y medicación antiemética'
           WHEN 2 THEN 'Antihistamínicos y crema tópica'
           WHEN 3 THEN 'Antibióticos y supresor de tos'
           ELSE 'Gotas óticas y limpieza'
           END,
       CASE (ROW_NUMBER() OVER () % 5)
           WHEN 0 THEN 'N/A'
           WHEN 1 THEN 'Metronidazol'
           WHEN 2 THEN 'Difenhidramina'
           WHEN 3 THEN 'Amoxicilina'
           ELSE 'Otomax'
           END,
       NOW() - (INTERVAL '1 day' * (ROW_NUMBER() OVER () * 10)),
       mh.pet,
       e.employee_id,
       mh.id,
       NOW(),
       NOW(),
       TRUE
FROM medical_histories mh
         CROSS JOIN (SELECT employee_id
                     FROM employee
                     WHERE employee_role = 'VETERINARIAN'
                     LIMIT 1) e
LIMIT 10;

-- =================================================================================================
--  WAITING ROOM TABLE
-- =================================================================================================
INSERT INTO waiting_room (client, pet, arrival_time, status, reason_for_visit,
                          priority, notes,
                          consultation_started_at, completed_at)
SELECT po.owners,
       po.pet_id,
       NOW() - (INTERVAL '1 hour' * (ROW_NUMBER() OVER ())),
       CASE (ROW_NUMBER() OVER () % 3)
           WHEN 0 THEN 'ESPERANDO'
           WHEN 1 THEN 'EN_CONSULTA'
           ELSE 'ESPERANDO'
           END,
       CASE (ROW_NUMBER() OVER () % 5)
           WHEN 0 THEN 'Chequeo'
           WHEN 1 THEN 'Vacunación'
           WHEN 2 THEN 'Problema de piel'
           WHEN 3 THEN 'Vómitos'
           ELSE 'Cojera'
           END,
       CASE (ROW_NUMBER() OVER () % 3)
           WHEN 0 THEN 'NORMAL'
           WHEN 1 THEN 'URGENTE'
           ELSE 'EMERGENCIA'
           END,
       'Paciente en sala de espera',
       CASE
           WHEN (ROW_NUMBER() OVER () % 3) = 1
               THEN NOW() - (INTERVAL '30 minutes')
           ELSE NULL
           END,
       NULL
FROM pet_owners po
LIMIT 5;

-- =================================================================================================
--  SERVICES TABLE
-- =================================================================================================
INSERT INTO services (name, description, active, price, service_type, created_at, updated_at)
SELECT s.name,
       s.description,
       s.active,
       s.price,
       s.service_type,
       NOW(),
       NOW()
FROM (VALUES
          ('Consulta Veterinaria', 'Consulta general y diagnóstico', TRUE, 800.00, 'MEDICAL'),
          ('Baño y Corte', 'Servicio de grooming completo', TRUE, 1200.00, 'GROOMING'),
          ('Vacunación', 'Aplicación de vacunas', TRUE, 600.00, 'MEDICAL'),
          ('Desparasitación', 'Tratamiento antiparasitario', TRUE, 500.00, 'MEDICAL'),
          ('Guardería', 'Cuidado diario de mascotas', TRUE, 1000.00, 'KENNEL'),
          ('Laboratorio', 'Análisis clínicos veterinarios', TRUE, 1500.00, 'LAB'),
          ('Pensión', 'Hospedaje temporal de mascotas', TRUE, 2000.00, 'KENNEL'),
          ('Cirugía menor', 'Procedimientos quirúrgicos menores', TRUE, 3500.00, 'MEDICAL'),
          ('Corte de uñas', 'Recorte profesional de uñas', TRUE, 300.00, 'GROOMING'),
          ('Limpieza dental', 'Limpieza bucal veterinaria', TRUE, 900.00, 'MEDICAL')
     ) AS s(name, description, active, price, service_type);


-- =================================================================================================
--  INVOICES TABLE
-- =================================================================================================
-- Create sample invoice for a company client
INSERT INTO invoices (client, issued_date, payment_date, sales_order, status,
                      subtotal, discount_percentage, discount, tax, total,
                      paid_to_date, notes, active, created_by, created_date,
                      last_modified_by, last_modified_date)
SELECT c.client_id,
       CURRENT_DATE - INTERVAL '15 days',
       CURRENT_DATE + INTERVAL '15 days',
       'SO-001',
       'PENDING',
       2500.00,
       CASE
           WHEN c.company_name = 'Refugio Patitas Felices' THEN 20.00
           ELSE 10.00
           END,
       CASE
           WHEN c.company_name = 'Refugio Patitas Felices' THEN 500.00
           ELSE 250.00
           END,
       CASE
           WHEN c.company_name = 'Refugio Patitas Felices' THEN 360.00
           ELSE 405.00
           END,
       CASE
           WHEN c.company_name = 'Refugio Patitas Felices' THEN 2360.00
           ELSE 2655.00
           END,
       0.00,
       CASE
           WHEN c.company_name = 'Refugio Patitas Felices'
               THEN 'Factura con descuento especial ONG'
           ELSE 'Factura cliente corporativo'
           END,
       TRUE,
        'admin',
       NOW(),
       'admin',
       NOW()
FROM client c
WHERE c.rnc IS NOT NULL
LIMIT 3;

-- =================================================================================================
--   Insert sample invoices from consultations made by veterinarians
-- =================================================================================================
INSERT INTO invoices (created_by, created_date, last_modified_by,
                      last_modified_date, client, consultation, consultation_notes,
                      issued_date, payment_date, sales_order, status, subtotal,
                      discount_percentage, discount, tax, total, paid_to_date, notes, active)
SELECT i.created_by,
       i.created_date,
       i.last_modified_by,
       i.last_modified_date,
       i.client,
       i.consultation,
       i.consultation_notes,
       i.issued_date,
       i.payment_date,
       i.sales_order,
       i.status,
       i.subtotal,
       i.discount_percentage,
       i.discount,
       i.tax,
       i.total,
       i.paid_to_date,
       i.notes,
       i.active
FROM (VALUES
            ('vet.martinez', NOW(), 'vet.martinez',
           NOW(),13, 1, 'aa',
           CURRENT_DATE, CURRENT_DATE + INTERVAL '30 day', NULL, 'PENDING',
           3000, CAST(NULL AS NUMERIC), CAST(NULL AS NUMERIC), 540, 3540, 0, NULL, TRUE),
            ('vet.sanchez', NOW(), 'vet.sanchez',
           NOW(),14, 2, 'bb',
           CURRENT_DATE, CURRENT_DATE + INTERVAL '30 day', NULL, 'PENDING',
           1500, CAST(NULL AS NUMERIC), CAST(NULL AS NUMERIC), 270, 1770, 0, NULL, TRUE),
            ('vet.martinez', NOW(), 'vet.martinez',
           NOW(),15, 3, 'cc',
           CURRENT_DATE, CURRENT_DATE + INTERVAL '30 day', NULL, 'PENDING',
           4500, CAST(NULL AS NUMERIC), CAST(NULL AS NUMERIC), 810, 5310, 0, NULL, TRUE),
            ('vet.sanchez', NOW(), 'vet.sanchez',
            NOW(),16, 4, 'dd',
            CURRENT_DATE, CURRENT_DATE + INTERVAL '30 day', NULL, 'PENDING',
            2000, CAST(NULL AS NUMERIC), CAST(NULL AS NUMERIC), 360, 2360, 0, NULL, TRUE),
            ('vet.martinez', NOW(), 'vet.martinez',
            NOW(),17, 5, 'ee',
            CURRENT_DATE, CURRENT_DATE + INTERVAL '30 day', NULL, 'PENDING',
            3500, CAST(NULL AS NUMERIC), CAST(NULL AS NUMERIC), 630, 4130, 0, NULL, TRUE),
            ('vet.sanchez', NOW(), 'vet.sanchez',
            NOW(),18, 6, 'ff',
            CURRENT_DATE, CURRENT_DATE + INTERVAL '30 day', NULL,
            'PAID', 4000, CAST(NULL AS NUMERIC), CAST(NULL AS NUMERIC), 720, 4720, 0, NULL, TRUE),
            ('vet.martinez', NOW(), 'vet.martinez',
            NOW(),19, 7, 'gg',
            CURRENT_DATE, CURRENT_DATE + INTERVAL '30 day', NULL,
            'PAID', 2500, CAST(NULL AS NUMERIC), CAST(NULL AS NUMERIC), 450, 2950, 0, NULL, TRUE),
            ('vet.sanchez', NOW(), 'vet.sanchez',
            NOW(),20, 8, 'hh',
            CURRENT_DATE, CURRENT_DATE + INTERVAL '30 day', NULL,
            'PENDING', 1000, CAST(NULL AS NUMERIC), CAST(NULL AS NUMERIC), 180, 1180, 0, NULL, TRUE),
            ('vet.martinez', NOW(), 'vet.martinez',
            NOW(),21, 9, 'ii',
            CURRENT_DATE, CURRENT_DATE + INTERVAL '30 day', NULL,
            'PENDING', 5000, CAST(NULL AS NUMERIC), CAST(NULL AS NUMERIC), 900, 5900, 0, NULL, TRUE),
            ('vet.sanchez', NOW(), 'vet.sanchez',
            NOW(),22, 10, 'jj',
            CURRENT_DATE, CURRENT_DATE + INTERVAL '30 day', NULL,
            'PAID', 6000, CAST(NULL AS NUMERIC), CAST(NULL AS NUMERIC), 1080, 7080, 0, NULL, TRUE)
      ) AS i(created_by, created_date, last_modified_by,
          last_modified_date, client, consultation, consultation_notes,
          issued_date, payment_date, sales_order, status, subtotal,
          discount_percentage, discount, tax, total, paid_to_date, notes, active);


-- =================================================================================================
--  INVOICE_PRODUCT TABLE
-- =================================================================================================
INSERT INTO invoice_product (invoice, product, quantity, price, amount,
                             created_by, created_date, last_modified_by,
                             last_modified_date)
SELECT i.code,
       p.product_id,
       CASE (ROW_NUMBER() OVER (PARTITION BY i.code) % 3)
           WHEN 0 THEN 2
           WHEN 1 THEN 3
           ELSE 1
           END,
       p.sales_price,
       p.sales_price * CASE (ROW_NUMBER() OVER (PARTITION BY i.code) % 3)
                           WHEN 0 THEN 2
                           WHEN 1 THEN 3
                           ELSE 1
           END,
       'admin',
       NOW(),
       'admin',
       NOW()
FROM invoices i
         CROSS JOIN (SELECT product_id, sales_price FROM products LIMIT 3) p;

-- =================================================================================================
--  APPOINTMENTS TABLE
-- =================================================================================================
INSERT INTO appointments (start_appointment_date, end_appointment_date,
                          service_type, status,
                          reason, notes, client_id, pet_id, employee_id,
                          created_at,
                          updated_at, created_by, updated_by)
SELECT NOW() + (INTERVAL '1 day' * (ROW_NUMBER() OVER ())),
       NOW() + (INTERVAL '1 day' * (ROW_NUMBER() OVER ())) +
       INTERVAL '30 minutes',
       CASE (ROW_NUMBER() OVER () % 4)
           WHEN 0 THEN 'GROOMING'
           ELSE 'MEDICAL'
           END,
       CASE (ROW_NUMBER() OVER () % 3)
           WHEN 0 THEN 'PROGRAMADA'
           WHEN 1 THEN 'EN_PROGRESO'
           ELSE 'COMPLETADA'
           END,
       CASE (ROW_NUMBER() OVER () % 4)
           WHEN 0 THEN 'Chequeo anual'
           WHEN 1 THEN 'Vacunas de refuerzo'
           WHEN 2 THEN 'Baño y corte'
           ELSE 'Urgencia médica'
           END,
       'Cita programada',
       po.owners,
       po.pet_id,
       e.employee_id,
       NOW(),
       NOW(),
       'admin',
       NULL
FROM pet_owners po
         CROSS JOIN (SELECT employee_id
                     FROM employee
                     WHERE employee_role = 'VETERINARIAN'
                     LIMIT 1) e
LIMIT 10;

-- =================================================================================================
--  PAYMENTS TABLE
-- =================================================================================================
INSERT INTO payments (payment_date, total_amount, method, status,
                      reference_number, notes,
                      created_by, created_date, last_modified_by,
                      last_modified_date)
SELECT CURRENT_DATE - (INTERVAL '1 day' * (ROW_NUMBER() OVER ())),
       CASE (ROW_NUMBER() OVER () % 3)
           WHEN 0 THEN 500.00
           WHEN 1 THEN 750.00
           ELSE 1000.00
           END,
       CASE (ROW_NUMBER() OVER () % 4)
           WHEN 0 THEN 'EFECTIVO'
           WHEN 1 THEN 'TARJETA'
           WHEN 2 THEN 'TRANSFERENCIA'
           ELSE 'CHEQUE'
           END,
       'COMPLETADO',
       'REF-' || LPAD((ROW_NUMBER() OVER ())::text, 6, '0'),
       'Pago procesado correctamente',
       'admin',
       NOW(),
       'admin',
       NOW()
FROM generate_series(1, 5);

-- =================================================================================================
--  PAYMENTS_DETAIL TABLE
-- =================================================================================================
INSERT INTO payments_detail (payment, invoice, amount,
                             created_by, created_date, last_modified_by,
                             last_modified_date)
SELECT p.code,
       i.code,
       LEAST(p.total_amount, i.total),
       'admin',
       NOW(),
       'admin',
       NOW()
FROM payments p
         CROSS JOIN invoices i
WHERE p.code <= (SELECT COUNT(*) FROM invoices)
LIMIT 3;