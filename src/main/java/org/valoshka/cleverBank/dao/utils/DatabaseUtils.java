package org.valoshka.cleverBank.dao.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A utility class for common database operations.
 */
public class DatabaseUtils {

    /**
     * Checks if a record with the given value exists in the specified table and column.
     *
     * @param connection  The database connection to use for the query.
     * @param tableName   The name of the database table to check.
     * @param columnName  The name of the column to check for the value.
     * @param value       The value to check for in the specified column.
     * @return True if a record with the given value exists, false otherwise.
     */
    public static boolean recordExists(Connection connection, String tableName, String columnName, String value) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, value);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}