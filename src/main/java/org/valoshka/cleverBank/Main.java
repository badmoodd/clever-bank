package org.valoshka.cleverBank;

import org.valoshka.cleverBank.dao.BankAccountDAO;
import org.valoshka.cleverBank.dao.ClientDAO;
import org.valoshka.cleverBank.models.BankAccount;
import org.valoshka.cleverBank.models.Client;
import java.util.*;


public class Main {
    public static void main(String[] args) {
        // objects to work with bd
        ClientDAO clientDAO = new ClientDAO();
        BankAccountDAO bankAccountDAO = new BankAccountDAO();

        //Load some entities to tables


        //Генерируем клиентов и их связи со счетами в бд
        // Создаем список клиентов
        List<Client> clients = new ArrayList<>();

        // Создаем 20 клиентов и добавляем к ним счета
        for (int i = 1; i <= 20; i++) {
            String clientName = "Клиент " + i; // Уникальное имя клиента
            Client client = new Client(clientName);

            // Создаем два уникальных банковских счета для каждого клиента
            Currency currency = Currency.getInstance("BYN");
            BankAccount firstBankAccount = new BankAccount(generateUniqueAccountNumber(), "Банк 1", currency);
            BankAccount secondBankAccount = new BankAccount(generateUniqueAccountNumber(), "Банк 2", currency);

            // Добавляем счета к клиенту
            client.addAccount(firstBankAccount);
            client.addAccount(secondBankAccount);
            clientDAO.save(client);
            bankAccountDAO.save(firstBankAccount);
            bankAccountDAO.save(secondBankAccount);

            // Добавляем клиента в список
            clients.add(client);
        }

        // Выводим информацию о клиентах и их счетах
        for (Client client : clients) {
            System.out.println("Клиент: " + client.getName());
            List<BankAccount> accounts = client.getAccountsList();
            for (BankAccount account : accounts) {
                System.out.println("Счет: " + account.getAccountNumber());
            }
            System.out.println();
        }

    }

    // Генерируем уникальный номер счета
    private static String generateUniqueAccountNumber() {
        StringBuilder accountNumber = new StringBuilder("BY");
        Random random = new Random();
        for (int i = 0; i < 26; i++) { // 26 цифр после "BY"
            int digit = random.nextInt(10);
            accountNumber.append(digit);
            if (i % 4 == 3 && i < 25) {
                accountNumber.append(" ");
            }
        }
        return accountNumber.toString();
    }
}