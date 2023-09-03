# CleverBank Console Application

## Main Entities
- Bank
- BankAccount
- Client
- Transaction

## Overview
This application models a bank with clients who can have one or many bank accounts. There are three types of transactions: deposit, withdrawal, and transfers between accounts. The application utilizes three tables to store bank accounts, clients, and transactions.

### Table Relationships
**Client and BankAccount:**
- The "BankAccount" table contains a column called "owner_id," which references the "client_id" column in the "Client" table. This relationship signifies that each bank account is associated with one client, and each client can have multiple bank accounts.

**Transaction and BankAccount:**
- The "Transaction" table contains two columns, "source_account" and "target_account," both of which reference the "account_number" column in the "BankAccount" table. These columns represent the source and target accounts for each transaction. This relationship allows for tracking which accounts are involved in each transaction.

These relationships help organize the database structure for tracking clients, their bank accounts, and transactions between them.

## Usage
1. Clone this repository.
2. Change directory to `src/main/resources/postgreSQL`.
3. Run the following command to set up the PostgreSQL database with Docker:

```sh
docker-compose up -d
```


4. Initialize or fill clients and bank accounts in the `Main` class. You can set relations between clients and accounts using the `ClientDAO` and `BankAccountDAO` classes to save entities to the database. For example:
```java
Client first = new Client("Васильков Егор Дмитриевич");
Currency currency = Currency.getInstance("BYN");
BankAccount firstBankAccount = new BankAccount("QA12 JKDG 5600 2132 ASDA 903A 2132", "Belarusbank", currency);
BankAccount secondBankAccount = new BankAccount("QWER 1234 ABCD 5678 EFGH 9012 IJKL", "Priorbank", currency);

first.addAccount(firstBankAccount);
// or
secondBankAccount.setOwner(first);
```

```java
// Deposit and withdrawal
Transaction.depositAndwithdrawal("BY9735 5326 7540 9130 5142 9475 33", 89.90, TransactionType.DEPOSIT);
Transaction.depositAndwithdrawal("BY9735 5326 7540 9130 5142 9475 33", 15.90, TransactionType.WITHDRAWAL);

// Transfer
Transaction.transfer("BY9503 5768 6686 5952 4058 0120 46", "BY7611 8364 6495 2382 6916 0859 75", 20.00);
```
