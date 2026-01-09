ALTER TABLE ingressos ADD COLUMN lote int DEFAULT 1 NOT NULL;

-- Optional: set distinct lotes for some events (adjust as needed)
-- UPDATE ingressos SET lote = 1 WHERE evento_id = 11 AND id = 12;
-- UPDATE ingressos SET lote = 2 WHERE evento_id = 11 AND id = 24;
-- UPDATE ingressos SET lote = 3 WHERE evento_id = 11 AND id = 46;