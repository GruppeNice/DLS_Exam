package db.migration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Renames legacy review_vote.value to vote_value on older MySQL volumes.
 * Fresh schemas already define vote_value in V1, so this is a no-op there.
 */
public class V4__RenameReviewVoteValueColumn extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!columnExists(connection, "review_vote", "value")) {
            return;
        }

        String product = connection.getMetaData().getDatabaseProductName().toLowerCase();
        try (Statement statement = connection.createStatement()) {
            if (product.contains("mysql")) {
                statement.execute("ALTER TABLE review_vote CHANGE COLUMN value vote_value INT NOT NULL");
            } else {
                statement.execute("ALTER TABLE review_vote RENAME COLUMN value TO vote_value");
            }
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws Exception {
        DatabaseMetaData meta = connection.getMetaData();
        String[] tablePatterns = { table, table.toUpperCase(), table.toLowerCase() };
        String[] columnPatterns = { column, column.toUpperCase(), column.toLowerCase() };

        for (String tablePattern : tablePatterns) {
            for (String columnPattern : columnPatterns) {
                try (ResultSet columns = meta.getColumns(null, null, tablePattern, columnPattern)) {
                    if (columns.next()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
