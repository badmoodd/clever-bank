package org.valoshka.cleverBank.dao;

import org.valoshka.cleverBank.dao.utils.DatabaseUtils;
import org.valoshka.cleverBank.models.Client;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class ClientDAO implements Dao<Client> {
    private static final Properties properties = new Properties();

    public ClientDAO() {
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

    @Override
    public Optional<Client> get(String clientName) {  // get existing object from db else can return empty optional
        String sql = "SELECT * FROM Client WHERE name = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, clientName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int clientId = resultSet.getInt("client_id");
                    String name = resultSet.getString("name");

                    Client client = new Client();
                    client.setName(name);
                    client.setId(clientId);

                    return Optional.of(client);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Client> getAll() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM Client";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int clientId = resultSet.getInt("client_id");
                String name = resultSet.getString("name");
                Client client = new Client(name);
                client.setId(clientId);

                clients.add(client);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clients;
    }

    @Override
    public void save(Client client) {
        if (clientExists(client.getName())) {
            System.out.println("Client with name '" + client.getName() + "' already exists.");
            return;
        }
        String sql = "INSERT INTO Client (name) VALUES (?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, client.getName());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating client failed, no rows affected.");
            }
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int clientId = generatedKeys.getInt(1);
                    client.setId(clientId);
                } else {
                    throw new SQLException("Creating client failed, no ID obtained.");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    } // save New object else nothing to do

    public boolean clientExists(String name) {
        try (Connection connection = getConnection()) {
            return DatabaseUtils.recordExists(connection, "Client", "name", name);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //(client, newString[]{"Фамилия Имя Отчество"}
    @Override
    public void update(Client client, String[] params) {
        if (params == null || params.length == 0) {
            return;
        }

        String sql = "UPDATE client SET name=? WHERE name=?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, params[0]);
            preparedStatement.setString(2, client.getName());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("No client with ID '" + client.getId() + "' found to update.");
            } else {
                System.out.println("Client with ID '" + client.getId() + "' updated successfully.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteByName(String clientName) {  // delete if exist else nothing
        String sql = "DELETE FROM Client WHERE name = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, clientName);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("No client with ID '" + clientName + "' found to delete.");
            } else {
                System.out.println("Client with ID '" + clientName + "' deleted successfully.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    // Остальные методы для работы с базой данных
}
