-- Atualiza os valores corretos de lote para o evento 11, baseado nos IDs existentes
-- Analisando o contexto: múltiplos ingressos para o mesmo evento devem ter lotes distintos
UPDATE ingressos SET lote = 1 WHERE id = 12 AND evento_id = 11;
UPDATE ingressos SET lote = 2 WHERE id = 24 AND evento_id = 11;
UPDATE ingressos SET lote = 3 WHERE id = 46 AND evento_id = 11;