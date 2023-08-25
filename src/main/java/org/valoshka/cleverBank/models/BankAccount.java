package org.valoshka.cleverBank.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Currency;

@NoArgsConstructor
public class BankAccount {

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
