-- Align with entity column vote_value (older local DBs may still have "value").
SET @has_legacy_value_column := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'review_vote'
      AND COLUMN_NAME = 'value'
);

SET @rename_sql := IF(
    @has_legacy_value_column > 0,
    'ALTER TABLE review_vote CHANGE COLUMN value vote_value INT NOT NULL',
    'SELECT 1'
);

PREPARE rename_stmt FROM @rename_sql;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;
