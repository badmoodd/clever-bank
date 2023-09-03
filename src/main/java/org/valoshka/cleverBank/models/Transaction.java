package org.valoshka.cleverBank.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.valoshka.cleverBank.dao.BankAccountDAO;
import org.valoshka.cleverBank.dao.TransactionDAO;
import org.valoshka.cleverBank.enums.TransactionStatus;
import org.valoshka.cleverBank.enums.TransactionType;
import org.valoshka.cleverBank.statements.BankStatement;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

/**
 * Represents a financial transaction in the banking system.
 * This class provides methods for deposit, withdrawal, and transfers between accounts.
 */
@NoArgsConstructor
public class Transaction {

    /**
     * Creates a new transaction with the specified details.
     *
     * @param transactionType  The type of the transaction (e.g., DEPOSIT, WITHDRAWAL, TRANSFER).
     * @param sourceAccount    The source bank account for the transaction.
     * @param targetAccount    The target bank account for the transaction.
     * @param amount           The amount of money involved in the transaction.
     * @param currency         The currency used for the transaction.
     */
    public Transaction(TransactionType transactionType, BankAccount sourceAccount, BankAccount targetAccount, double amount, Currency currency) {
        this.dateTimeOfTransaction = LocalDateTime.now();
        this.transactionType = transactionType;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.currency = currency;
    }

    /**
     * Performs a deposit or withdrawal transaction for the specified target account.
     *
     * @param targetAccountNumber The account number of the target account.
     * @param amount              The amount of money to deposit or withdraw.
     * @param transactionType     The type of the transaction (DEPOSIT or WITHDRAWAL).
     * @return True if the transaction is successful, false otherwise.
     */
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
                    int transactionId = transactionDAO.save(transaction);
                    transaction.setId(transactionId);

                    //save transaction check to folder
                    BankStatement.saveTransactionCheck(transaction);

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

    /**
     * Performs a transfer transaction between two bank accounts.
     *
     * @param sourceAccountNumber The account number of the source account.
     * @param targetAccountNumber The account number of the target account.
     * @param amount              The amount of money to transfer.
     * @return True if the transaction is successful, false otherwise.
     */
    public static boolean transfer(String sourceAccountNumber, String targetAccountNumber, double amount) {
        if (amount < 0) {
            System.out.println("Your amount should be more than Zero");
            return false;
        }

        int transactionId;
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

                        transactionId = transactionDAO.save(transaction);
                        transaction.setId(transactionId);

                        //save transaction check to folder
                        BankStatement.saveTransactionCheck(transaction);
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

                    transactionId = transactionDAO.save(transaction);
                    transaction.setId(transactionId);

                    //save transaction check to folder
                    BankStatement.saveTransactionCheck(transaction);

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

