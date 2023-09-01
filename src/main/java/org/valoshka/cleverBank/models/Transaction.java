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

    public static boolean depositAndWithdrawal(String targetAccountNumber, double amount, TransactionType transactionType) {
        if (amount < 0) {
            System.out.println("Your amount should be more than Zero");
            return false;
        }

        BankAccountDAO bankAccountDAO = new BankAccountDAO();
        Optional<BankAccount> optionalAccount = bankAccountDAO.get(targetAccountNumber);

        if (optionalAccount.isPresent()) {
            BankAccount targetAccount = optionalAccount.get();
            synchronized (targetAccount) {
                try {
                    double currentBalance = targetAccount.getBalance();
                    double newBalance = (transactionType == TransactionType.DEPOSIT)
                            ? currentBalance + amount
                            : currentBalance - amount;

                    if (newBalance < 0 && transactionType == TransactionType.WITHDRAWAL) {
                        System.out.println("Insufficient funds");
                        return false;
                    }

                    targetAccount.setBalance(newBalance);

                    // Firstly update account
                    // We are working with target account balance!
                    bankAccountDAO.update(targetAccount, new String[]{String.valueOf(targetAccount.getBalance())});

                    Transaction transaction = new Transaction(
                            transactionType,
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
            System.out.println("Account doesn't exist");
            return false;
        }
    }

    public static boolean transfer(String sourceAccountNumber, String targetAccountNumber, double amount) {
        if (amount < 0) {
            System.out.println("Your amount should be more than Zero");
            return false;
        }

        BankAccountDAO bankAccountDAO = new BankAccountDAO();
        TransactionDAO transactionDAO = new TransactionDAO();
        Optional<BankAccount> optionalSourceAccount = bankAccountDAO.get(sourceAccountNumber);
        Optional<BankAccount> optionalTargetAccount = bankAccountDAO.get(targetAccountNumber);

        if (optionalSourceAccount.isPresent() && optionalTargetAccount.isPresent()) {
            BankAccount sourceAccount = optionalSourceAccount.get();
            BankAccount targetAccount = optionalTargetAccount.get();
            synchronized (targetAccount) {
                try {
                    double newBalanceSourceAccount = sourceAccount.getBalance() - amount;
                    double newBalanceTargetAccount = targetAccount.getBalance() + amount;

                    if (newBalanceSourceAccount < 0) {
                        System.out.println("Insufficient funds");
                        Transaction transaction = new Transaction(
                                TransactionType.TRANSFER,
                                sourceAccount,
                                targetAccount,
                                amount,
                                targetAccount.getCurrency());
                        transaction.setTransactionStatus(TransactionStatus.FAILED);
                        transactionDAO.save(transaction);
                        return false;
                    }

                    sourceAccount.setBalance(newBalanceSourceAccount);
                    targetAccount.setBalance(newBalanceTargetAccount);

                    // Firstly update account
                    // We are working with target account balance!
                    bankAccountDAO.update(sourceAccount, new String[]{String.valueOf(sourceAccount.getBalance())});
                    bankAccountDAO.update(targetAccount, new String[]{String.valueOf(targetAccount.getBalance())});

                    Transaction transaction = new Transaction(
                            TransactionType.TRANSFER,
                            sourceAccount,
                            targetAccount,
                            amount,
                            targetAccount.getCurrency());
                    transaction.setTransactionStatus(TransactionStatus.COMPLETED);
                    transactionDAO.save(transaction);

                    Thread.sleep(100);
                    return true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } else {
            System.out.println("Account doesn't exist");
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

