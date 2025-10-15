package com.charles;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UserAccountManager {
    private List<UserAccount> userAccounts = new ArrayList<>();
    private final Database database = Database.getInstance();

    public UserAccountManager() throws SQLException {
        this.userAccounts = database.fetchUserAccounts();
    }

    // Add User Account
    public boolean addAccount(UserAccount userAccount) throws SQLException {
        if (!verifyAccountByUsername(userAccount.getUsername())) {
            if (userAccount.getCurrency().equalsIgnoreCase("Euro")
                    | userAccount.getCurrency().equalsIgnoreCase("Dollar")) {
                boolean result = database.insertAccount(userAccount);
                if (result) {
                    System.out.println("User Account Added");
                    return true;
                } else {
                    return false;
                }

            } else {
                System.out.println("Invalid Currency Choice!");
                return false;
            }
        }
        System.out.println("Username already exists");
        return false;
    }

    // Edit User Account
    public boolean editUserAccount(UserAccount userAccount) throws SQLException {
        fetchUserAccounts();
        for (UserAccount validAccount : this.userAccounts) {
            if (validAccount.getAccountId() == userAccount.getAccountId()) {
                validAccount.setFirstName(userAccount.getFirstName());
                validAccount.setLastName(userAccount.getLastName());
                validAccount.setUsername(userAccount.getUsername());
                validAccount.setBirthday(userAccount.getBirthday());
                validAccount.setPassword(userAccount.getPassword());
                validAccount.setEmail(userAccount.getEmail());

                boolean result = database.updateAccount(validAccount);
                if (result) {
                    System.out.println("Account modified successfully!");
                    return true;
                }
            }
        }
        System.out.println("User Account not found");
        return false;
    }

    // Delete User Account
    public boolean deleteUserAccount(int accountId) throws SQLException {
        fetchUserAccounts();
        for (UserAccount validUserAccount : this.userAccounts) {
            if (validUserAccount.getAccountId() == accountId) {
                userAccounts.remove(validUserAccount);

                boolean result = database.deleteUserAccount(accountId);
                if (result) {
                    System.out.println("Account deleted successfully!\n");
                    return true;
                }
            }
        }
        System.out.println("User Account not found");
        return false;
    }

    // Edit User Account Password
    public boolean editUserAccountPassword(int userAccountId, String password) throws SQLException {
        fetchUserAccounts();
        for (UserAccount validAccount : this.userAccounts) {
            if (validAccount.getAccountId() == userAccountId) {
                validAccount.setPassword(password);
                boolean result = database.updateAccountPassword(userAccountId, password);
                if (result) {
                    System.out.println("Account password modified successfully!");
                    return true;
                }
            }
        }
        System.out.println("User Account not found");
        return false;
    }

    // Get User Account
    public UserAccount getUserAccount(int accountId) throws SQLException {
        UserAccount userAccount;
        userAccount = database.getUserAccount(accountId);
        if (userAccount != null) {
            return userAccount;
        }
        return null;
    }

    // Get User Accounts from the database
    public List<UserAccount> getUserAccounts() throws SQLException {
        return database.fetchUserAccounts();
    }

    // Helper: Verify User Account
    public boolean verifyUserAccount(int accountId) throws SQLException {
        return database.verifyUserAccount(accountId);
    }

    // Helper: Verify User account by username
    public boolean verifyAccountByUsername(String username) throws SQLException {
        return database.verifyAccountByUsername(username);
    }

    // Helper: Fetch all user accounts into cache
    public void fetchUserAccounts() throws SQLException {
        this.userAccounts = getUserAccounts();
    }
}