package com.charles;

public class LoginResponse {
    private final String token;
    private final int accountId;

    public LoginResponse(String token, int accountId) {
        this.token = token;
        this.accountId = accountId;
    }

    public String getToken() {
        return token;
    }

    public int getAccountId() {
        return accountId;
    }
}
