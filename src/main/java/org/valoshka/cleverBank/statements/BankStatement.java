package org.valoshka.cleverBank.statements;


import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.AsciiTableException;
import de.vandermeer.asciithemes.TA_GridThemes;
import org.valoshka.cleverBank.enums.TransactionType;
import org.valoshka.cleverBank.models.Transaction;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A utility class for creating and saving bank transaction checks.
 */
public class BankStatement {
    // How to get the generated id of transaction? Using date and time! It's unique! I'm genius

    /**
     * Saves a transaction check to a file in the "checks" folder.
     *
     * @param transaction The transaction for which to create and save a check.
     */
    public static void saveTransactionCheck(Transaction transaction) {
        String checkTemplate;
        checkTemplate = createCheckTemplate(transaction);
        saveCheck(checkTemplate, transaction.getId());
    }

    /**
     * Saves a check template to a file with a given check ID.
     *
     * @param templateCheck The check template to save.
     * @param checkId       The unique ID of the check.
     */
    private static void saveCheck(String templateCheck, int checkId) {
        String folderPath = "checks";

        Path folder = Paths.get(folderPath);

        if (!folder.toFile().exists()) {
            boolean created = folder.toFile().mkdirs();
            if (created) {
                System.out.println("Folder created: " + folderPath);
            } else {
                System.err.println("Failed to create folder: " + folderPath);
            }
        }

        String fileName = String.format("check_%d.txt", checkId);

        String filePath = Paths.get(folderPath, fileName).toString();

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(templateCheck);
            System.out.println("Check saved to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error saving check.");
        }
    }

    /**
     * Creates a check template for a given transaction.
     *
     * @param transaction The transaction for which to create a check template.
     * @return The check template as a formatted string.
     */
    private static String createCheckTemplate(Transaction transaction) {
        String name = String.format("%44s", "Банковский чек\n");
        String overallCheck;

        try {
            AsciiTable at = new AsciiTable();

            at.addRule();
            at.addRow("Чек:", transaction.getId());
            at.addRule();
            at.addRow(transaction.getDateTimeOfTransaction().toLocalDate(), transaction.getDateTimeOfTransaction().toLocalTime());
            at.addRule();
            at.addRow("Тип транзакции:", transaction.getTransactionType());
            at.addRule();

            if (transaction.getTransactionType() == TransactionType.TRANSFER) {
                addSourceAccountInfoToTable(at, transaction);
            }

            at.addRow("Банк получателя:", transaction.getTargetAccount().getBankName());
            at.addRule();
            at.addRow("Счёт получателя:", transaction.getSourceAccount().getAccountNumber());
            at.addRule();
            at.addRow("Сумма:", transaction.getAmount() + " " + transaction.getCurrency().getSymbol());
            at.addRule();

            at.getContext().setWidth(74);
            at.getContext().setGridTheme(TA_GridThemes.OUTSIDE);

            overallCheck = name + at.render();

        } catch (NullPointerException | AsciiTableException e) {
            e.printStackTrace();
            return "Can't create check template";
        }

        return overallCheck;
    }

    /**
     * Adds source account information to the ASCII table.
     *
     * @param at           The ASCII table to which to add source account information.
     * @param transaction  The transaction containing source account details.
     */
    private static void addSourceAccountInfoToTable(AsciiTable at, Transaction transaction) {
        at.addRow("Банк отправителя:", transaction.getSourceAccount().getBankName());
        at.addRule();
        at.addRow("Банк отправителя:", transaction.getSourceAccount().getBankName());
        at.addRule();
    }

}
