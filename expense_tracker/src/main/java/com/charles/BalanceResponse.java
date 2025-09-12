package com.charles;

public class BalanceResponse {
    private final double totalBalance;
    private final double totalIncome;
    private final double totalExpenses;
    private final String currency;

    public BalanceResponse(double totalBalance, double totalIncome, double totalExpenses, String currency) {
        this.totalBalance = totalBalance;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.currency = currency;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public String getCurrency() {
        return currency;
    }
}