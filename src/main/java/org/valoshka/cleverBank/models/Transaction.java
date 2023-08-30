package org.valoshka.cleverBank.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.valoshka.cleverBank.dao.BankAccountDAO;
import org.valoshka.cleverBank.dao.TransactionDAO;
import org.valoshka.cleverBank.enums.TransactionStatus;
import org.valoshka.cleverBank.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

@NoArgsConstructor
public class Transaction {

    public Transaction(TransactionType transactionType, BankAccount sourceAccount, BankAccount targetAccount, double amount, Currency currency) {
        this.dateTimeOfTransaction = LocalDateTime.now();
        this.transactionType = transactionType;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.currency = currency;
    }

    public static boolean deposit(String targetAccountNumber, double amount) {
        if (amount < 0) {
            System.out.println("Your amount should bbe more than Zero");
            return false;
        }

        BankAccountDAO bankAccountDAO = new BankAccountDAO();
        Optional<BankAccount> optionalAccount = bankAccountDAO.get(targetAccountNumber);

        if (optionalAccount.isPresent()) {
            BankAccount targetAccount = optionalAccount.get();
            synchronized (targetAccount) {
                try {
                    double currentBalance = targetAccount.getBalance();
                    double newBalance = currentBalance + amount;
                    targetAccount.setBalance(newBalance);

                    // firstly update account
                    // we are working with target account balance!
                    bankAccountDAO.update(targetAccount, new String[]{String.valueOf(targetAccount.getBalance())});

                    Transaction transaction = new Transaction(
                            TransactionType.DEPOSIT,
                            targetAccount,
                            targetAccount,
                            amount,
                            targetAccount.getCurrency());
                    transaction.setTransactionStatus(TransactionStatus.COMPLETED);

                    TransactionDAO transactionDAO = new TransactionDAO();
                    transactionDAO.save(transaction);

                    Thread.sleep(100);
                    return true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } else {
            System.out.println("Account don't exist");
            return false;
        }
    }


    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private LocalDateTime dateTimeOfTransaction;

    @Getter
    @Setter
    private TransactionType transactionType;

    @Getter
    @Setter
    private TransactionStatus transactionStatus;

    @Getter
    @Setter
    private BankAccount sourceAccount;

    @Getter
    @Setter
    private BankAccount targetAccount;

    @Getter
    @Setter
    private double amount;

    @Getter
    @Setter
    private Currency currency;
}

