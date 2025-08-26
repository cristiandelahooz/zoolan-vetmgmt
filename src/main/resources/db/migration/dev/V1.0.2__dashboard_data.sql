-- =================================================================================================
--  ZOOLAN VET-MGMT DASHBOARD ENHANCEMENT DATA (REDUCED VERSION)
-- =================================================================================================
--  PURPOSE: This script populates the database with limited, realistic data
--           to make the executive dashboard functional without performance issues.
--           Limited to 15 records per table for development purposes.
-- =================================================================================================

-- To ensure a clean slate, we remove previous transactional data.
-- Master data (users, pets, products, etc.) is preserved.
TRUNCATE TABLE appointments, consultations, grooming_sessions, invoices, invoice_product, invoice_offerings, payments, payments_detail, waiting_room RESTART IDENTITY CASCADE;

-- =================================================================================================
--  1. APPOINTMENTS & CONSULTATIONS (15 records each)
-- =================================================================================================

-- Generate 15 appointments over the last 30 days
INSERT INTO appointments (start_appointment_date, end_appointment_date, offering_type, status, reason, client_id, pet_id, employee_id, created_by, created_date, last_modified_by, last_modified_date)
SELECT
    (NOW() - (n * INTERVAL '2 days'))::date + '08:00:00'::time + (floor(random() * 8) || ' hours')::interval,
    (NOW() - (n * INTERVAL '2 days'))::date + '08:30:00'::time + (floor(random() * 8) || ' hours')::interval,
    CASE (random() * 3)::int
        WHEN 0 THEN 'MEDICAL'
        WHEN 1 THEN 'GROOMING'
        ELSE 'VACCINATION'
        END,
    'COMPLETADA',
    'Cita histórica generada para dashboard',
    (SELECT client_id FROM client ORDER BY random() LIMIT 1),
    (SELECT id FROM pets ORDER BY random() LIMIT 1),
    (SELECT employee_id FROM employee WHERE employee_role = 'VETERINARIAN' ORDER BY random() LIMIT 1),
    'system',
    (NOW() - (n * INTERVAL '2 days'))::timestamp,
    'system',
    (NOW() - (n * INTERVAL '2 days'))::timestamp
FROM generate_series(0, 14) n
WHERE EXTRACT(DOW FROM (NOW() - (n * INTERVAL '2 days'))::date) != 0; -- Exclude Sundays (0 = Sunday)

-- Generate 15 consultations linked to appointments
INSERT INTO consultations (pet, veterinarian, medical_history, consultation_date, diagnosis, treatment, notes, status, created_at, updated_at, active)
SELECT
    a.pet_id,
    a.employee_id,
    (SELECT id FROM medical_histories WHERE pet = a.pet_id LIMIT 1),
    a.start_appointment_date::date,
    CASE (random() * 4)::int
        WHEN 0 THEN 'Chequeo de rutina'
        WHEN 1 THEN 'Alergia leve'
        WHEN 2 THEN 'Control de peso'
        ELSE 'Vacunación anual'
        END,
    'Tratamiento estándar aplicado',
    'Consulta histórica generada para dashboard',
    'COMPLETADO',
    a.created_date,
    a.last_modified_date,
    TRUE
FROM appointments a
LIMIT 15;

-- Generate grooming sessions from appointments
INSERT INTO grooming_sessions (pet, groomer, grooming_date, notes, active, created_at, updated_at)
SELECT
    a.pet_id,
    (SELECT employee_id FROM employee WHERE employee_role = 'GROOMER' ORDER BY random() LIMIT 1),
    a.start_appointment_date,
    'Grooming histórico generado para dashboard',
    TRUE,
    a.created_date,
    a.last_modified_date
FROM appointments a
WHERE a.offering_type = 'GROOMING';

-- =================================================================================================
--  2. INVOICES (15 records)
-- =================================================================================================

-- Create invoices for medical consultations (10 invoices)
INSERT INTO invoices (client, issued_date, payment_date, status, subtotal, tax, total, paid_to_date, notes, active, created_by, created_date, last_modified_by, last_modified_date, consultation)
SELECT
    (SELECT po.owners FROM pet_owners po WHERE po.pet_id = con.pet LIMIT 1) as client_id,
    con.consultation_date,
    con.consultation_date + (random() * 15 || ' days')::interval,
    CASE WHEN random() < 0.8 THEN 'PAID' ELSE 'PENDING' END,
    sub.subtotal_val,
    sub.subtotal_val * 0.18,
    sub.subtotal_val * 1.18,
    CASE WHEN (CASE WHEN random() < 0.8 THEN 'PAID' ELSE 'PENDING' END) = 'PAID' THEN sub.subtotal_val * 1.18 ELSE 0 END,
    'Factura de consulta médica generada para dashboard',
    TRUE,
    'system',
    con.consultation_date::timestamp,
    'system',
    con.consultation_date::timestamp,
    con.id
FROM consultations con
         CROSS JOIN LATERAL (SELECT (random() * 1500 + 300)::numeric(10,2) AS subtotal_val) AS sub
ORDER BY con.id
LIMIT 10;

-- Create invoices for grooming sessions (5 invoices)
INSERT INTO invoices (client, issued_date, payment_date, status, subtotal, tax, total, paid_to_date, notes, active, created_by, created_date, last_modified_by, last_modified_date, grooming)
SELECT
    (SELECT po.owners FROM pet_owners po WHERE po.pet_id = gs.pet LIMIT 1) as client_id,
    gs.grooming_date,
    gs.grooming_date + (random() * 10 || ' days')::interval,
    CASE WHEN random() < 0.9 THEN 'PAID' ELSE 'PENDING' END,
    sub.subtotal_val,
    sub.subtotal_val * 0.18,
    sub.subtotal_val * 1.18,
    CASE WHEN (CASE WHEN random() < 0.9 THEN 'PAID' ELSE 'PENDING' END) = 'PAID' THEN sub.subtotal_val * 1.18 ELSE 0 END,
    'Factura de sesión de grooming generada para dashboard',
    TRUE,
    'system',
    gs.grooming_date::timestamp,
    'system',
    gs.grooming_date::timestamp,
    gs.id
FROM grooming_sessions gs
         CROSS JOIN LATERAL (SELECT (random() * 800 + 200)::numeric(10,2) AS subtotal_val) AS sub
ORDER BY gs.id
LIMIT 5;

-- =================================================================================================
--  3. INVOICE DETAILS (15 records each)
-- =================================================================================================

-- Invoice Services (15 records)
INSERT INTO invoice_offerings (invoice, offering, quantity, amount)
SELECT
    i.code,
    s.id,
    1,
    s.price
FROM invoices i
         CROSS JOIN offerings s
WHERE random() < 0.5
ORDER BY random()
LIMIT 15;

-- Invoice Products (15 records)
INSERT INTO invoice_product (invoice, product, quantity, price, amount, created_by, created_date, last_modified_by, last_modified_date)
SELECT
    i.code,
    p.product_id,
    (random() * 3 + 1)::int,
    p.sales_price,
    p.sales_price * (random() * 3 + 1)::int,
    'system',
    i.issued_date,
    'system',
    i.issued_date
FROM invoices i
         CROSS JOIN products p
WHERE p.usage_type = 'VENTA' AND random() < 0.3
ORDER BY random()
LIMIT 15;

-- =================================================================================================
--  4. STOCK HEALTH ALERTS (Limited adjustments)
-- =================================================================================================

-- OUT_OF_STOCK: Set 3 products to zero stock
UPDATE products
SET available_stock = 0, accounting_stock = 0
WHERE product_id IN (
    SELECT product_id FROM products ORDER BY random() LIMIT 3
);

-- CRITICAL: Set 3 products below reorder level
UPDATE products
SET available_stock = GREATEST(0, reorder_level - 2)
WHERE product_id IN (
    SELECT product_id FROM products
    WHERE available_stock > 0
    ORDER BY random()
    LIMIT 3
);

-- LOW: Set 3 products at reorder level
UPDATE products
SET available_stock = reorder_level + 1
WHERE product_id IN (
    SELECT product_id FROM products
    WHERE available_stock > reorder_level
    ORDER BY random()
    LIMIT 3
);

-- =================================================================================================
--  5. PAYMENTS (15 records)
-- =================================================================================================

INSERT INTO payments (payment_date, total_amount, method, status, reference_number, notes, created_by, created_date, last_modified_by, last_modified_date)
SELECT
    i.issued_date + (random() * 5)::int * '1 day'::interval,
    i.total,
    CASE (random() * 3)::int
        WHEN 0 THEN 'CASH'
        WHEN 1 THEN 'ELECTRONIC'
        ELSE 'TRANSFER'
        END,
    'SUCCESS',
    'PAY-' || i.code || '-' || (random() * 1000)::int,
    'Pago para factura histórica',
    'system',
    i.issued_date,
    'system',
    i.issued_date
FROM invoices i
WHERE i.status = 'PAID'
LIMIT 15;

-- Link payments to invoices (15 records)
INSERT INTO payments_detail (payment, invoice, amount, created_by, created_date, last_modified_by, last_modified_date)
SELECT
    p.code,
    i.code,
    i.total,
    'system',
    p.payment_date,
    'system',
    p.payment_date
FROM payments p
         JOIN invoices i ON p.reference_number LIKE 'PAY-' || i.code || '-%'
WHERE NOT EXISTS (SELECT 1 FROM payments_detail pd WHERE pd.invoice = i.code)
LIMIT 15;

-- Final cleanup: Update paid amounts for PAID invoices
UPDATE invoices i
SET paid_to_date = i.total
WHERE i.status = 'PAID' AND i.paid_to_date = 0;