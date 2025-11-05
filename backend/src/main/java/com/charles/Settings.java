package com.charles;

import java.sql.SQLException;

import org.springframework.stereotype.Service;

@Service("appSettings")
public class Settings {
    private final UserAccountManager userAccountManager;
    private final Database database = Database.getInstance();

    public Settings(UserAccountManager userAccountManager) {
        this.userAccountManager = userAccountManager;
    }

    public boolean changeAccountCurrency(int accountId, String newCurrency) throws SQLException {
        boolean result = database.updateAccountCurrency(accountId, newCurrency);
        return result;
    }

    public String getAccountCurrency(int accountId) throws SQLException {
        UserAccount userAccount = userAccountManager.getUserAccount(accountId);
        return userAccount.getCurrency();
    }
}