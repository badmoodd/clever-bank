package org.valoshka.cleverBank.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Currency;

/**
 * Represents a bank account in the banking system.
 */
@NoArgsConstructor
public class BankAccount {

    /**
     * Initializes a new bank account with the specified account number, bank name, and currency.
     *
     * @param accountNumber The account number of the bank account.
     * @param bankName      The name of the bank associated with the account.
     * @param currency      The currency of the account.
     */
    public BankAccount(String accountNumber, String bankName, Currency currency) {
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.createdAt = LocalDate.now();
        this.balance = 0;
        this.currency = currency;
    }


    @Getter
    @Setter
    private String accountNumber;

    @Getter
    @Setter
    private String bankName;

    @Getter
    @Setter
    private LocalDate createdAt;

    @Getter
    @Setter
    private double balance;

    @Getter
    @Setter
    private Currency currency;

    @Getter
    @Setter
    private Client owner;

}
