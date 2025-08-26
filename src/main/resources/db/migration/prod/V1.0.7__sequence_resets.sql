--   ─────▀▄▀─────▄─────▄
--   ──▄███████▄──▀██▄██▀
--   ▄█████▀█████▄──▄█
--   ███████▀████████▀
--   ─▄▄▄▄▄▄███████▀

-- =================================================================================================
--  ZOOLAN VETMGMT - SEQUENCE RESETS MIGRATION
--  Version: V1.0.7__sequence_resets.sql
--  Dependencies: V1.0.6__audit_logs.sql
--  Description: Reset sequence values to ensure proper ID generation after data migration
-- =================================================================================================

-- Migration validation
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM flyway_schema_history WHERE version = '1.0.6' AND success = true) THEN
        RAISE EXCEPTION 'Audit logs migration (V1.0.6) must complete successfully before running this migration';
    END IF;
END $$;

-- =================================================================================================
--  SEQUENCE MANAGEMENT
--  
--  PostgreSQL sequences maintain the next available ID for auto-generated identity columns.
--  After inserting data with explicit ID values, sequences must be reset to prevent
--  primary key conflicts when the application generates new records.
--  
--  Each setval call sets the sequence to the next available value based on the
--  highest ID currently in the corresponding table.
-- =================================================================================================

-- =================================================================================================
--  BUSINESS ENTITY SEQUENCES
--  Reset sequences for core business entities to next available values
-- =================================================================================================

-- Users table sequence (highest ID: 28 from master data migration)
SELECT pg_catalog.setval('public.users_user_id_seq', 28, true);

-- Pets table sequence (highest ID: 15 from master data migration)  
SELECT pg_catalog.setval('public.pets_id_seq', 15, true);

-- Appointments table sequence (highest ID: 20 from transactional data migration)
SELECT pg_catalog.setval('public.appointments_id_seq', 20, true);

-- Medical histories table sequence (highest ID: 15 from transactional data migration)
SELECT pg_catalog.setval('public.medical_histories_id_seq', 15, true);

-- Waiting room table sequence (highest ID: 7 from transactional data migration)
SELECT pg_catalog.setval('public.waiting_room_id_seq', 7, true);

-- =================================================================================================
--  OFFERING AND INVENTORY SEQUENCES
--  Reset sequences for service offerings and inventory management
-- =================================================================================================

-- Offerings table sequence (highest ID: 10 from reference data migration)
SELECT pg_catalog.setval('public.offerings_id_seq', 10, true);

-- Products table sequence (highest ID: 15 from master data migration)
SELECT pg_catalog.setval('public.products_product_id_seq', 15, true);

-- Suppliers table sequence (highest ID: 5 from reference data migration)
SELECT pg_catalog.setval('public.suppliers_supplier_id_seq', 5, true);

-- Warehouses table sequence (highest ID: 5 from reference data migration)
SELECT pg_catalog.setval('public.warehouses_id_seq', 5, true);

-- =================================================================================================
--  FINANCIAL AND TRANSACTION SEQUENCES
--  Reset sequences for financial transactions (initially empty, set to 1)
-- =================================================================================================

-- Invoices sequence (no data inserted yet, start at 1)
SELECT pg_catalog.setval('public.invoices_code_seq', 1, false);

-- Invoice offerings sequence (no data inserted yet, start at 1)
SELECT pg_catalog.setval('public.invoice_offerings_id_seq', 1, false);

-- Invoice products sequence (no data inserted yet, start at 1)
SELECT pg_catalog.setval('public.invoice_product_code_seq', 1, false);

-- Payments sequence (no data inserted yet, start at 1)
SELECT pg_catalog.setval('public.payments_code_seq', 1, false);

-- Payment details sequence (no data inserted yet, start at 1)
SELECT pg_catalog.setval('public.payments_detail_code_seq', 1, false);

-- =================================================================================================
--  CONSULTATION AND GROOMING SEQUENCES  
--  Reset sequences for medical and grooming services (initially empty, set to 1)
-- =================================================================================================

-- Consultations sequence (no data inserted yet, start at 1)
SELECT pg_catalog.setval('public.consultations_id_seq', 1, false);

-- Grooming sessions sequence (no data inserted yet, start at 1)  
SELECT pg_catalog.setval('public.grooming_sessions_id_seq', 1, false);

-- =================================================================================================
--  AUDIT SEQUENCE
--  Reset audit revision sequence (highest ID: 10 from transactional data migration)
-- =================================================================================================

-- Revision sequence for audit logging (highest ID: 10 from transactional data migration)
SELECT pg_catalog.setval('public.revision_seq', 10, true);

-- =================================================================================================
--  SEQUENCE RESET VERIFICATION
--  
--  After running this migration, verify that sequences are properly set by checking:
--  
--  1. SELECT currval('sequence_name') - Should return the expected next value
--  2. Test INSERT operations without specifying ID values
--  3. Ensure no primary key conflicts occur during normal operations
--  
--  Note: The third parameter in setval():
--  - true: Sets the sequence to the specified value (next call returns value + 1)
--  - false: Sets the sequence so the next call returns the specified value
-- =================================================================================================