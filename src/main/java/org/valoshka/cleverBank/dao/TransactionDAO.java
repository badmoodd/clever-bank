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

    public void save(Transaction transaction) {

        String sql = "INSERT INTO Transaction (trans_date_time, trans_type, trans_status, source_account, target_account, amount, currency) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(transaction.getDateTimeOfTransaction()));
            preparedStatement.setObject(2, transaction.getTransactionType(), Types.OTHER);
            preparedStatement.setObject(3, transaction.getTransactionStatus(), Types.OTHER);
            preparedStatement.setString(4, transaction.getSourceAccount().getAccountNumber());
            preparedStatement.setString(5, transaction.getTargetAccount().getAccountNumber());
            preparedStatement.setDouble(6, transaction.getAmount());
            preparedStatement.setString(7, transaction.getCurrency().getCurrencyCode());

            preparedStatement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
