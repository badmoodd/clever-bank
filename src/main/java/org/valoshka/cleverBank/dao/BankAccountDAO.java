package org.valoshka.cleverBank.dao;

import org.valoshka.cleverBank.dao.utils.DatabaseUtils;
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

/**
 * Data Access Object (DAO) for managing bank account data in the database.
 */
public class BankAccountDAO implements Dao<BankAccount> {

    private static final Properties properties = new Properties();

    /**
     * Constructs a new BankAccountDAO and loads database properties.
     */
    public BankAccountDAO() {
        try (InputStream inputStream = ClientDAO.class.getClassLoader().getResourceAsStream("postgreSQL/database.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.out.println("Error reading database properties file");
            e.printStackTrace();
        }
    }

    /**
     * Gets a database connection using the properties loaded during construction.
     *
     * @return A database connection.
     * @throws SQLException           If a database access error occurs.
     * @throws ClassNotFoundException If the database driver class is not found.
     */
    public Connection getConnection() throws SQLException, ClassNotFoundException {
        return ConnectionManager.getConnection(properties);
    }

    /**
     * Retrieves a bank account from the database by account number.
     *
     * @param accountNumber The account number of the bank account to retrieve.
     * @return An Optional containing the retrieved bank account if found, or an empty Optional if not found.
     */
    @Override
    public Optional<BankAccount> get(String accountNumber) {
        String sql = "SELECT ba.*, c.name as client_name FROM BankAccount ba " +
                "INNER JOIN Client c ON ba.owner_id = c.client_id " +
                "WHERE ba.account_number = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(createBankAccountFromResultSet(resultSet));
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Retrieves all bank accounts from the database.
     *
     * @return A list of all bank accounts in the database.
     */
    @Override
    public List<BankAccount> getAll() {
        List<BankAccount> bankAccounts = new ArrayList<>();
        String sql = "SELECT ba.*, c.name as client_name FROM BankAccount ba " +
                "INNER JOIN Client c ON ba.owner_id = c.client_id";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                bankAccounts.add(createBankAccountFromResultSet(resultSet));
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return bankAccounts;
    }


    private BankAccount createBankAccountFromResultSet(ResultSet resultSet) throws SQLException {
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

        return bankAccount;
    }

    /**
     * Saves a bank account to the database.
     *
     * @param bankAccount The bank account to save.
     */
    @Override
    public void save(BankAccount bankAccount) {
        ClientDAO clientDAO = new ClientDAO();

        if (accountExists(bankAccount.getAccountNumber())) {
            System.out.println("Account with name '" + bankAccount.getAccountNumber() + "' already exists.");
            return;
        }

        String clientName = bankAccount.getOwner().getName();
        if (clientDAO.clientExists(clientName)) {
            bankAccount.setOwner(clientDAO.get(clientName).orElseThrow()); //sql запрос
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

    /**
     * Checks if a bank account with the given account number already exists in the database.
     *
     * @param accountNumber The account number to check.
     * @return True if a bank account with the given account number exists, false otherwise.
     */
    public boolean accountExists(String accountNumber) {
        try (Connection connection = getConnection()) {
            return DatabaseUtils.recordExists(connection, "bankaccount", "account_number", accountNumber);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates a bank account's balance in the database.
     *
     * @param bankAccount The bank account to update.
     * @param params      An array of parameters for the update operation.
     */
    @Override
    public void update(BankAccount bankAccount, String[] params) {

        String sql = "UPDATE bankaccount SET balance=? WHERE account_number=?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setDouble(1, Double.parseDouble(params[0]));
            preparedStatement.setString(2, bankAccount.getAccountNumber());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating bank account failed, no rows affected.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a bank account from the database by account number.
     *
     * @param accountNumber The account number of the bank account to delete.
     */
    @Override
    public void deleteByName(String accountNumber) {
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
