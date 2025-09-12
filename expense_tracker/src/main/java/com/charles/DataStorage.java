package com.charles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class DataStorage {
    private final TransactionManager transactionManager;

    public DataStorage(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // Save transactions to CSV file
    public void saveData(String filePath, int accountId) throws IOException, SQLException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("TransactionId,Type,Amount,Category,Source,Description,Date\n"); // CSV Header
            for (Transaction transaction : transactionManager.getTransactions(accountId)) {
                writer.write(String.format("%s,%s,%.2f,%s,%s,%s,%s\n",
                        transaction.getTransactionId(), transaction.getType(), transaction.getAmount(),
                        transaction.getCategory(), transaction.getSource(),
                        transaction.getDescription(), transaction.getDate().toString()));
            }
        }
        System.out.println("Data saved to: " + filePath + "\n");
    }

    // Load transactions from CSV file
    public void loadData(String filePath, int accountId) throws IOException, SQLException {
        List<Transaction> transactions = new ArrayList<>();
        // Create new file if it does not exist
        File file = new File(filePath);
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write("Type,Amount,Category,Source,Description,Date\n");
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 6) { // Ensure valid format excluding transactionId
                    Transaction transaction = new Transaction();
                    transaction.setType(data[0]);
                    transaction.setAmount(Double.parseDouble(data[1]));
                    transaction.setCategory(data[2]);
                    transaction.setSource(data[3]);
                    transaction.setDescription(data[4]);
                    transaction.setDate(Date.valueOf(data[5]));
                    transactions.add(transaction);
                }
            }

            for (Transaction transaction : transactions) {
                try {
                    transactionManager.addTransaction(transaction, transaction.getType(),
                            accountId);
                } catch (ClassNotFoundException ex) {
                }
            }
        }

        System.out.println("Data loaded from: \n" + filePath);
    }
}