package org.valoshka.cleverBank.statements;


import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.TA_GridThemes;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.valoshka.cleverBank.models.Transaction;

public class BankStatement {
    public static void main(String[] args) {
        AsciiTable at = new AsciiTable();


        at.addRule();
        at.addRow("Чек:", "номер чека");
        at.addRule();
        at.addRow("дата:", "время");
        at.addRule();
        at.addRow("Тип транзакции:", "перевод");
        at.addRule();
        at.addRow("Банк отправителя:", "Clever-bank");
        at.addRule();
        at.addRow("Банк получателя:", "Clever-bank");
        at.addRule();
        at.addRow("Счёт отправителя:", "QA12 JKDG 5600 2132 ASDA 903A 2132");
        at.addRule();
        at.addRow("Счёт получателя:", "QA12 JKDG 5600 2132 ASDA 903A 2132");
        at.addRule();
        at.addRow("Сумма:", "200 BYN");
        at.addRule();

        at.getContext().setWidth(74);

        at.getContext().setGridTheme(TA_GridThemes.OUTSIDE);
        System.out.println(at.render());
    }

    // How to get the generated id of transaction? Using date and time! It's uniq! I'm genius
    public static boolean createDepositWithdrawalCheck(Transaction transaction) {
        try {
            String name = "Банковский чек\n";

            AsciiTable at = new AsciiTable();

            at.addRule();
            at.addRow("Чек:", transaction.getId());
            at.addRule();
            at.addRow(transaction.getDateTimeOfTransaction().toLocalDate(), transaction.getDateTimeOfTransaction().toLocalTime());
            at.addRule();
            at.addRow("Тип транзакции:", transaction.getTransactionType());
            at.addRule();
            at.addRow("Банк:", "Clever-bank");
            at.addRule();
            at.addRow("Номер счета:", transaction.getTargetAccount().getAccountNumber());
            at.addRule();
            at.addRow("Сумма:", transaction.getAmount() + " " + transaction.getCurrency().getSymbol());
            at.addRule();

            at.getContext().setWidth(74);
            at.getContext().setGridTheme(TA_GridThemes.OUTSIDE);

            String overallCheck = name + at.render();
            System.out.println(overallCheck);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
