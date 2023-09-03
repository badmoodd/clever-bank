package org.valoshka.cleverBank.dao;

import org.valoshka.cleverBank.models.Transaction;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class TransactionDAO {
    private static final Properties properties = new Properties();

    public TransactionDAO() {
        try (InputStream inputStream = ClientDAO.class.getClassLoader().getResourceAsStream("postgreSQL/database.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.out.println("Error reading database properties file");
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        return ConnectionManager.getConnection(properties);
    }

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
