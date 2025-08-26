--   ─────▀▄▀─────▄─────▄
--   ──▄███████▄──▀██▄██▀
--   ▄█████▀█████▄──▄█
--   ███████▀████████▀
--   ─▄▄▄▄▄▄███████▀

-- =================================================================================================
--  ZOOLAN VETMGMT - AUDIT LOGS MIGRATION
--  Version: V1.0.6__audit_logs.sql
--  Dependencies: V1.0.5__transactional_data.sql
--  Description: Historical audit trail data for change tracking (Hibernate Envers)
-- =================================================================================================

-- Migration validation
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM flyway_schema_history WHERE version = '1.0.5' AND success = true) THEN
        RAISE EXCEPTION 'Transactional data migration (V1.0.5) must complete successfully before running this migration';
    END IF;
END $$;

-- =================================================================================================
--  AUDIT LOG DISCLAIMER
--  
--  This migration contains sample audit log entries for demonstration purposes.
--  In a production environment, audit logs are typically:
--  
--  1. Generated automatically by Hibernate Envers during entity changes
--  2. Managed by the application, not inserted manually
--  3. Substantial in volume and should be carefully managed
--  
--  The sample data below demonstrates the audit trail structure for key business
--  operations performed during system initialization.
-- =================================================================================================

-- =================================================================================================
--  APPOINTMENTS AUDIT LOG - Sample appointment change tracking
--  Dependencies: revision table, appointments table
-- =================================================================================================
INSERT INTO appointments_log (assigned_employee_mod, client_mod, end_appointment_date_mod, guest_client_info_mod,
                             notes_mod, offering_type_mod, pet_mod, reason_mod, rev, revtype,
                             start_appointment_date_mod, status_mod, client_id, employee_id,
                             end_appointment_date, id, pet_id, start_appointment_date, reason, notes,
                             guest_client_email, guest_client_name, guest_client_pet_breed,
                             guest_client_pet_type, guest_client_phone, offering_type, status)
VALUES 
    -- Sample appointment creation logs
    (false, true, true, false, true, true, true, true, 1, 0, true, true, 14, NULL, '2025-08-26 14:00:00', 11, 1,
     '2025-08-26 13:00:00', 'Chequeo General', 'Todo bajo control', NULL, NULL, NULL, NULL, NULL, 'MEDICAL',
     'PROGRAMADA'),
    (false, false, true, true, true, true, false, true, 2, 0, true, true, NULL, NULL, '2025-08-26 15:00:00', 12,
     NULL, '2025-08-26 14:00:00', 'Cortar unas', 'Corte de unas trimestral', 'a@example.com', 'Cristian',
     'Golden Retriever', 'PERRO', '8291231324', 'GROOMING', 'PROGRAMADA'),
    (false, true, true, false, true, true, true, true, 3, 0, true, true, 14, NULL, '2025-08-27 10:00:00', 13, 1,
     '2025-08-27 08:00:00', 'Radiografia', 'Radiografia a Milo', NULL, NULL, NULL, NULL, NULL, 'MEDICAL',
     'PROGRAMADA');

-- =================================================================================================
--  PLACEHOLDER FOR ADDITIONAL AUDIT LOGS
--  
--  Additional audit log tables that would be populated in a full production system:
--  - users_log: User account changes
--  - client_log: Client information updates  
--  - employee_log: Employee data modifications
--  - pets_log: Pet information changes
--  - products_log: Product inventory changes
--  - medical_histories_log: Medical record updates
--  - invoices_log: Invoice modifications
--  - payments_log: Payment record changes
--  - consultations_log: Consultation updates
--  - grooming_sessions_log: Grooming service changes
--  
--  These would be populated automatically by the application during normal operations.
-- =================================================================================================

-- =================================================================================================
--  DEVELOPMENT NOTE
--  
--  This migration includes minimal audit log data for system initialization.
--  In production environments:
--  
--  1. Audit logs grow continuously as users modify data
--  2. Consider implementing log rotation and archival strategies
--  3. Monitor audit table sizes and performance impact
--  4. Audit logs are critical for compliance and change tracking
--  
--  For development/testing environments, you may populate additional sample
--  audit data by extending this migration or creating environment-specific
--  data migration files under /db/migration/dev/.
-- =================================================================================================