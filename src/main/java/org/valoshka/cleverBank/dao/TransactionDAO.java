package org.valoshka.cleverBank.dao;

import org.valoshka.cleverBank.models.Transaction;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Data Access Object (DAO) for managing transactions in the database.
 */
public class TransactionDAO {
    private static final Properties properties = new Properties();

    /**
     * Constructs a new TransactionDAO and loads database properties from a configuration file.
     */
    public TransactionDAO() {
        try (InputStream inputStream = ClientDAO.class.getClassLoader().getResourceAsStream("postgreSQL/database.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.out.println("Error reading database properties file");
            e.printStackTrace();
        }
    }

    /**
     * Gets a database connection using the loaded properties.
     *
     * @return A database connection.
     * @throws SQLException           If a database access error occurs.
     * @throws ClassNotFoundException If the database driver class is not found.
     */
    public Connection getConnection() throws SQLException, ClassNotFoundException {
        return ConnectionManager.getConnection(properties);
    }

    /**
     * Saves a transaction record in the database and returns the generated transaction ID.
     *
     * @param transaction The Transaction object to be saved.
     * @return The generated transaction ID, or -1 if the save operation failed.
     */
    public int save(Transaction transaction) {
        String sql = "INSERT INTO Transaction (trans_date_time, trans_type, trans_status, source_account, target_account, amount, currency) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(transaction.getDateTimeOfTransaction()));
            preparedStatement.setObject(2, transaction.getTransactionType(), Types.OTHER);
            preparedStatement.setObject(3, transaction.getTransactionStatus(), Types.OTHER);
            preparedStatement.setString(4, transaction.getSourceAccount().getAccountNumber());
            preparedStatement.setString(5, transaction.getTargetAccount().getAccountNumber());
            preparedStatement.setDouble(6, transaction.getAmount());
            preparedStatement.setString(7, transaction.getCurrency().getCurrencyCode());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating transaction failed, no rows affected.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }






}
