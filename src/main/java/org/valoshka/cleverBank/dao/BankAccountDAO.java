package org.valoshka.cleverBank.dao;

import org.valoshka.cleverBank.models.BankAccount;
import org.valoshka.cleverBank.models.Client;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class BankAccountDAO {

    private static final Properties properties = new Properties();

    public BankAccountDAO() {
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

    public Optional<BankAccount> get(String accountNumber) {
        String sql = "SELECT ba.*, c.name as client_name FROM BankAccount ba " +
                "INNER JOIN Client c ON ba.owner_id = c.client_id " +
                "WHERE ba.account_number = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String bankAccountNumber = resultSet.getString("account_number");
                    String bankName = resultSet.getString("bank_name");
                    LocalDate createdAt = resultSet.getDate("created_At").toLocalDate();
                    double balance = resultSet.getDouble("balance");
                    Currency currency = Currency.getInstance(resultSet.getString("currency"));
                    String clientName = resultSet.getString("client_name");

                    Client owner = new Client();
                    owner.setName(clientName);
                    int ownerId = resultSet.getInt("owner_id");
                    owner.setId(ownerId);

                    BankAccount bankAccount = new BankAccount();
                    bankAccount.setAccountNumber(bankAccountNumber);
                    bankAccount.setBankName(bankName);
                    bankAccount.setCreatedAt(createdAt);
                    bankAccount.setBalance(balance);
                    bankAccount.setCurrency(currency);

                    bankAccount.setOwner(owner);
                    owner.addAccount(bankAccount);


                    return Optional.of(bankAccount);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<BankAccount> getAll() {
        List<BankAccount> bankAccounts = new ArrayList<>();
        String sql = "SELECT ba.*, c.name as client_name FROM BankAccount ba " +
                "INNER JOIN Client c ON ba.owner_id = c.client_id";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String bankAccountNumber = resultSet.getString("account_number");
                String bankName = resultSet.getString("bank_name");
                LocalDate createdAt = resultSet.getDate("created_At").toLocalDate();
                double balance = resultSet.getDouble("balance");
                Currency currency = Currency.getInstance(resultSet.getString("currency"));
                String clientName = resultSet.getString("client_name");
                int ownerId = resultSet.getInt("owner_id");

                Client owner = new Client();
                owner.setName(clientName);
                owner.setId(ownerId);

                BankAccount bankAccount = new BankAccount();
                bankAccount.setAccountNumber(bankAccountNumber);
                bankAccount.setBankName(bankName);
                bankAccount.setCreatedAt(createdAt);
                bankAccount.setBalance(balance);
                bankAccount.setCurrency(currency);

                bankAccount.setOwner(owner);
                owner.addAccount(bankAccount);

                bankAccounts.add(bankAccount);
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return bankAccounts;
    }

    public void save(BankAccount bankAccount) {
        ClientDAO clientDAO = new ClientDAO();

        if (accountExists(bankAccount.getAccountNumber())) {
            System.out.println("Account with name '" + bankAccount.getAccountNumber() + "' already exists.");
            return;
        }

        String clientName = bankAccount.getOwner().getName();
        if (clientDAO.clientExists(clientName)) {
            bankAccount.setOwner(clientDAO.getByName(clientName).orElseThrow()); //sql запрос
        }


        String sql = "INSERT INTO BankAccount (account_number, bank_name, created_at, balance, currency, owner_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, bankAccount.getAccountNumber());
            preparedStatement.setString(2, bankAccount.getBankName());
            preparedStatement.setDate(3, java.sql.Date.valueOf(bankAccount.getCreatedAt()));
            preparedStatement.setDouble(4, bankAccount.getBalance());
            preparedStatement.setString(5, bankAccount.getCurrency().getCurrencyCode());
            preparedStatement.setInt(6, bankAccount.getOwner().getId());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating bank account failed, no rows affected.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean accountExists(String accountNumber) {
        String sql = "SELECT COUNT(*) FROM bankaccount WHERE account_number = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void update(BankAccount bankAccount, String[] params) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE BankAccount SET ");
        List<Object> values = new ArrayList<>();

        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            switch (param) {
                case "accountNumber" -> {
                    sqlBuilder.append("account_number = ?");
                    values.add(bankAccount.getAccountNumber());
                }
                case "bankName" -> {
                    sqlBuilder.append("bank_name = ?");
                    values.add(bankAccount.getBankName());
                }
                case "createdAt" -> {
                    sqlBuilder.append("created_at = ?");
                    values.add(Date.parse(String.valueOf(bankAccount.getCreatedAt())));
                }
                case "balance" -> {
                    sqlBuilder.append("balance = ?");
                    values.add(bankAccount.getBalance());
                }
                case "currency" -> {
                    sqlBuilder.append("currency = ?");
                    values.add(bankAccount.getCurrency().getCurrencyCode());
                }
                // Добавьте обработку других полей по аналогии
            }

            if (i < params.length - 1) {
                sqlBuilder.append(", ");
            }
        }

        sqlBuilder.append(" WHERE account_number = ?");
        values.add(bankAccount.getAccountNumber());

        String sql = sqlBuilder.toString();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                preparedStatement.setObject(i + 1, values.get(i));
            }

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating bank account failed, no rows affected.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void deleteById(String accountNumber) {
        String sql = "DELETE FROM BankAccount WHERE account_number = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountNumber);
            preparedStatement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
