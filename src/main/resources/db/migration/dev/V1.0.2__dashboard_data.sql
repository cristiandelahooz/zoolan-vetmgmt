-- =================================================================================================
--  ZOOLAN VET-MGMT DASHBOARD ENHANCEMENT DATA
-- =================================================================================================
--  PURPOSE: This script populates the database with extensive, realistic, time-series
--           data to make the executive dashboard charts meaningful and visually rich.
--           It generates data spanning the last 12 months to showcase trends,
--           forecasts, and operational patterns.
-- =================================================================================================

-- To ensure a clean slate, we remove previous transactional data.
-- Master data (users, pets, products, etc.) is preserved.
TRUNCATE TABLE appointments, consultations, invoices, invoice_product, invoice_offerings, payments, payments_detail, waiting_room RESTART IDENTITY;

-- =================================================================================================
--  1. APPOINTMENTS & CONSULTATIONS
--     - Generate historical data for the last 12 months.
--     - Create variety in times and days to feed utilization and trend charts.
-- =================================================================================================

-- Generate a series of dates for the last 365 days
CREATE TEMP TABLE IF NOT EXISTS date_series AS
SELECT (NOW() - (n * INTERVAL '1 day'))::date AS appointment_date
FROM generate_series(0, 365) n;

-- Generate appointments over the last year
INSERT INTO appointments (start_appointment_date, end_appointment_date, offering_type, status, reason, client_id, pet_id, employee_id, created_at, updated_at, created_by)
SELECT
    d.appointment_date + '08:00:00'::time + (floor(random() * 10) || ' hours')::interval, -- Start between 8 AM and 6 PM
    d.appointment_date + '08:30:00'::time + (floor(random() * 10) || ' hours')::interval, -- 30 min duration
    CASE (random() * 2)::int
        WHEN 0 THEN 'MEDICAL'
        WHEN 1 THEN 'GROOMING'
        ELSE 'KENNEL'
        END,
    'COMPLETADA',
    'Cita histórica generada para dashboard',
    (SELECT client_id FROM client ORDER BY random() LIMIT 1),
    (SELECT id FROM pets ORDER BY random() LIMIT 1),
    (SELECT employee_id FROM employee WHERE employee_role = 'VETERINARIAN' ORDER BY random() LIMIT 1),
    d.appointment_date,
    d.appointment_date,
    'system'
FROM date_series d
         CROSS JOIN generate_series(1, (random() * 8 + 3)::int) -- 3 to 11 appointments per day
WHERE extract(isodow from d.appointment_date) < 6; -- Monday to Friday

-- Generate consultations linked to some of these appointments
INSERT INTO consultations (pet, veterinarian, medical_history, consultation_date, diagnosis, treatment, notes, created_at, updated_at, active)
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
    a.created_at,
    a.updated_at,
    TRUE
FROM appointments a
WHERE random() < 0.8; -- Create consultations for 80% of appointments


-- =================================================================================================
--  2. INVOICES
--     - Create invoices from the historical consultations.
--     - Spread them over the last 12 months for revenue analysis.
-- =================================================================================================

INSERT INTO invoices (client, issued_date, payment_date, status, subtotal, tax, total, paid_to_date, notes, active, created_by, created_date, last_modified_by, last_modified_date, consultation)
SELECT
    a.client_id,
    c.consultation_date,
    c.consultation_date + (random() * 30 || ' days')::interval,
    CASE WHEN random() < 0.85 THEN 'PAID' ELSE 'PENDING' END,
    sub.subtotal_val,
    sub.subtotal_val * 0.18,
    sub.subtotal_val * 1.18,
    CASE WHEN (CASE WHEN random() < 0.85 THEN 'PAID' ELSE 'PENDING' END) = 'PAID' THEN sub.subtotal_val * 1.18 ELSE 0 END,
    'Factura histórica generada para dashboard',
    TRUE,
    'system',
    c.consultation_date,
    'system',
    c.consultation_date,
    c.id
FROM consultations c
         JOIN appointments a ON c.pet = a.pet_id AND c.veterinarian = a.employee_id AND c.consultation_date = a.start_appointment_date::date
         CROSS JOIN LATERAL (SELECT (random() * 5000 + 800)::numeric(10,2) AS subtotal_val) AS sub;


-- =================================================================================================
--  3. INVOICE DETAILS (SERVICES & PRODUCTS)
--     - Populate invoices with various services and products for profitability analysis.
-- =================================================================================================

-- Invoice Services
INSERT INTO invoice_offerings (invoice, offering, quantity, amount)
SELECT
    i.code,
    s.id,
    1,
    s.price
FROM invoices i
         JOIN offerings s ON 1=1 -- Cross join and then limit
WHERE random() < 0.9 -- 90% of invoices will have a service
ORDER BY random()
LIMIT (SELECT count(*) FROM invoices) * 1.2; -- Average 1.2 services per invoice

-- Invoice Products
INSERT INTO invoice_product (invoice, product, quantity, price, amount, created_by, created_date, last_modified_by, last_modified_date)
SELECT
    i.code,
    p.product_id,
    (random() * 4 + 1)::int, -- Quantity between 1 and 5
    p.sales_price,
    p.sales_price * (random() * 4 + 1)::int,
    'system',
    i.issued_date,
    'system',
    i.issued_date
FROM invoices i
         JOIN products p ON p.usage_type = 'VENTA'
WHERE random() < 0.65 -- 65% of invoices will have products
ORDER BY random()
LIMIT (SELECT count(*) FROM invoices) * 2.5; -- Average 2.5 products per invoice


-- =================================================================================================
--  4. STOCK HEALTH ALERTS
--     - Manually adjust stock levels for specific products to trigger alerts.
-- =================================================================================================

-- OUT_OF_STOCK: Set available stock to 0 for multiple products
UPDATE products
SET available_stock = 0, accounting_stock = 0
WHERE name IN ('Flea Treatment', 'Pet Carrier', 'Durable Dog Collar');

-- CRITICAL: Set available stock just below the reorder level for multiple products
UPDATE products
SET available_stock = reorder_level - (random() * 2 + 1)::int -- 1 to 3 below reorder
WHERE name IN ('Antibiotic Tablets', 'Pet Vitamins', 'Gentle Dog Shampoo');

-- LOW: Set available stock just at or slightly above the reorder level for multiple products
UPDATE products
SET available_stock = reorder_level + (random() * 3)::int -- 0 to 3 above reorder
WHERE name IN ('Premium Dog Food', 'High Protein Cat Food', 'Healthy Dog Treats', 'Clumping Cat Litter');


-- =================================================================================================
--  5. CLIENT RETENTION DATA
--     - Ensure some clients have invoices in multiple distinct months.
-- =================================================================================================

-- Pick 10 random clients and give them an invoice in each of the last 9 months
WITH RECURRING_CLIENTS AS (
    SELECT client_id FROM client ORDER BY random() LIMIT 10
)
INSERT INTO invoices (client, issued_date, payment_date, status, subtotal, tax, total, paid_to_date, notes, active, created_by, created_date, last_modified_by, last_modified_date)
SELECT
    rc.client_id,
    (NOW() - (m.month || ' month')::interval)::date,
    (NOW() - (m.month || ' month')::interval)::date + '30 days'::interval,
    'PAID',
    sub.subtotal_val,
    sub.subtotal_val * 0.18,
    sub.subtotal_val * 1.18,
    sub.subtotal_val * 1.18,
    'Factura mensual para cliente recurrente',
    TRUE,
    'system',
    (NOW() - (m.month || 'month')::interval)::date,
    'system',
    (NOW() - (m.month || 'month')::interval)::date
FROM RECURRING_CLIENTS rc, generate_series(1, 9) m(month)
                               CROSS JOIN LATERAL (SELECT (random() * 2000 + 500)::numeric(10,2) AS subtotal_val) AS sub;


-- =================================================================================================
--  6. PAYMENTS
--     - Create payments for the invoices marked as PAID.
-- =================================================================================================
INSERT INTO payments (payment_date, total_amount, method, status, reference_number, notes, created_by, created_date, last_modified_by, last_modified_date)
SELECT
    i.issued_date + (random() * 10)::int * '1 day'::interval,
    i.total,
    CASE (random() * 3)::int
        WHEN 0 THEN 'EFECTIVO'
        WHEN 1 THEN 'TARJETA'
        ELSE 'TRANSFERENCIA'
        END,
    'COMPLETADO',
    'PAY-' || i.code || '-' || (random() * 10000)::int,
    'Pago para factura histórica',
    'system',
    i.issued_date,
    'system',
    i.issued_date
FROM invoices i
WHERE i.status = 'PAID';

-- Link payments to invoices
INSERT INTO payments_detail (payment, invoice, amount, created_by, created_date)
SELECT
    p.code,
    i.code,
    i.total,
    'system',
    p.payment_date
FROM payments p
         JOIN invoices i ON p.reference_number LIKE 'PAY-' || i.code || '-% '
WHERE NOT EXISTS (SELECT 1 FROM payments_detail pd WHERE pd.invoice = i.code);

-- Final check: Ensure all PAID invoices have their paid_to_date amount updated correctly.
UPDATE invoices i
SET paid_to_date = i.total
WHERE i.status = 'PAID' AND i.paid_to_date = 0;