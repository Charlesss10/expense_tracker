package com.charles;

public class TransactionRequest {
    private Transaction transaction;
    private String type;
    private int accountId;

    // Getters and setters
    public Transaction getTransaction() {
        return this.transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
}