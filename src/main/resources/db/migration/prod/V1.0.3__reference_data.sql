--   ─────▀▄▀─────▄─────▄
--   ──▄███████▄──▀██▄██▀
--   ▄█████▀█████▄──▄█
--   ███████▀████████▀
--   ─▄▄▄▄▄▄███████▀

-- =================================================================================================
--  ZOOLAN VETMGMT - REFERENCE DATA MIGRATION
--  Version: V1.0.3__reference_data.sql
--  Dependencies: V0.0.1__init.sql
--  Description: Foundational reference data including warehouses, suppliers, and offerings
-- =================================================================================================

-- Migration validation
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM flyway_schema_history WHERE version = '0.0.1' AND success = true) THEN
        RAISE EXCEPTION 'Schema initialization (V0.0.1) must complete successfully before running this migration';
    END IF;
END $$;

-- =================================================================================================
--  WAREHOUSES - Storage location configuration
--  Dependencies: None (foundational data)
-- =================================================================================================
INSERT INTO warehouses (available_for_sale, status, id, name, warehouse_type)
VALUES 
    (true, true, 1, 'Almacén Principal', 'PRINCIPAL'),
    (false, true, 2, 'Almacén Secundario', 'SECUNDARIO'),
    (true, true, 3, 'Almacén de Higiene', 'SECUNDARIO'),
    (true, true, 4, 'Almacén de Accesorios', 'SECUNDARIO'),
    (false, true, 5, 'Almacén Médico', 'SECUNDARIO');

-- =================================================================================================
--  SUPPLIERS - Product and service suppliers
--  Dependencies: None (foundational data)
-- =================================================================================================
INSERT INTO suppliers (active, supplier_id, rnc, company_name, contact_email, contact_person, contact_phone,
                      municipality, province, sector, street_address)
VALUES 
    (true, 1, '12345678900', 'Pets & Suppliers Co.', 'johndoe@petsuppliers.com', 'John Doe', '8091234567',
     'Santo Domingo Este', 'Santo Domingo', 'Los Mina', '123 Main St.'),
    (true, 2, '98765432100', 'Pet Supplies Inc.', 'janesmith@petsupplies.com', 'Jane Smith', '8099876543',
     'Santiago de los Caballeros', 'Santiago', 'Los Jardines', '456 Elm St.'),
    (true, 3, '45678912300', 'Animal Care Supplies', 'carlosperez@animalcare.com', 'Carlos Perez', '8094567890',
     'La Romana', 'La Romana', 'Villa Verde', '789 Oak St.'),
    (true, 4, '32165498700', 'Pet Food & More', 'marialopez@petfood.com', 'Maria Lopez', '8093216543',
     'Puerto Plata', 'Puerto Plata', 'El Pueblito', '321 Pine St.'),
    (true, 5, '65432178900', 'Vet Supplies Dominican', 'luisgarcia@vetsupplies.do', 'Luis Garcia', '8096543210',
     'San Cristobal', 'San Cristobal', 'Villa Altagracia', '654 Maple St.');

-- =================================================================================================
--  OFFERINGS - Veterinary services offered
--  Dependencies: None (foundational data)
-- =================================================================================================
INSERT INTO offerings (active, price, created_at, id, updated_at, description, name, offering_type)
VALUES 
    (true, 800.00, '2025-08-26 12:12:33.452646', 1, '2025-08-26 12:12:33.452646', 
     'Consulta general y diagnóstico', 'Consulta Veterinaria', 'MEDICAL'),
    (true, 1200.00, '2025-08-26 12:12:33.452646', 2, '2025-08-26 12:12:33.452646', 
     'Servicio de grooming completo', 'Baño y Corte', 'GROOMING'),
    (true, 600.00, '2025-08-26 12:12:33.452646', 3, '2025-08-26 12:12:33.452646', 
     'Aplicación de vacunas', 'Vacunación', 'MEDICAL'),
    (true, 500.00, '2025-08-26 12:12:33.452646', 4, '2025-08-26 12:12:33.452646', 
     'Tratamiento antiparasitario', 'Desparasitación', 'VACCINATION'),
    (true, 1000.00, '2025-08-26 12:12:33.452646', 5, '2025-08-26 12:12:33.452646', 
     'Cuidado diario de mascotas', 'Guardería', 'MEDICAL'),
    (true, 1500.00, '2025-08-26 12:12:33.452646', 6, '2025-08-26 12:12:33.452646', 
     'Análisis clínicos veterinarios', 'Laboratorio', 'VACCINATION'),
    (true, 2000.00, '2025-08-26 12:12:33.452646', 7, '2025-08-26 12:12:33.452646', 
     'Hospedaje temporal de mascotas', 'Pensión', 'MEDICAL'),
    (true, 3500.00, '2025-08-26 12:12:33.452646', 8, '2025-08-26 12:12:33.452646',
     'Procedimientos quirúrgicos menores', 'Cirugía menor', 'MEDICAL'),
    (true, 300.00, '2025-08-26 12:12:33.452646', 9, '2025-08-26 12:12:33.452646', 
     'Recorte profesional de uñas', 'Corte de uñas', 'GROOMING'),
    (true, 900.00, '2025-08-26 12:12:33.452646', 10, '2025-08-26 12:12:33.452646', 
     'Limpieza bucal veterinaria', 'Limpieza dental', 'MEDICAL');