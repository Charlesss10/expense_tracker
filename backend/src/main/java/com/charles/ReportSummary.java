package com.charles;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service("appReportSummary")
public class ReportSummary extends TransactionList {
    private double totalIncome;
    private double totalExpenses;
    private double totalBalance;
    private String highestCategory;
    private String highestSource;
    private Map<String, String> expensesPercentage = new HashMap<>();
    private Map<String, String> incomePercentage = new HashMap<>();
    private Map<String, String> expensesByCategory = new HashMap<>();
    private Map<String, String> incomeBySource = new HashMap<>();

    public ReportSummary(Settings settings, TransactionManager transactionManager)
            throws SQLException {
        super(settings, transactionManager);
    }

    public boolean generateReportSummary(int accountId, String targetMonth, String targetYear) throws SQLException {
        // Filter transactions based on tragetMonth or targetYear
        List<Transaction> filteredTransactions = transactionManager.getTransactions(accountId).stream()
                .filter(t -> {
                    if (targetMonth != null) {
                        String[] parts = targetMonth.split("-");
                        int filterYear = Integer.parseInt(parts[0]);
                        int filterMonth = Integer.parseInt(parts[1]);

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(t.getDate());
                        int transactionYear = cal.get(Calendar.YEAR);
                        int transactionMonth = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero-based

                        return transactionYear == filterYear && transactionMonth == filterMonth;
                    } else if (targetYear != null) {
                        int filterYear = Integer.parseInt(targetYear);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(t.getDate());
                        int transactionYear = cal.get(Calendar.YEAR);
                        return transactionYear == filterYear;
                    }
                    return true; // If no filter, include all
                })
                .collect(Collectors.toList());

        if (!filteredTransactions.isEmpty()) {
            calculateTotalBalance(filteredTransactions);
            categorizeExpenses(filteredTransactions);
            categorizeIncome(filteredTransactions);

            if (this.highestSource == null) {
                this.highestSource = "N/A";
            }
            if (this.highestCategory == null) {
                this.highestCategory = "N/A";
            }

            return true;
        } else {
            System.out.println("No transaction record found.");
            return false;
        }
    }

    public Map<String, Object> getReportData() throws SQLException {
        Map<String, Object> report = new HashMap<>();
        report.put("totalIncome", this.totalIncome);
        report.put("totalExpenses", this.totalExpenses);
        report.put("totalBalance", this.totalBalance);
        report.put("highestCategory", this.highestCategory);
        report.put("highestSource", this.highestSource);
        report.put("expensesByCategory", this.expensesByCategory);
        report.put("incomeBySource", this.incomeBySource);
        report.put("expensesPercentage", this.expensesPercentage);
        report.put("incomePercentage", this.incomePercentage);

        return report;
    }

    // Helper
    public void clearFilter() {
        this.totalIncome = 0.0;
        this.totalExpenses = 0.0;
        this.totalBalance = 0.0;
        this.highestCategory = null;
        this.highestSource = null;
        this.expensesByCategory = new HashMap<>();
        this.incomeBySource = new HashMap<>();
        this.expensesPercentage = new HashMap<>();
        this.incomePercentage = new HashMap<>();
    }

    // Helper
    public void calculateTotalBalance(List<Transaction> filteredTransactions) throws SQLException {
        this.totalIncome = 0.0;
        this.totalExpenses = 0.0;
        this.totalBalance = 0.0;

        for (Transaction transaction : filteredTransactions) {
            switch (transaction.getType()) {
                case "INCOME" -> {
                    this.totalIncome = this.totalIncome + transaction.getAmount();
                    break;
                }
                case "EXPENSES" -> {
                    this.totalExpenses = this.totalExpenses + transaction.getAmount();
                    break;
                }
                default -> System.out.println("Invalid Type.");
            }
        }
        this.totalBalance = Math.round((this.totalIncome - this.totalExpenses) * 100.0) / 100.0;
        this.totalIncome = Math.round(this.totalIncome * 100.0) / 100.0;
        this.totalExpenses = Math.round(this.totalExpenses * 100.0) / 100.0;
    }

    // Helper
    public void categorizeExpenses(List<Transaction> filteredTransactions) {
        // Group by Category
        if (this.expensesByCategory != null) {
            this.expensesByCategory = new HashMap<>();
        }
        this.expensesByCategory = filteredTransactions.stream()
                .filter(t -> t.getType().equalsIgnoreCase("EXPENSES")) // Filter only expenses
                .collect(Collectors.groupingBy(
                        Transaction::getCategory, // Group by category
                        Collectors.collectingAndThen(
                                Collectors.summingDouble(t -> t.getAmount()), // Sum amounts
                                String::valueOf // Convert the sum to String
                        )));

        // Highest Category & Percentage
        double highestCategoryAmount = 0.0;
        this.highestCategory = null;
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

    // Helper
    public void categorizeIncome(List<Transaction> filteredTransactions) {
        // Group by Category
        if (this.incomeBySource != null) {
            this.incomeBySource = new HashMap<>();
        }
        this.incomeBySource = filteredTransactions.stream()
                .filter(t -> t.getType().equalsIgnoreCase("INCOME")) // Filter only expenses
                .collect(Collectors.groupingBy(
                        Transaction::getSource, // Group by category
                        Collectors.collectingAndThen(
                                Collectors.summingDouble(t -> t.getAmount()), // Sum amounts
                                String::valueOf // Convert the sum to String
                        )));

        // Highest Category & Percentage
        double highestSourceAmount = 0.0;
        this.highestSource = null;
        if (this.incomePercentage != null) {
            this.incomePercentage = new HashMap<>();
        }

        for (Map.Entry<String, String> entry : this.incomeBySource.entrySet()) {
            double currentSourceAmount = Double.parseDouble(entry.getValue());
            if (currentSourceAmount > highestSourceAmount) {
                highestSourceAmount = currentSourceAmount;
                this.highestSource = entry.getKey();
            }
            double percentage = (Double.parseDouble(entry.getValue()) / this.totalIncome) * 100;
            int wholeNumberPercentage = (int) Math.round(percentage);
            this.incomePercentage.put(entry.getKey(), String.valueOf(wholeNumberPercentage));
        }
    }

    public void exportToCSV(String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Report Summary\n");
            writer.write(String.format("Total Income: %.2f\n", this.totalIncome));
            writer.write(String.format("Total Expenses: %.2f\n", this.totalExpenses));
            writer.write(String.format("Total Balance: %.2f\n", this.totalBalance));
            writer.write(String.format("Highest Source: %s\n", this.highestSource));
            writer.write(String.format("Highest Category: %s\n\n", this.highestCategory));

            if (!this.incomeBySource.isEmpty()) {
                writer.write("Income by Source:\n");
                writer.write("Source,Amount,Percentage\n");
                this.incomeBySource.forEach((source, amount) -> {
                    try {
                        writer.write(String.format("%s,%s,%s\n", source, amount, this.incomePercentage.get(source)));
                    } catch (IOException e) {
                    }
                });
                writer.write("\n");
            }

            if (!this.expensesByCategory.isEmpty()) {
                writer.write("Expenses by Category:\n");
                writer.write("Category,Amount,Percentage\n");
                this.expensesByCategory.forEach((category, amount) -> {
                    try {
                        writer.write(
                                String.format("%s,%s,%s\n", category, amount, this.expensesPercentage.get(category)));
                    } catch (IOException e) {
                    }
                });
            }
        }
        System.out.println("Report successfully exported to CSV: " + filePath + "\n");
    }

    // Getters
    public double getTotalBalance() {
        return this.totalBalance;
    }

    public double getTotalIncome() {
        return this.totalIncome;
    }

    public double getTotalExpenses() {
        return this.totalExpenses;
    }

    public String getHighestCategory() {
        return this.highestCategory;
    }

    public String getHighestSource() {
        return this.highestSource;
    }

    public Map<String, String> getIncomeBySource() {
        return this.incomeBySource;
    }

    public Map<String, String> getExpensesByCategory() {
        return this.expensesByCategory;
    }

    public Map<String, String> getExpensesPercentage() {
        return this.expensesPercentage;
    }

    public Map<String, String> getIncomePercentage() {
        return this.incomePercentage;
    }

    @Override
    public void update() {
        System.out.println("Report Summary: Update in Transaction Manager!\n");
    }
}