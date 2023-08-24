package org.valoshka.cleverBank;

import org.valoshka.cleverBank.dao.ClientDAO;
import org.valoshka.cleverBank.models.Bank;
import org.valoshka.cleverBank.models.BankAccount;
import org.valoshka.cleverBank.models.Client;

import java.util.Currency;
import java.util.Optional;


public class Main {
    public static void main(String[] args) {

        System.out.println("Hello and welcome!");

        Currency currency = Currency.getInstance("BYN");
        System.out.println(currency.getSymbol());
        Client client = new Client("Bасильков Егор Дмитриевич");
        Bank bank = new Bank();
        bank.setName("Clever-Bank");
        BankAccount bankAccount =
                new BankAccount("AS12 ASDG 1200 2132 ASDA 353A 2132", bank, currency);
        bankAccount.setOwner(client);
        ClientDAO clientDAO = new ClientDAO();
//        clientDAO.save(client);
//        Optional<Client> m = clientDAO.get(6);
//        System.out.println(m.map(Client::getName));
//        System.out.println(m.map(Client::getId));
//        clientDAO.deleteById(3);
//        clientDAO.deleteById(4);
        clientDAO.deleteById(5);clientDAO.deleteById(6);



    }
}