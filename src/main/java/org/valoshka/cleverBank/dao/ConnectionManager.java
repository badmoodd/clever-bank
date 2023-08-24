package org.valoshka.cleverBank.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManager {
    public static Connection getConnection(Properties properties) throws SQLException, ClassNotFoundException {
        String jdbcUrl = properties.getProperty("database.connection.url");
        String username = properties.getProperty("database.connection.username");
        String password = properties.getProperty("database.connection.password");

        Class.forName(properties.getProperty("database.driver_class"));

        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}