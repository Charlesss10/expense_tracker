package com.charles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service("appDataStorage")
public class DataStorage {
    private final TransactionManager transactionManager;

    public DataStorage(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // Save transactions to CSV file Console
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

    // Save transactions to CSV file Browser
    public String exportTransactionsAsCSV(int accountId) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("TransactionId,Type,Amount,Category,Source,Description,Date\n");
        for (Transaction transaction : transactionManager.getTransactions(accountId)) {
            sb.append(transaction.getTransactionId()).append(",");
            sb.append(transaction.getType()).append(",");
            sb.append(String.format("%.2f", transaction.getAmount())).append(",");
            sb.append(transaction.getCategory()).append(",");
            sb.append(transaction.getSource()).append(",");
            sb.append(transaction.getDescription()).append(",");
            sb.append(transaction.getDate().toString()).append("\n");
        }
        return sb.toString();
    }

    // Load transactions from CSV file Console
    public void loadDataConsole(String filePath, int accountId) throws IOException, SQLException {
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
        System.out.println("Data loaded from uploaded file for account: " + accountId);
    }

    // Load transactions from CSV file Browser
    public void loadData(InputStream inputStream, int accountId) throws IOException, SQLException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
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
                    transactionManager.addTransaction(transaction, transaction.getType(), accountId);
                } catch (ClassNotFoundException ex) {
                    // Handle exception if needed
                }
            }
        }
        System.out.println("Data loaded from uploaded file for account: " + accountId);
    }
}