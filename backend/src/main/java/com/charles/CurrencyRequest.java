package com.charles;

public class CurrencyRequest {
    private int accountId;
    private String newCurrency;

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getNewCurrency() {
        return newCurrency;
    }

    public void setNewCurrency(String newCurrency) {
        this.newCurrency = newCurrency;
    }
}