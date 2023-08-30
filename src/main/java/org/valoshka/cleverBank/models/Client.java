package org.valoshka.cleverBank.models;


import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@RequiredArgsConstructor
public class Client {

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
