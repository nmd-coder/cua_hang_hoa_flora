-- Synthetic development stock matching the sample catalog, never imported from a real store.
INSERT INTO stock(item_id,on_hand,reserved) VALUES
('10000000-0000-0000-0000-000000000001',20,0),
('10000000-0000-0000-0000-000000000002',20,0),
('10000000-0000-0000-0000-000000000003',20,0)
ON CONFLICT (item_id) DO NOTHING;