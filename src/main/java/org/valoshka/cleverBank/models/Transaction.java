package org.valoshka.cleverBank.models;

import org.valoshka.cleverBank.enums.TransactionStatus;
import org.valoshka.cleverBank.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.Currency;

public class Transaction {

    private int id;

    private LocalDateTime dateTimeOfTransaction;

    private TransactionType transactionType;

    private TransactionStatus transactionStatus;

    private BankAccount sourceAccount;

    private BankAccount targetAccount;

    private double amount;

    private Currency currency;
}

