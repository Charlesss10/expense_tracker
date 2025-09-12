package com.charles;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class ExpenseSummary extends TransactionList {
    private double totalExpenses;
    private String highestCategory;
    private Map<String, String> expensesPercentage = new HashMap<>();
    private Map<String, String> expensesByCategory = new HashMap<>();

    public ExpenseSummary(Settings settings, TransactionManager transactionManager)
            throws SQLException {
        super(settings, transactionManager);
    }

    public boolean getExpensesSummary(int accountId) throws SQLException {
        if (transactionManager.getTransactions(accountId) == null
                || transactionManager.getTransactions(accountId).isEmpty()) {
            System.out.println("No transactions available.");
            return false;
        }
        this.totalExpenses = 0.0;
        this.expensesCalculator(accountId);
        this.categorizeExpenses(accountId);
        return true;
    }

    public Map<String, Object> getExpensesSummaryData(int accountId) throws SQLException {
        getExpensesSummary(accountId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalExpenses", totalExpenses);
        summary.put("highestCategory", highestCategory);
        summary.put("expensesPercentage", expensesPercentage);
        summary.put("expensesByCategory", expensesByCategory);
        return summary;
    }

    // Helper
    public void expensesCalculator(int accountId) throws SQLException {
        for (Transaction transaction : transactionManager.getTransactions(accountId)) {
            if (transaction.getType().equalsIgnoreCase("EXPENSES")) {
                this.totalExpenses = this.totalExpenses + transaction.getAmount();
            }
        }
    }

    // Helper
    public void categorizeExpenses(int accountId) throws SQLException {
        // Group by Category
        if (this.expensesByCategory != null) {
            this.expensesByCategory = new HashMap<>();
        }
        this.expensesByCategory = transactionManager.getTransactions(accountId).stream()
                .filter(t -> t.getType().equalsIgnoreCase("EXPENSES")) // Filter only expenses
                .collect(Collectors.groupingBy(
                        Transaction::getCategory, // Group by category
                        Collectors.collectingAndThen(
                                Collectors.summingDouble(t -> t.getAmount()), // Sum amounts
                                String::valueOf // Convert the sum to String
                        )));

        // Highest Category & Percentage
        double highestCategoryAmount = 0.0;
        if (this.expensesPercentage != null) {
            this.expensesPercentage = new HashMap<>();
        }

        for (Map.Entry<String, String> entry : this.expensesByCategory.entrySet()) {
            double currentCategoryAmount = Double.parseDouble(entry.getValue());
            if (currentCategoryAmount > highestCategoryAmount) {
                highestCategoryAmount = currentCategoryAmount;
                this.highestCategory = entry.getKey();
            }
            double percentage = (Double.parseDouble(entry.getValue()) / this.totalExpenses) * 100;
            int wholeNumberPercentage = (int) Math.round(percentage);
            this.expensesPercentage.put(entry.getKey(), String.valueOf(wholeNumberPercentage));
        }
    }

    // Getters
    public double getTotalExpenses() {
        return this.totalExpenses;
    }

    public String getHighestCategory() {
        return this.highestCategory;
    }

    public Map<String, String> getExpensesPercentage() {
        return this.expensesPercentage;
    }

    public Map<String, String> getExpensesByCategory() {
        return this.expensesByCategory;
    }

    @Override
    public void update() {
        System.out.println("Expense Summary: Update in Transaction Manager!\n");
    }
}