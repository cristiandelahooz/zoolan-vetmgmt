--   ─────▀▄▀─────▄─────▄
--   ──▄███████▄──▀██▄██▀
--   ▄█████▀█████▄──▄█
--   ███████▀████████▀
--   ─▄▄▄▄▄▄███████▀

-- Data for users table (40 rows)
INSERT INTO users (user_id, username, password, email, first_name, last_name, phone_number, birth_date, gender, nationality, province, municipality, sector, street_address, reference_points, profile_picture_url, active, created_at, updated_at, system_role) VALUES
(1, 'john.doe', 'hashed_password_1', 'john.doe@example.com', 'John', 'Doe', '8092223333', '1985-01-15', 'MASCULINO', 'Dominican', 'Santo Domingo', 'Distrito Nacional', 'Naco', 'Calle Principal 123', 'Near the park', NULL, TRUE, NOW(), NOW(), 'USER'),
(2, 'jane.smith', 'hashed_password_2', 'jane.smith@example.com', 'Jane', 'Smith', '8095556666', '1990-03-20', 'FEMENINO', 'Dominican', 'Santiago', 'Santiago de los Caballeros', 'Centro', 'Avenida Central 456', 'Next to the bank', NULL, TRUE, NOW(), NOW(), 'USER'),
(3, 'peter.jones', 'hashed_password_3', 'peter.jones@example.com', 'Peter', 'Jones', '8098889999', '1978-07-01', 'MASCULINO', 'Dominican', 'La Vega', 'Concepcion de La Vega', 'La Esmeralda', 'Callejon Sin Nombre 789', 'Behind the church', NULL, TRUE, NOW(), NOW(), 'USER'),
(4, 'mary.brown', 'hashed_password_4', 'mary.brown@example.com', 'Mary', 'Brown', '8091112222', '1995-11-10', 'FEMENINO', 'Dominican', 'Puerto Plata', 'San Felipe de Puerto Plata', 'Costambar', 'Carretera Principal 101', 'Near the beach', NULL, TRUE, NOW(), NOW(), 'USER'),
(5, 'david.wilson', 'hashed_password_5', 'david.wilson@example.com', 'David', 'Wilson', '8094445555', '1982-02-28', 'MASCULINO', 'Dominican', 'San Cristobal', 'San Cristobal', 'Pueblo Nuevo', 'Callejon de la Paz 202', 'Near the school', NULL, TRUE, NOW(), NOW(), 'USER'),
(6, 'susan.davis', 'hashed_password_6', 'susan.davis@example.com', 'Susan', 'Davis', '8297778888', '1998-09-05', 'FEMENINO', 'Dominican', 'La Romana', 'La Romana', 'Villa Hermosa', 'Avenida del Sol 303', 'In front of the market', NULL, TRUE, NOW(), NOW(), 'USER'),
(7, 'robert.miller', 'hashed_password_7', 'robert.miller@example.com', 'Robert', 'Miller', '8290001111', '1975-04-12', 'MASCULINO', 'Dominican', 'Duarte', 'San Francisco de Macoris', 'Los Rieles', 'Callejon de la Luna 404', 'Next to the river', NULL, TRUE, NOW(), NOW(), 'USER'),
(8, 'linda.garcia', 'hashed_password_8', 'linda.garcia@example.com', 'Linda', 'Garcia', '8293334444', '1988-06-18', 'FEMENINO', 'Dominican', 'Espaillat', 'Moca', 'Salitre', 'Carretera Vieja 505', 'Near the farm', NULL, TRUE, NOW(), NOW(), 'USER'),
(9, 'william.rodriguez', 'hashed_password_9', 'william.rodriguez@example.com', 'William', 'Rodriguez', '8296667777', '1993-10-25', 'MASCULINO', 'Dominican', 'Peravia', 'Bani', 'El Fundo', 'Calle Principal 606', 'Near the church', NULL, TRUE, NOW(), NOW(), 'USER'),
(10, 'patricia.martinez', 'hashed_password_10', 'patricia.martinez@example.com', 'Patricia', 'Martinez', '829-999-0000', '1980-12-30', 'FEMENINO', 'Dominican', 'Azua', 'Azua de Compostela', 'La Bombita', 'Avenida Central 707', 'Next to the school', NULL, TRUE, NOW(), NOW(), 'USER'),
(11, 'james.hernandez', 'hashed_password_11', 'james.hernandez@example.com', 'James', 'Hernandez', '809-111-1111', '1987-01-01', 'MASCULINO', 'Dominican', 'Barahona', 'Santa Cruz de Barahona', 'Villa Central', 'Callejon Sin Nombre 808', 'Behind the hospital', NULL, TRUE, NOW(), NOW(), 'USER'),
(12, 'elizabeth.lopez', 'hashed_password_12', 'elizabeth.lopez@example.com', 'Elizabeth', 'Lopez', '809-222-2222', '1992-02-02', 'FEMENINO', 'Dominican', 'Monte Cristi', 'San Fernando de Monte Cristi', 'Las Matas', 'Carretera Principal 909', 'Near the river', NULL, TRUE, NOW(), NOW(), 'USER'),
(13, 'charles.gonzalez', 'hashed_password_13', 'charles.gonzalez@example.com', 'Charles', 'Gonzalez', '809-333-3333', '1979-03-03', 'MASCULINO', 'Dominican', 'Samaná', 'Santa Barbara de Samana', 'El Catey', 'Avenida del Sol 1010', 'In front of the hotel', NULL, TRUE, NOW(), NOW(), 'USER'),
(14, 'nancy.perez', 'hashed_password_14', 'nancy.perez@example.com', 'Nancy', 'Perez', '809-444-4444', '1996-04-04', 'FEMENINO', 'Dominican', 'San Juan', 'San Juan de la Maguana', 'Hato Nuevo', 'Callejon de la Paz 1111', 'Near the church', NULL, TRUE, NOW(), NOW(), 'USER'),
(15, 'thomas.sanchez', 'hashed_password_15', 'thomas.sanchez@example.com', 'Thomas', 'Sanchez', '809-555-5555', '1983-05-05', 'MASCULINO', 'Dominican', 'Valverde', 'Mao', 'Esperanza', 'Calle Principal 1212', 'Next to the park', NULL, TRUE, NOW(), NOW(), 'USER'),
(16, 'margaret.ramirez', 'hashed_password_16', 'margaret.ramirez@example.com', 'Margaret', 'Ramirez', '829-666-6666', '1991-06-06', 'FEMENINO', 'Dominican', 'Hermanas Mirabal', 'Salcedo', 'Tenares', 'Avenida Central 1313', 'Behind the school', NULL, TRUE, NOW(), NOW(), 'USER'),
(17, 'daniel.cruz', 'hashed_password_17', 'daniel.cruz@example.com', 'Daniel', 'Cruz', '829-777-7777', '1976-07-07', 'MASCULINO', 'Dominican', 'Monte Plata', 'Monte Plata', 'Bayaguana', 'Callejon Sin Nombre 1414', 'Near the river', NULL, TRUE, NOW(), NOW(), 'USER'),
(18, 'dorothy.morales', 'hashed_password_18', 'dorothy.morales@example.com', 'Dorothy', 'Morales', '829-888-8888', '1994-08-08', 'FEMENINO', 'Dominican', 'Hato Mayor', 'Hato Mayor del Rey', 'Sabana de la Mar', 'Carretera Principal 1515', 'Near the farm', NULL, TRUE, NOW(), NOW(), 'USER'),
(19, 'paul.gomez', 'hashed_password_19', 'paul.gomez@example.com', 'Paul', 'Gomez', '829-999-9999', '1981-09-09', 'MASCULINO', 'Dominican', 'El Seibo', 'Santa Cruz de El Seibo', 'Miches', 'Avenida del Sol 1616', 'In front of the market', NULL, TRUE, NOW(), NOW(), 'USER'),
(20, 'sandra.diaz', 'hashed_password_20', 'sandra.diaz@example.com', 'Sandra', 'Diaz', '829-000-0000', '1989-10-10', 'FEMENINO', 'Dominican', 'Independencia', 'Jimaní', 'Duverge', 'Callejon de la Luna 1717', 'Next to the bank', NULL, TRUE, NOW(), NOW(), 'USER'),
(21, 'mark.alvarez', 'hashed_password_21', 'mark.alvarez@example.com', 'Mark', 'Alvarez', '849-456-7890', '1984-11-11', 'MASCULINO', 'Dominican', 'Dajabón', 'Dajabon', 'Loma de Cabrera', 'Calle Principal 1818', 'Near the park', NULL, TRUE, NOW(), NOW(), 'USER'),
(22, 'sharon.ruiz', 'hashed_password_22', 'sharon.ruiz@example.com', 'Sharon', 'Ruiz', '849-654-3210', '1997-12-12', 'FEMENINO', 'Dominican', 'Baoruco', 'Neiba', 'Tamayo', 'Avenida Central 1919', 'Next to the school', NULL, TRUE, NOW(), NOW(), 'USER'),
(23, 'george.torres', 'hashed_password_23', 'george.torres@example.com', 'George', 'Torres', '849-233-4455', '1977-01-13', 'MASCULINO', 'Dominican', 'Elías Piña', 'Comendador', 'Bánica', 'Callejon Sin Nombre 2020', 'Behind the church', NULL, TRUE, NOW(), NOW(), 'USER'),
(24, 'betty.flores', 'hashed_password_24', 'betty.flores@example.com', 'Betty', 'Flores', '849-677-8899', '1990-02-14', 'FEMENINO', 'Dominican', 'San José de Ocoa', 'San Jose de Ocoa', 'Rancho Arriba', 'Carretera Principal 2121', 'Near the river', NULL, TRUE, NOW(), NOW(), 'USER'),
(25, 'kenneth.rivera', 'hashed_password_25', 'kenneth.rivera@example.com', 'Kenneth', 'Rivera', '849-788-9900', '1986-03-15', 'MASCULINO', 'Dominican', 'Monseñor Nouel', 'Bonao', 'Piedra Blanca', 'Avenida del Sol 2222', 'In front of the market', NULL, TRUE, NOW(), NOW(), 'USER'),
(26, 'helen.guzman', 'hashed_password_26', 'helen.guzman@example.com', 'Helen', 'Guzman', '849-899-0011', '1999-04-16', 'FEMENINO', 'Dominican', 'Hermanas Mirabal', 'Salcedo', 'Villa Tapia', 'Callejon de la Luna 2323', 'Next to the bank', NULL, TRUE, NOW(), NOW(), 'USER'),
(27, 'steven.pena', 'hashed_password_27', 'steven.pena@example.com', 'Steven', 'Pena', '849-900-1122', '1974-05-17', 'MASCULINO', 'Dominican', 'María Trinidad Sánchez', 'Nagua', 'Cabrera', 'Calle Principal 2424', 'Near the park', NULL, TRUE, NOW(), NOW(), 'USER'),
(28, 'kimberly.castro', 'hashed_password_28', 'kimberly.castro@example.com', 'Kimberly', 'Castro', '849-011-2233', '1992-06-18', 'FEMENINO', 'Dominican', 'Sánchez Ramírez', 'Cotuí', 'Fantino', 'Avenida Central 2525', 'Next to the school', NULL, TRUE, NOW(), NOW(), 'USER'),
(29, 'edward.rojas', 'hashed_password_29', 'edward.rojas@example.com', 'Edward', 'Rojas', '849-122-3344', '1980-07-19', 'MASCULINO', 'Dominican', 'San Pedro de Macorís', 'San Pedro de Macoris', 'Consuelo', 'Callejon Sin Nombre 2626', 'Behind the church', NULL, TRUE, NOW(), NOW(), 'USER'),
(30, 'donna.soto', 'hashed_password_30', 'donna.soto@example.com', 'Donna', 'Soto', '849-233-4455', '1987-08-20', 'FEMENINO', 'Dominican', 'Santiago Rodríguez', 'San Ignacio de Sabaneta', 'Moncion', 'Carretera Principal 2727', 'Near the river', NULL, TRUE, NOW(), NOW(), 'USER'),
(31, 'joseph.vargas', 'hashed_password_31', 'joseph.vargas@example.com', 'Joseph', 'Vargas', '849-344-5566', '1985-09-21', 'MASCULINO', 'Dominican', 'Santo Domingo', 'Santo Domingo Norte', 'Villa Mella', 'Avenida del Sol 2828', 'In front of the hotel', NULL, TRUE, NOW(), NOW(), 'USER'),
(32, 'michelle.reyes', 'hashed_password_32', 'michelle.reyes@example.com', 'Michelle', 'Reyes', '849-455-6677', '1993-10-22', 'FEMENINO', 'Dominican', 'Santo Domingo', 'Santo Domingo Oeste', 'Manoguayabo', 'Callejon de la Paz 2929', 'Near the church', NULL, TRUE, NOW(), NOW(), 'USER'),
(33, 'ryan.mendez', 'hashed_password_33', 'ryan.mendez@example.com', 'Ryan', 'Mendez', '849-566-7788', '1978-11-23', 'MASCULINO', 'Dominican', 'Santo Domingo', 'Santo Domingo Este', 'Los Mina', 'Calle Principal 3030', 'Next to the park', NULL, TRUE, NOW(), NOW(), 'USER'),
(34, 'jessica.nuñez', 'hashed_password_34', 'jessica.nuñez@example.com', 'Jessica', 'Nuñez', '829-677-8899', '1996-12-24', 'FEMENINO', 'Dominican', 'Santo Domingo', 'Boca Chica', 'Andres', 'Avenida Central 3131', 'Behind the school', NULL, TRUE, NOW(), NOW(), 'USER'),
(35, 'gary.guerrero', 'hashed_password_35', 'gary.guerrero@example.com', 'Gary', 'Guerrero', '829-788-9900', '1982-01-25', 'MASCULINO', 'Dominican', 'Santo Domingo', 'Pedro Brand', 'La Cuaba', 'Callejon Sin Nombre 3232', 'Near the river', NULL, TRUE, NOW(), NOW(), 'USER'),
(36, 'cynthia.santana', 'hashed_password_36', 'cynthia.santana@example.com', 'Cynthia', 'Santana', '809-899-0011', '1990-02-26', 'FEMENINO', 'Dominican', 'Santo Domingo', 'San Antonio de Guerra', 'Hato Viejo', 'Carretera Principal 3333', 'Near the farm', NULL, TRUE, NOW(), NOW(), 'USER'),
(37, 'larry.castillo', 'hashed_password_37', 'larry.castillo@example.com', 'Larry', 'Castillo', '809-900-1122', '1975-03-27', 'MASCULINO', 'Dominican', 'Santo Domingo', 'Los Alcarrizos', 'Pantoja', 'Avenida del Sol 3434', 'In front of the market', NULL, TRUE, NOW(), NOW(), 'USER'),
(38, 'angela.herrera', 'hashed_password_38', 'angela.herrera@example.com', 'Angela', 'Herrera', '809-011-2233', '1998-04-28', 'FEMENINO', 'Dominican', 'Santo Domingo', 'Santo Domingo Este', 'San Isidro', 'Callejon de la Luna 3535', 'Next to the bank', NULL, TRUE, NOW(), NOW(), 'USER'),
(39, 'frank.dominguez', 'hashed_password_39', 'frank.dominguez@example.com', 'Frank', 'Dominguez', '849-122-3344', '1981-05-29', 'MASCULINO', 'Dominican', 'Santo Domingo', 'Santo Domingo Norte', 'Sabana Perdida', 'Calle Principal 3636', 'Near the park', NULL, TRUE, NOW(), NOW(), 'USER'),
(40, 'brenda.vasquez', 'hashed_password_40', 'brenda.vasquez@example.com', 'Brenda', 'Vasquez', '849-233-4455', '1989-06-30', 'FEMENINO', 'Dominican', 'Santo Domingo', 'Santo Domingo Oeste', 'Palmarejo', 'Avenida Central 3737', 'Next to the school', NULL, TRUE, NOW(), NOW(), 'USER');
--
--
-- -- Data for employee table (5 rows)
INSERT INTO employee (employee_id, employee_role, salary, hire_date, available, work_schedule, emergency_contact_name, emergency_contact_phone) VALUES
(1, 'VETERINARIAN', 80000.00, '2022-01-15', TRUE, 'Mon-Fri 9-5', 'Jane Doe', '8092223333'),
(2, 'VETERINARIAN', 75000.00, '2023-03-01', TRUE, 'Mon-Sat 8-4', 'John Smith', '8095556666'),
(3, 'RECEPTIONIST', 40000.00, '2023-06-20', TRUE, 'Mon-Fri 8-5', 'Peter Jones', '8098889999'),
(4, 'VETERINARIAN', 78000.00, '2022-10-01', TRUE, 'Tue-Sat 9-5', 'Alice Brown', '8091112222'),
(5, 'RECEPTIONIST', 42000.00, '2024-01-10', TRUE, 'Mon-Fri 9-6', 'Bob White', '8094445555');

-- -- Data for client table (40 rows, matching user_id)
INSERT INTO client (client_id, cedula, passport, rnc, company_name,
                    preferred_contact_method, emergency_contact_name,
                    emergency_contact_number, rating, credit_limit,
                    current_balance, payment_terms_days, notes,
                    reference_source, verified)
VALUES (6, '67890123456', NULL, NULL, NULL, 'SMS', 'Emergency Contact 6',
        '849-777-8888', 'MUY_BUENO', 1500.00, 0.00, 30, 'VIP client',
        'RECOMENDACION_PROFESIONAL', TRUE),
       (7, '78901234567', NULL, NULL, NULL, 'EMAIL', 'Emergency Contact 7',
        '849-000-1111', 'BUENO', 700.00, 20.00, 15, 'Always on time', 'OTRO',
        TRUE),
       (8, '89012345678', NULL, NULL, NULL, 'EMAIL', 'Emergency Contact 8',
        '849-333-4444', 'REGULAR', 400.00, 80.00, 7, 'Needs reminders',
        'RECOMENDACION_PROFESIONAL', TRUE),
       (9, '90123456789', NULL, NULL, NULL, 'EMAIL', 'Emergency Contact 9',
        '849-666-7777', 'MUY_BUENO', 1100.00, 0.00, 30, 'Prefers specific vet',
        'PASANTE', TRUE),
       (10, '01234567890', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 10', '829-999-0000', 'BUENO', 600.00, 10.00, 20,
        'Travels frequently', 'GOOGLE', TRUE),
       (11, '11234567890', NULL, NULL, NULL, 'WHATSAPP', 'Emergency Contact 11',
        '829-111-1111', 'MUY_BUENO', 900.00, 0.00, 30, 'Calm pets', 'GOOGLE',
        TRUE),
       (12, '18345678901', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 12', '829-222-2222', 'BUENO', 450.00, 30.00, 15,
        'Anxious pets', 'GOOGLE', TRUE),
       (13, '13456789012', NULL, NULL, NULL, 'WHATSAPP', 'Emergency Contact 13',
        '829-333-3333', 'MUY_BUENO', 1300.00, 0.00, 30,
        'Prefers evening appointments', 'REFERIDO_CLIENTE', TRUE),
       (14, '14567890123', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 14', '849-444-4444', 'REGULAR', 250.00, 70.00, 7,
        'Always brings treats', 'REDES_SOCIALES', TRUE),
       (15, '15678901234', NULL, NULL, NULL, 'EMAIL', 'Emergency Contact 15',
        '849-555-5555', 'BUENO', 750.00, 0.00, 20, 'Very organized',
        'REDES_SOCIALES', TRUE),
       (16, '16789012345', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 16', '829-666-6666', 'MUY_BUENO', 1400.00, 0.00, 30,
        'Loves to chat', 'REFERIDO_CLIENTE', TRUE),
       (17, '17890123456', NULL, NULL, NULL, 'WHATSAPP', 'Emergency Contact 17',
        '809-777-7777', 'BUENO', 650.00, 15.00, 15, 'Quick visits',
        'REDES_SOCIALES', TRUE),
       (18, '18901234567', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 18', '809-888-8888', 'REGULAR', 350.00, 90.00, 7,
        'Last minute appointments', 'PUBLICIDAD', TRUE),
       (19, '19012345678', NULL, NULL, NULL, 'EMAIL', 'Emergency Contact 19',
        '809-999-9999', 'MUY_BUENO', 1050.00, 0.00, 30,
        'Prefers online booking', 'PUBLICIDAD', TRUE),
       (20, '20123456789', NULL, NULL, NULL, 'WHATSAPP', 'Emergency Contact 20',
        '829-000-0000', 'BUENO', 550.00, 25.00, 20, 'Always asks questions',
        'REDES_SOCIALES', TRUE),
       (21, '21234567890', NULL, NULL, NULL, 'EMAIL', 'Emergency Contact 21',
        '829-456-7890', 'PAGO_TARDIO', 950.00, 0.00, 30, 'Very patient',
        'REDES_SOCIALES', TRUE),
       (22, '22345678901', NULL, NULL, NULL, 'EMAIL', 'Emergency Contact 22',
        '829-654-3210', 'BUENO', 400.00, 40.00, 15, 'Always on time',
        'REDES_SOCIALES', TRUE),
       (23, '23456789012', NULL, NULL, NULL, 'EMAIL', 'Emergency Contact 23',
        '829-233-4455', 'REGULAR', 1150.00, 0.00, 30, 'Prefers specific vet',
        'REFERIDO_CLIENTE', TRUE),
       (24, '24567890123', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 24', '849-677-8899', 'REGULAR', 300.00, 60.00, 7,
        'Has multiple pets', 'REFERIDO_CLIENTE', TRUE),
       (25, '25678901234', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 25', '829-788-9900', 'BUENO', 850.00, 0.00, 20,
        'Sensitive pet', 'PUBLICIDAD', TRUE),
       (26, '26789012345', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 26', '849-899-0011', 'MUY_BUENO', 1600.00, 0.00, 30,
        'VIP client', 'PUBLICIDAD', TRUE),
       (27, '27890123456', NULL, NULL, NULL, 'WHATSAPP', 'Emergency Contact 27',
        '829-900-1122', 'BUENO', 700.00, 20.00, 15, 'Always on time',
        'PUBLICIDAD', TRUE),
       (28, '28901234567', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 28', '829-011-2233', 'PAGO_TARDIO', 400.00, 80.00, 7,
        'Needs reminders', 'REFERIDO_CLIENTE', TRUE),
       (29, '29012345678', NULL, NULL, NULL, 'WHATSAPP', 'Emergency Contact 29',
        '849-122-3344', 'MUY_BUENO', 1200.00, 0.00, 30, 'Prefers specific vet',
        'REFERIDO_CLIENTE', TRUE),
       (30, '30123456789', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 30', '849-233-4455', 'BUENO', 600.00, 10.00, 20,
        'Travels frequently', 'REFERIDO_CLIENTE', TRUE),
       (31, '31234567890', NULL, NULL, NULL, 'WHATSAPP', 'Emergency Contact 31',
        '849-344-5566', 'MUY_BUENO', 1000.00, 0.00, 30, 'Calm pets', 'GOOGLE',
        TRUE),
       (32, '32345678901', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 32', '809-455-6677', 'BUENO', 500.00, 50.00, 15,
        'Anxious pets', 'GOOGLE', TRUE),
       (33, '33456789012', NULL, NULL, NULL, 'WHATSAPP', 'Emergency Contact 33',
        '829-566-7788', 'PAGO_TARDIO', 1300.00, 0.00, 30,
        'Prefers evening appointments', 'GOOGLE', TRUE),
       (34, '84567890123', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 34', '809-677-8899', 'CONFLICTIVO', 300.00, 100.00,
        7, 'Always brings treats', 'GOOGLE', TRUE),
       (35, '35678901234', NULL, NULL, NULL, 'SMS', 'Emergency Contact 35',
        '849-788-9900', 'BUENO', 800.00, 0.00, 20, 'Very organized', 'GOOGLE',
        TRUE),
       (36, '36789012345', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 36', '849-899-0011', 'MUY_BUENO', 1500.00, 0.00, 30,
        'Loves to chat', 'PASANTE', TRUE),
       (37, '37890123456', NULL, NULL, NULL, 'SMS', 'Emergency Contact 37',
        '849-900-1122', 'BUENO', 700.00, 20.00, 15, 'Quick visits', 'PASANTE',
        TRUE),
       (38, '38901234567', NULL, NULL, NULL, 'PHONE_CALL',
        'Emergency Contact 38', '809-011-2233', 'CONFLICTIVO', 400.00, 80.00, 7,
        'Last minute appointments', 'PASANTE', TRUE),
       (39, '39012345678', NULL, NULL, NULL, 'SMS', 'Emergency Contact 39',
        '849-122-3344', 'BUENO', 1100.00, 0.00, 30, 'Prefers online booking',
        'REFERIDO_CLIENTE', TRUE),
       (40, '40123456789', NULL, NULL, NULL, 'WHATSAPP', 'Emergency Contact 40',
        '829-233-4455', 'BUENO', 600.00, 10.00, 20, 'Always asks questions',
        'PASANTE', TRUE);
--
-- -- Data for pets table (40 rows)
INSERT INTO pets (id, name, type, breed, birth_date, gender, active, color, size, fur_type) VALUES
(1, 'Milo', 'PERRO', 'Yorkshire Terrier', '2020-05-10', 'MASCULINO', TRUE, 'Marrón', 'PEQUEÑO', 'LARGO'),
(2, 'Lucy', 'GATO', 'Persa', '2021-01-15', 'FEMENINO', TRUE, 'Blanco', 'MEDIANO', 'LARGO'),
(3, 'Max', 'PERRO', 'Golden Retriever', '2019-11-22', 'MASCULINO', TRUE, 'Dorado', 'GRANDE', 'LARGO'),
(4, 'Bella', 'PERRO', 'Pastor Alemán', '2022-08-01', 'FEMENINO', TRUE, 'Negro y fuego', 'GRANDE', 'CORTO'),
(5, 'Rocky', 'PERRO', 'Bulldog Francés', '2018-03-12', 'MASCULINO', TRUE, 'Gris', 'MEDIANO', 'CORTO'),
(6, 'Misty', 'GATO', 'Persa', '2022-09-20', 'FEMENINO', TRUE, 'Gris claro', 'MEDIANO', 'LARGO'),
(7, 'Duke', 'PERRO', 'Poodle', '2021-07-11', 'MASCULINO', TRUE, 'Blanco', 'MEDIANO', 'LARGO'),
(8, 'Coco', 'GATO', 'Persa', '2023-01-05', 'FEMENINO', TRUE, 'Beige', 'MEDIANO', 'LARGO'),
(9, 'Zeus', 'PERRO', 'Rottweiler', '2020-02-28', 'MASCULINO', TRUE, 'Negro y fuego', 'GRANDE', 'CORTO'),
(10, 'Sasha', 'PERRO', 'Yorkshire Terrier', '2022-11-15', 'FEMENINO', TRUE, 'Marrón', 'PEQUEÑO', 'LARGO');
-- (11, 'Oliver', 'Cat', 'Domestic Shorthair', '2023-03-01', 'MALE', TRUE),
-- (12, 'Daisy', 'Dog', 'Beagle', '2022-04-10', 'FEMALE', TRUE),
-- (13, 'Leo', 'Cat', 'Ragdoll', '2021-05-20', 'MALE', TRUE),
-- (14, 'Zoe', 'Dog', 'Dachshund', '2020-06-30', 'FEMALE', TRUE),
-- (15, 'Milo', 'Cat', 'Bengal', '2023-07-01', 'MALE', TRUE),
-- (16, 'Ruby', 'Dog', 'Australian Shepherd', '2022-08-10', 'FEMALE', TRUE),
-- (17, 'Jasper', 'Cat', 'Sphynx', '2021-09-20', 'MALE', TRUE),
-- (18, 'Penny', 'Dog', 'Corgi', '2020-10-30', 'FEMALE', TRUE),
-- (19, 'Simba', 'Cat', 'Persian', '2023-11-01', 'MALE', TRUE),
-- (20, 'Luna', 'Dog', 'Golden Retriever', '2022-12-10', 'FEMALE', TRUE),
-- (21, 'Maxine', 'Dog', 'Pug', '2023-03-01', 'FEMALE', TRUE),
-- (22, 'Buddy Jr.', 'Cat', 'Tabby', '2022-04-10', 'MALE', TRUE),
-- (23, 'Charlie', 'Dog', 'Bulldog', '2021-05-20', 'MALE', TRUE),
-- (24, 'Daisy Mae', 'Cat', 'Calico', '2020-06-30', 'FEMALE', TRUE),
-- (25, 'Cooper', 'Dog', 'Shih Tzu', '2023-07-01', 'MALE', TRUE),
-- (26, 'Lucy Lu', 'Cat', 'Siamese', '2022-08-10', 'FEMALE', TRUE),
-- (27, 'Rocky Balboa', 'Dog', 'Rottweiler', '2021-09-20', 'MALE', TRUE),
-- (28, 'Molly', 'Cat', 'Persian', '2020-10-30', 'FEMALE', TRUE),
-- (29, 'Bear', 'Dog', 'Chihuahua', '2023-11-01', 'MALE', TRUE),
-- (30, 'Kitty', 'Cat', 'Domestic Longhair', '2022-12-10', 'FEMALE', TRUE),
-- (31, 'Shadow', 'Dog', 'Border Collie', '2019-02-01', 'MALE', TRUE),
-- (32, 'Cleo', 'Cat', 'Bengal', '2020-03-15', 'FEMALE', TRUE),
-- (33, 'Apollo', 'Dog', 'Doberman', '2021-04-20', 'MALE', TRUE),
-- (34, 'Willow', 'Cat', 'Ragdoll', '2022-05-25', 'FEMALE', TRUE),
-- (35, 'Gus', 'Dog', 'Basset Hound', '2018-06-30', 'MALE', TRUE),
-- (36, 'Hazel', 'Cat', 'Siberian', '2019-07-01', 'FEMALE', TRUE),
-- (37, 'Finn', 'Dog', 'Irish Setter', '2020-08-05', 'MALE', TRUE),
-- (38, 'Piper', 'Cat', 'Russian Blue', '2021-09-10', 'FEMALE', TRUE),
-- (39, 'Koda', 'Dog', 'Akita', '2022-10-15', 'MALE', TRUE),
-- (40, 'Nala', 'Cat', 'Abyssinian', '2023-11-20', 'FEMALE', TRUE);
--
-- -- Data for pet_owners table (40 rows)
INSERT INTO pet_owners (client_id, pet_id)
VALUES (6, 1),
       (7, 2),
       (8, 3),
       (9, 4),
       (10, 5),
       (11, 6),
       (12, 7),
       (13, 8),
       (14, 9),
       (15, 10);
-- (6, 11), (6, 12), (7, 13), (7, 14), (8, 15), (8, 16), (9, 17), (9, 18), (10, 19), (10, 20),
-- (11, 21), (11, 22), (12, 23), (12, 24), (13, 25), (13, 26), (14, 27), (14, 28), (15, 29), (15, 30),
-- (16, 31), (16, 32), (17, 33), (17, 34), (18, 35), (18, 36), (19, 37), (19, 38), (20, 39), (20, 40);
--
-- -- Data for medical_histories table (40 rows)
INSERT INTO medical_histories (id, pet_id, allergies, medications, vaccinations,
                               surgeries, chronic_conditions, notes, created_at,
                               updated_at, active)
VALUES (1, 1, 'Pollen', 'None', 'Up to date', 'None', 'None',
        'Healthy and active.', NOW(), NOW(), TRUE),
       (2, 2, 'None', 'Flea treatment', 'Up to date', 'Spayed', 'None',
        'Sensitive stomach.', NOW(), NOW(), TRUE),
       (3, 3, 'None', 'None', 'Up to date', 'None', 'Hip dysplasia',
        'Requires joint supplements.', NOW(), NOW(), TRUE),
       (4, 4, 'Chicken', 'None', 'Up to date', 'None', 'None', 'N/A', NOW(),
        NOW(), TRUE),
       (5, 5, 'Grain', 'None', 'Up to date', 'None', 'None', 'Energetic.',
        NOW(), NOW(), TRUE),
       (6, 6, 'None', 'Dewormer', 'Needs boosters', 'None', 'None',
        'Shy but friendly.', NOW(), NOW(), TRUE),
       (7, 7, 'Dust', 'Joint supplements', 'Up to date', 'None', 'Arthritis',
        'Requires daily medication.', NOW(), NOW(), TRUE),
       (8, 8, 'None', 'None', 'Up to date', 'None', 'None',
        'Playful and curious.', NOW(), NOW(), TRUE),
       (9, 9, 'Beef', 'None', 'Up to date', 'None', 'Sensitive skin',
        'Needs special diet.', NOW(), NOW(), TRUE),
       (10, 10, 'None', 'Heartworm prevention', 'Up to date', 'None', 'None',
        'Very active.', NOW(), NOW(), TRUE);
-- (11, 11, 'None', 'None', 'Up to date', 'None', 'None', 'Loves to cuddle.', NOW(), NOW(), TRUE),
-- (12, 12, 'Grass', 'None', 'Up to date', 'None', 'Seasonal allergies', 'Sneezes in spring.', NOW(), NOW(), TRUE),
-- (13, 13, 'None', 'None', 'Needs boosters', 'None', 'None', 'Quiet and observant.', NOW(), NOW(), TRUE),
-- (14, 14, 'Pollen', 'None', 'Up to date', 'None', 'None', 'Very friendly.', NOW(), NOW(), TRUE),
-- (15, 15, 'None', 'None', 'Up to date', 'None', 'None', 'Always exploring.', NOW(), NOW(), TRUE),
-- (16, 16, 'Fleas', 'Flea prevention', 'Up to date', 'None', 'None', 'Loves to run.', NOW(), NOW(), TRUE),
-- (17, 17, 'None', 'None', 'Needs boosters', 'None', 'None', 'Very vocal.', NOW(), NOW(), TRUE),
-- (18, 18, 'Peanuts', 'None', 'Up to date', 'None', 'None', 'Enjoys walks.', NOW(), NOW(), TRUE),
-- (19, 19, 'None', 'None', 'Up to date', 'None', 'None', 'Calm and gentle.', NOW(), NOW(), TRUE),
-- (20, 20, 'Bee stings', 'None', 'Up to date', 'None', 'None', 'Loves to play fetch.', NOW(), NOW(), TRUE),
-- (21, 21, 'None', 'None', 'Up to date', 'None', 'None', 'Very playful.', NOW(), NOW(), TRUE),
-- (22, 22, 'Dust', 'None', 'Up to date', 'None', 'None', 'Loves to nap.', NOW(), NOW(), TRUE),
-- (23, 23, 'Pollen', 'None', 'Needs boosters', 'None', 'None', 'Friendly with everyone.', NOW(), NOW(), TRUE),
-- (24, 24, 'None', 'None', 'Up to date', 'None', 'None', 'Quiet and sweet.', NOW(), NOW(), TRUE),
-- (25, 25, 'Grass', 'None', 'Up to date', 'None', 'None', 'Loves attention.', NOW(), NOW(), TRUE),
-- (26, 26, 'None', 'None', 'Needs boosters', 'None', 'None', 'Very curious.', NOW(), NOW(), TRUE),
-- (27, 27, 'Fleas', 'None', 'Up to date', 'None', 'None', 'Protective.', NOW(), NOW(), TRUE),
-- (28, 28, 'None', 'None', 'Up to date', 'None', 'None', 'Loves to be groomed.', NOW(), NOW(), TRUE),
-- (29, 29, 'Peanuts', 'None', 'Needs boosters', 'None', 'None', 'Small but mighty.', NOW(), NOW(), TRUE),
-- (30, 30, 'None', 'None', 'Up to date', 'None', 'None', 'Very independent.', NOW(), NOW(), TRUE),
-- (31, 31, 'None', 'None', 'Up to date', 'None', 'None', 'Loves to explore.', NOW(), NOW(), TRUE),
-- (32, 32, 'Dust', 'None', 'Needs boosters', 'None', 'None', 'Very affectionate.', NOW(), NOW(), TRUE),
-- (33, 33, 'Pollen', 'None', 'Up to date', 'None', 'None', 'Energetic and playful.', NOW(), NOW(), TRUE),
-- (34, 34, 'None', 'None', 'Up to date', 'None', 'None', 'Calm and gentle.', NOW(), NOW(), TRUE),
-- (35, 35, 'Grass', 'None', 'Needs boosters', 'None', 'None', 'Loves to dig.', NOW(), NOW(), TRUE),
-- (36, 36, 'None', 'None', 'Up to date', 'None', 'None', 'Very agile.', NOW(), NOW(), TRUE),
-- (37, 37, 'Fleas', 'None', 'Up to date', 'None', 'None', 'Loves to retrieve.', NOW(), NOW(), TRUE),
-- (38, 38, 'None', 'None', 'Needs boosters', 'None', 'None', 'Very intelligent.', NOW(), NOW(), TRUE),
-- (39, 39, 'Peanuts', 'None', 'Up to date', 'None', 'None', 'Loyal and protective.', NOW(), NOW(), TRUE),
-- (40, 40, 'None', 'None', 'Up to date', 'None', 'None', 'Graceful and elegant.', NOW(), NOW(), TRUE);
--
-- -- Data for consultations table (10 rows)
INSERT INTO consultations (id, notes, diagnosis, treatment, prescription,
                           consultation_date, pet_id, veterinarian_id,
                           medical_history_id, created_at, updated_at, active)
VALUES (1, 'Annual check-up.', 'Healthy', 'N/A', 'N/A', '2024-06-01 10:00:00',
        1, 2, 1, NOW(), NOW(), TRUE),
       (2, 'Vomiting and lethargy.', 'Gastroenteritis',
        'Fluid therapy and anti-nausea medication.', 'Metronidazole',
        '2024-06-15 11:30:00', 2, 4, 2, NOW(), NOW(), TRUE),
       (3, 'Limps on right hind leg.', 'Mild arthritis',
        'Pain medication and rest.', 'Carprofen', '2024-07-01 09:00:00', 3, 2,
        3, NOW(), NOW(), TRUE),
       (4, 'Skin rash and itching.', 'Allergic dermatitis',
        'Antihistamines and topical cream.', 'Diphenhydramine',
        '2024-07-05 14:00:00', 4, 4, 4, NOW(), NOW(), TRUE),
       (5, 'Coughing and sneezing.', 'Upper respiratory infection',
        'Antibiotics and cough suppressant.', 'Amoxicillin',
        '2024-07-10 10:00:00', 5, 2, 5, NOW(), NOW(), TRUE),
       (6, 'Loss of appetite.', 'Dental disease',
        'Dental cleaning recommended.', 'N/A', '2024-07-12 11:00:00', 6, 4, 6,
        NOW(), NOW(), TRUE),
       (7, 'Ear infection.', 'Otitis externa', 'Ear drops and cleaning.',
        'Otomax', '2024-07-15 15:00:00', 7, 2, 7, NOW(), NOW(), TRUE),
       (8, 'Weight loss.', 'Hyperthyroidism', 'Medication to regulate thyroid.',
        'Methimazole', '2024-07-18 09:30:00', 8, 4, 8, NOW(), NOW(), TRUE),
       (9, 'Lump on side.', 'Lipoma', 'Monitor, surgical removal if grows.',
        'N/A', '2024-07-20 13:00:00', 9, 2, 9, NOW(), NOW(), TRUE),
       (10, 'Eye discharge.', 'Conjunctivitis', 'Eye drops.', 'Tobramycin',
        '2024-07-22 10:00:00', 10, 4, 10, NOW(), NOW(), TRUE);

-- -- Data for appointments table (40 rows)
--INSERT INTO appointments (id, start_appointment_date, end_appointment_date, service_type, status, reason, notes, client_id, pet_id, employee_id, created_at, updated_at, created_by, updated_by, guest_client_name, guest_client_phone, guest_client_email) VALUES
-- (1, '2024-07-10 10:00:00', '2024-07-10 10:30:00', 'Check-up', 'PROGRAMADA', 'Annual check-up', 'N/A', 6, 1, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (2, '2024-07-11 14:00:00', '2024-07-11 14:45:00', 'Vaccination', 'PROGRAMADA', 'Booster shots', 'Needs rabies and distemper.', 7, 3, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (3, '2024-07-12 09:00:00', '2024-07-12 10:00:00', 'Grooming', 'PROGRAMADA', 'Full groom', 'Lion cut requested.', 8, 2, 3, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (4, '2024-07-15 11:00:00', '2024-07-15 11:30:00', 'Emergency', 'COMPLETADA', 'Hit by car', 'X-rays taken.', 9, 4, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (5, '2024-07-16 10:00:00', '2024-07-16 10:30:00', 'Check-up', 'PROGRAMADA', 'Itchy skin', 'N/A', 10, 5, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL);
-- (6, '2024-07-17 11:00:00', '2024-07-17 11:45:00', 'Vaccination', 'Scheduled', 'Kitten shots', 'Needs FVRCP.', 4, 7, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (7, '2024-07-18 15:00:00', '2024-07-18 15:30:00', 'Consultation', 'Scheduled', 'Lethargic', 'N/A', 3, 6, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (8, '2024-07-19 09:30:00', '2024-07-19 10:30:00', 'Grooming', 'Scheduled', 'Matting removal', 'Full body shave.', 4, 8, 3, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (9, '2024-07-20 12:00:00', '2024-07-20 12:45:00', 'Check-up', 'Scheduled', 'New patient exam', 'N/A', 5, 9, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (10, '2024-07-22 16:00:00', '2024-07-22 16:30:00', 'Emergency', 'Scheduled', 'Possible broken leg', 'X-rays needed.', 5, 10, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (11, '2024-07-23 09:00:00', '2024-07-23 09:30:00', 'Check-up', 'Scheduled', 'Routine check', 'N/A', 6, 11, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (12, '2024-07-24 10:00:00', '2024-07-24 10:45:00', 'Vaccination', 'Scheduled', 'Annual shots', 'Needs DHPP.', 7, 13, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (13, '2024-07-25 11:00:00', '2024-07-25 11:30:00', 'Consultation', 'Scheduled', 'Coughing', 'N/A', 6, 12, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (14, '2024-07-26 13:00:00', '2024-07-26 13:45:00', 'Grooming', 'Scheduled', 'Nail trim', 'Just nails.', 7, 14, 3, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (15, '2024-07-27 14:00:00', '2024-07-27 14:30:00', 'Check-up', 'Scheduled', 'New pet exam', 'N/A', 8, 15, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (16, '2024-07-28 15:00:00', '2024-07-28 15:45:00', 'Emergency', 'Scheduled', 'Limping', 'Possible sprain.', 8, 16, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (17, '2024-07-29 16:00:00', '2024-07-29 16:30:00', 'Consultation', 'Scheduled', 'Skin rash', 'N/A', 9, 17, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (18, '2024-07-30 09:00:00', '2024-07-30 09:45:00', 'Vaccination', 'Scheduled', 'Rabies shot', 'N/A', 10, 19, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (19, '2024-07-31 10:00:00', '2024-07-31 10:30:00', 'Check-up', 'Scheduled', 'Annual check', 'N/A', 9, 18, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (20, '2024-08-01 11:00:00', '2024-08-01 11:45:00', 'Emergency', 'Scheduled', 'Ingested foreign object', 'Needs endoscopy.', 10, 20, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (21, '2024-08-02 09:00:00', '2024-08-02 09:30:00', 'Check-up', 'Scheduled', 'Routine check', 'N/A', 11, 21, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (22, '2024-08-03 10:00:00', '2024-08-03 10:45:00', 'Vaccination', 'Scheduled', 'Annual shots', 'Needs FVRCP.', 12, 23, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (23, '2024-08-04 11:00:00', '2024-08-04 11:30:00', 'Consultation', 'Scheduled', 'Coughing', 'N/A', 11, 22, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (24, '2024-08-05 13:00:00', '2024-08-05 13:45:00', 'Grooming', 'Scheduled', 'Nail trim', 'Just nails.', 12, 24, 3, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (25, '2024-08-06 14:00:00', '2024-08-06 14:30:00', 'Check-up', 'Scheduled', 'New pet exam', 'N/A', 13, 25, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (26, '2024-08-07 15:00:00', '2024-08-07 15:45:00', 'Emergency', 'Scheduled', 'Limping', 'Possible sprain.', 14, 27, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (27, '2024-08-08 16:00:00', '2024-08-08 16:30:00', 'Consultation', 'Scheduled', 'Skin rash', 'N/A', 13, 26, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (28, '2024-08-09 09:00:00', '2024-08-09 09:45:00', 'Vaccination', 'Scheduled', 'Rabies shot', 'N/A', 14, 28, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (29, '2024-08-10 10:00:00', '2024-08-10 10:30:00', 'Check-up', 'Scheduled', 'Annual check', 'N/A', 15, 29, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (30, '2024-08-11 11:00:00', '2024-08-11 11:45:00', 'Emergency', 'Scheduled', 'Ingested foreign object', 'Needs endoscopy.', 15, 30, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (31, '2024-08-12 09:00:00', '2024-08-12 09:30:00', 'Check-up', 'Scheduled', 'Routine check', 'N/A', 16, 31, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (32, '2024-08-13 10:00:00', '2024-08-13 10:45:00', 'Vaccination', 'Scheduled', 'Annual shots', 'Needs DHPP.', 17, 33, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (33, '2024-08-14 11:00:00', '2024-08-14 11:30:00', 'Consultation', 'Scheduled', 'Coughing', 'N/A', 16, 32, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (34, '2024-08-15 13:00:00', '2024-08-15 13:45:00', 'Grooming', 'Scheduled', 'Nail trim', 'Just nails.', 17, 34, 3, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (35, '2024-08-16 14:00:00', '2024-08-16 14:30:00', 'Check-up', 'Scheduled', 'New pet exam', 'N/A', 18, 35, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (36, '2024-08-17 15:00:00', '2024-08-17 15:45:00', 'Emergency', 'Scheduled', 'Limping', 'Possible sprain.', 19, 37, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (37, '2024-08-18 16:00:00', '2024-08-18 16:30:00', 'Consultation', 'Scheduled', 'Skin rash', 'N/A', 18, 36, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (38, '2024-08-19 09:00:00', '2024-08-19 09:45:00', 'Vaccination', 'Scheduled', 'Rabies shot', 'N/A', 19, 38, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (39, '2024-08-20 10:00:00', '2024-08-20 10:30:00', 'Check-up', 'Scheduled', 'Annual check', 'N/A', 20, 39, 4, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL),
-- (40, '2024-08-21 11:00:00', '2024-08-21 11:45:00', 'Emergency', 'Scheduled', 'Ingested foreign object', 'Needs endoscopy.', 20, 40, 2, NOW(), NOW(), 'admin', NULL, NULL, NULL, NULL);
--
-- -- Data for waiting_room table (10 rows)
 INSERT INTO waiting_room (id, client_id, pet_id, arrival_time, status, reason_for_visit, priority, notes, consultation_started_at, completed_at) VALUES
 (1, 6, 1, '2024-07-05 09:00:00', 'WAITING', 'Check-up', 'NORMAL', 'N/A', NULL, NULL),
 (2, 7, 2, '2024-07-05 09:15:00', 'WAITING', 'Vaccination', 'URGENTE', 'N/A', NULL, NULL),
 (3, 8, 3, '2024-07-05 10:00:00', 'WAITING', 'Skin issue', 'NORMAL', 'N/A', NULL, NULL),
 (4, 9, 4, '2024-07-05 10:05:00', 'IN_CONSULTATION', 'Vaccination', 'NORMAL', 'N/A', '2024-07-05 10:10:00', NULL),
 (5, 10, 5, '2024-07-05 10:20:00', 'WAITING', 'Not eating', 'EMERGENCIA', 'N/A', NULL, NULL),
 (6, 11, 6, '2024-07-05 11:00:00', 'WAITING', 'Check-up', 'URGENTE', 'N/A', NULL, NULL),
 (7, 12, 7, '2024-07-05 11:15:00', 'WAITING', 'Vaccination', 'NORMAL', 'N/A', NULL, NULL),
 (8, 13, 8, '2024-07-05 11:30:00', 'IN_CONSULTATION', 'Cough', 'URGENTE', 'N/A', '2024-07-05 11:35:00', NULL),
 (9, 14, 9, '2024-07-05 11:45:00', 'WAITING', 'Grooming', 'NORMAL', 'N/A', NULL, NULL),
 (10, 15, 10, '2024-07-05 12:00:00', 'WAITING', 'New pet', 'URGENTE', 'N/A', NULL, NULL);



-- Data for suppliers table (10 rows)
INSERT INTO suppliers (supplier_id, rnc, company_name, contact_person,
                       contact_phone, contact_email, province, municipality,
                       sector, street_address, active)
VALUES (1, '123456789', 'Pets & Suppliers Co.', 'John Doe', '809-123-4567',
        'johndoe@example.com', 'Santo Domingo', 'Santo Domingo Este',
        'Los Mina', '123 Main St.', TRUE),
       (2, '987654321', 'Pet Supplies Inc.', 'Jane Smith', '809-987-6543',
        'janesmith@example.com', 'Santiago', 'Santiago de los Caballeros',
        'Los Jardines', '456 Elm St.', TRUE),
       (3, '456789123', 'Animal Care Supplies', 'Carlos Perez', '809-456-7890',
        'carlosperez@example.com', 'La Romana', 'La Romana', 'Villa Verde',
        '789 Oak St.', TRUE),
       (4, '321654987', 'Pet Food & More', 'Maria Lopez', '809-321-6543',
        'marialopez@example.com', 'Puerto Plata', 'Puerto Plata', 'El Pueblito',
        '321 Pine St.', TRUE),
       (5, '654321789', 'Vet Supplies Dominican', 'Luis Garcia', '809-654-3210',
        'luisgarcia@example', 'San Cristobal', 'San Cristobal',
        'Villa Altagracia', '654 Maple St.', TRUE);

-- Data for supplier_products table (10 rows)-- Data for products table with correct attributes and ProductCategory enum
 INSERT INTO products (product_id, name, description, active, price, stock, reorder_level, supplier_id, category) VALUES
 (1, 'Premium Dog Food', 'High-quality dry food for adult dogs', TRUE, 1500.00, 100, 20, 1, 'ALIMENTO'),
 (2, 'High Protein Cat food', 'Nutritious wet food for cats', TRUE, 1200.00, 200, 30, 1, 'ALIMENTO'),
 (3, 'Healthy Dog Treats', 'Natural training treats for dogs', TRUE, 500.00, 300, 50, 2, 'ALIMENTO'),
 (4, 'Crunchy Cat Treats', 'Dental health treats for cats', TRUE, 400.00, 250, 40, 2, 'ALIMENTO'),
 (5, 'Gentle Dog Shampoo', 'Hypoallergenic shampoo for sensitive skin', TRUE, 800.00, 150, 25, 3, 'HIGIENE'),
 (6, 'Soothing Cat Shampoo', 'Moisturizing shampoo for cats', TRUE, 700.00, 180, 30, 3, 'HIGIENE'),
 (7, 'Durable Dog Collar', 'Adjustable nylon collar for medium dogs', TRUE, 600.00, 120, 20, 4, 'ACCESORIO'),
 (8, 'Stylish Cat Collar', 'Decorative collar with bell for cats', TRUE, 550.00, 130, 25, 4, 'ACCESORIO'),
 (9, 'Strong Dog Leash', 'Retractable leash for large dogs', TRUE, 900.00, 110, 15, 5, 'ACCESORIO'),
 (10, 'Clumping Cat Litter', 'Odor-control clumping litter', TRUE, 300.00, 400, 60, 5, 'HIGIENE'),
 (11, 'Antibiotic Tablets', 'Broad-spectrum antibiotics for pets', TRUE, 2500.00, 50, 10, 1, 'MEDICINA'),
 (12, 'Flea Treatment', 'Monthly flea prevention for dogs and cats', TRUE, 1800.00, 75, 15, 2, 'MEDICINA'),
 (13, 'Pet Vitamins', 'Daily multivitamin supplements', TRUE, 1200.00, 100, 20, 3, 'MEDICINA'),
 (14, 'Dental Chews', 'Tartar control chews for dogs', TRUE, 650.00, 200, 35, 4, 'HIGIENE'),
 (15, 'Pet Carrier', 'Airline-approved pet travel carrier', TRUE, 3500.00, 25, 5, 5, 'ACCESORIO');
 
INSERT INTO invoices (client,
                      issued_date,
                      payment_date,
                      sales_order,
                      status,
                      subtotal,
                      discount_percentage,
                      discount,
                      tax,
                      total,
                      paid_to_date,
                      notes,
                      created_by,
                      created_date,
                      last_modified_by,
                      last_modified_date)
VALUES (6, -- client (foreign key to client_id)
        '2024-01-15', -- issuedDate
        '2024-02-14', -- paymentDate
        'SO-001', -- salesOrder
        'PENDING', -- status (DRAFT, PENDING, PAID, OVERDUE)
        1500.00, -- subtotal
        10.00, -- discountPercentage
        150.00, -- discount
        243.00, -- tax
        1593.00, -- total
        0.00, -- paidToDate
        'This is a sample note.', -- notes
        'john.doe', -- created_by
        '2025-04-24 11:10:27.717079-04', -- created_date
        'admin_user', -- last_modified_by
        '2025-04-24 11:10:27.717079-04' -- last_modified_date
       );

-- =================================================================================================
--  RESET SEQUENCES TO AVOID CONFLICTS
-- =================================================================================================
--  NOTE: This is necessary because we are manually inserting data with specific IDs.
--  We need to update the sequence for each table to ensure that the next auto-generated ID
--  is greater than the highest ID we've inserted manually.
-- =================================================================================================

SELECT setval(pg_get_serial_sequence('users', 'user_id'),
              COALESCE((SELECT MAX(user_id) FROM users), 1), true);
SELECT setval(pg_get_serial_sequence('employee', 'employee_id'),
              COALESCE((SELECT MAX(employee_id) FROM employee), 1), true);
SELECT setval(pg_get_serial_sequence('client', 'client_id'),
              COALESCE((SELECT MAX(client_id) FROM client), 1), true);
SELECT setval(pg_get_serial_sequence('pets', 'id'),
              COALESCE((SELECT MAX(id) FROM pets), 1), true);
SELECT setval(pg_get_serial_sequence('medical_histories', 'id'),
              COALESCE((SELECT MAX(id) FROM medical_histories), 1), true);
SELECT setval(pg_get_serial_sequence('consultations', 'id'),
              COALESCE((SELECT MAX(id) FROM consultations), 1), true);
SELECT setval(pg_get_serial_sequence('appointments', 'id'),
              COALESCE((SELECT MAX(id) FROM appointments), 1), true);
SELECT setval(pg_get_serial_sequence('waiting_room', 'id'),
              COALESCE((SELECT MAX(id) FROM waiting_room), 1), true);
SELECT setval(pg_get_serial_sequence('suppliers', 'supplier_id'),
              COALESCE((SELECT MAX(supplier_id) FROM suppliers), 1), true);
SELECT setval(pg_get_serial_sequence('products', 'product_id'),
              COALESCE((SELECT MAX(product_id) FROM products), 1), true);
