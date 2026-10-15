-- Demo seed data for local development
-- Run after schema.sql has executed

INSERT INTO tenants (id, name) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Green Valley Nursery'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Highland Botanicals');

INSERT INTO users (id, tenant_id, email, role) VALUES
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin@greenvalley.com', 'admin'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'grower@greenvalley.com', 'grower'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'admin@highland.com', 'admin');

INSERT INTO greenhouse_zones (id, tenant_id, name, microclimate) VALUES
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Propagation House A', '{"temp_min": 20, "temp_max": 24, "humidity_target": 75}'),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Growing House B', '{"temp_min": 18, "temp_max": 26, "humidity_target": 65}'),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Mature Sales Floor', '{"temp_min": 15, "temp_max": 22, "humidity_target": 55}'),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Main Greenhouse', '{"temp_min": 19, "temp_max": 25, "humidity_target": 70}');

INSERT INTO plant_batches (id, tenant_id, species, zone_id, propagation_date, quantity_initial, quantity_current, unit_cost, status) VALUES
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Ficus lyrata', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '2024-01-15', 500, 485, 2.50, 'growing'),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Monstera deliciosa', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '2024-02-01', 300, 292, 5.00, 'growing'),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Snake Plant', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', '2023-10-01', 1000, 876, 1.25, 'mature'),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Pothos Golden', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2024-03-10', 800, 800, 0.75, 'propagating'),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ZZ Plant', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', '2023-08-15', 400, 0, 3.00, 'sold'),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Calathea Orbifolia', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', '2024-01-20', 200, 187, 4.50, 'growing'),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Philodendron Brasil', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', '2024-04-01', 350, 350, 2.00, 'propagating');

INSERT INTO treatments (id, tenant_id, batch_id, product_name, treatment_type, applied_date, application_rate, withholding_days) VALUES
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Neem Oil Extract', 'organic', '2024-06-01', '2 tbsp/gal', 0),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Pyrethrin Spray', 'chemical', '2024-06-15', '1 oz/gal', 14),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Biofungicide', 'organic', '2024-05-20', 'label rate', 0);

INSERT INTO diagnostic_logs (id, tenant_id, batch_id, symptom_notes, needs_review) VALUES
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Yellowing lower leaves, possible overwatering', true),
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Small brown spots on new growth', true);

INSERT INTO orders (id, tenant_id, customer_name, order_type, status) VALUES
    ('g0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'City Garden Center', 'standard', 'pending'),
    ('g0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Landscape Pros LLC', 'wholesale', 'pending');

INSERT INTO order_line_items (id, tenant_id, order_id, batch_id, species, promised_qty, promised_date) VALUES
    ('h0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'g0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Snake Plant', 200, '2024-08-01'),
    ('h0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'g0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', null, 'Ficus lyrata', 100, '2024-09-01');
