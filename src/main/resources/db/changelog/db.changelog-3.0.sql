-- =============================================
-- Currency: replace € with $ in warranty conditions text
-- =============================================
UPDATE warranties SET conditions = REPLACE(conditions, '€', '$') WHERE conditions LIKE '%€%';
