package org.valoshka.cleverBank.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connections using JDBC.
 */
public class ConnectionManager {

    /**
     * Gets a database connection using the provided properties.
     *
     * @param properties The properties containing connection details (URL, username, password, driver class).
     * @return A database connection.
     * @throws SQLException           If a database access error occurs.
     * @throws ClassNotFoundException If the database driver class is not found.
     */
    public static Connection getConnection(Properties properties) throws SQLException, ClassNotFoundException {
        String jdbcUrl = properties.getProperty("database.connection.url");
        String username = properties.getProperty("database.connection.username");
        String password = properties.getProperty("database.connection.password");

        Class.forName(properties.getProperty("database.driver_class"));

        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}