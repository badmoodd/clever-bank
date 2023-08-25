package org.valoshka.cleverBank.dao;

import org.valoshka.cleverBank.models.Client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
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
    public Optional<Client> get(int id) {
        String sql = "SELECT * FROM Client WHERE client_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
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

    public Optional<Client> getByName(String clientName) {
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
    }

    public boolean clientExists(String name) {
        String sql = "SELECT COUNT(*) FROM Client WHERE name = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
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

    @Override
    public void update(Client client, String[] params) {
        if (params == null || params.length == 0) {
            return;
        }

        StringBuilder sqlBuilder = new StringBuilder("UPDATE Client SET ");
        List<Object> values = new ArrayList<>();

        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                sqlBuilder.append(", ");
            }
            String paramName = params[i];
            sqlBuilder.append(paramName).append(" = ?");
            values.add(getFieldValueByName(client, paramName));
        }

        sqlBuilder.append(" WHERE client_id = ?");
        values.add(client.getId());

        String sql = sqlBuilder.toString();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                preparedStatement.setObject(i + 1, values.get(i));
            }

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("No client with ID " + client.getId() + " found to update.");
            } else {
                System.out.println("Client with ID " + client.getId() + " updated successfully.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Object getFieldValueByName(Client client, String fieldName) {
        try {
            Field field = client.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(client);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM Client WHERE client_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("No client with ID " + id + " found to delete.");
            } else {
                System.out.println("Client with ID " + id + " deleted successfully.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    // Остальные методы для работы с базой данных
}
