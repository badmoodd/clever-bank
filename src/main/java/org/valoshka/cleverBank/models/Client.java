package org.valoshka.cleverBank.models;


import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a client in the banking system. Clients can have multiple bank accounts.
 */
@NoArgsConstructor
@RequiredArgsConstructor
public class Client {

    /**
     * Adds a bank account to the client's list of accounts and sets the owner of the account.
     *
     * @param bankAccountToAdd The bank account to add to the client.
     */
    public void addAccount(BankAccount bankAccountToAdd) {
        if (this.accountsList == null) {
            this.accountsList = new ArrayList<>();
        }
        accountsList.add(bankAccountToAdd);
        bankAccountToAdd.setOwner(this);
    }

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    @NonNull
    private String name;

    @Getter
    private List<BankAccount> accountsList;

}
