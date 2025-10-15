package com.charles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

@Service
public class AuthManager {
    private final UserAccountManager userAccountManager;
    private List<UserAccount> userAccounts = new ArrayList<>();
    private int failedLoginAttempts = 0;
    private int accountId;
    private String email;
    private String token;
    private static final String SECRET_KEY = "your_super_secure_and_long_secret_key_123!";
    private static final SecretKey SIGNING_KEY = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
    private final Database database = Database.getInstance();

    public AuthManager(UserAccountManager userAccountManager) throws SQLException {
        this.userAccountManager = userAccountManager;
    }

    // Login to the system
    public String login(String username, String password, String tokenFile) throws SQLException, IOException {
        this.userAccounts = userAccountManager.getUserAccounts();

        for (UserAccount userAccount : this.userAccounts) {
            if (userAccount.getUsername().equals(username)) {
                if (BCrypt.checkpw(password, userAccount.getPassword())) {
                    // Generate and return session token
                    // Invalidate any existing session for this account
                    database.deleteSessions(userAccount.getAccountId());

                    this.setAccountInfo(userAccount.getAccountId(), userAccount.getEmail());
                    token = generateSessionToken(userAccount.getAccountId());
                    String sessionId = UUID.randomUUID().toString();
                    System.out.println("Login successful.");
                    boolean result = saveSession(token, sessionId, userAccount.getAccountId(), tokenFile);
                    if (result) {
                        return token;
                    }
                } else {
                    System.out.println("Password mismatch.");
                    return null;
                }
            }
        }
        System.out.println("Username does not exist. Try again.");
        return null;
    }

    // Log out of the System
    public boolean logout(String token, int accountId, String tokenFile) throws IOException, SQLException {
        return terminateSession(token, accountId, tokenFile);
    }

    // Reset User Password
    public boolean resetPassword(String email, String password) throws SQLException {
        this.userAccounts = userAccountManager.getUserAccounts();

        for (UserAccount userAccount : this.userAccounts) {
            if (userAccount.getEmail().equals(email)) {
                System.out.println("Email found.\n");
                boolean result = userAccountManager.editUserAccountPassword(userAccount.getAccountId(), password);
                boolean sessionsDeleted = database.deleteSessions(userAccount.getAccountId());

                if (result && sessionsDeleted) {
                    System.out.println("Reset Successful.\n");
                    return true;
                }
            } else {
                System.out.println("Email not found");
                return false;
            }
        }
        System.out.println("User Account not found");
        return false;
    }

    // Validate Email
    public boolean validateEmail(String email) throws SQLException {
        this.userAccounts = userAccountManager.getUserAccounts();

        for (UserAccount userAccount : this.userAccounts) {
            if (userAccount.getEmail().equals(email)) {
                System.out.println("Email found.\n");
                return true;
            } else {
                System.out.println("Email not found");
                return false;
            }
        }
        return false;
    }

    // Load Session Token
    public String loadSessionToken(String tokenFile) throws IOException {
        if (Files.exists(Paths.get(tokenFile))) {
            return new String(Files.readAllBytes(Paths.get(tokenFile)));
        }
        return null;
    }

    // Fetch all user accounts from the userAccountManager
    public void fetchUserAccounts() throws SQLException {
        this.userAccounts = userAccountManager.getUserAccounts();
    }

    // Get session Token
    public String getSessionAccountId(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(SIGNING_KEY) // Use the SecretKey for verification
                    .build() // Build the parser
                    .parseSignedClaims(String.valueOf(token))
                    .getPayload()
                    .getSubject();
        } catch (JwtException e) {
            System.out.println("Invalid or expired token.");
            return null;
        }
    }

    // Validate Session Token
    public boolean isTokenValid(String token, int expectedAccountId) {
        try {
            // Check if token exists in auth_token.txt
            File tokenFile = new File("auth_token.txt");
            if (!tokenFile.exists()) {
                return false;
            }
            String storedToken = Files.readString(tokenFile.toPath()).trim();
            if (!storedToken.equals(token)) {
                return false;
            }
            var claims = Jwts.parser()
                    .verifyWith(SIGNING_KEY)
                    .build()
                    .parseSignedClaims(storedToken)
                    .getPayload();

            String subject = claims.getSubject();
            return String.valueOf(expectedAccountId).equals(subject);
        } catch (JwtException | IOException | IllegalArgumentException e) {
            System.out.println("Invalid or expired token.");
            return false;
        }
    }

    // Helper: Set Account Info
    public void setAccountInfo(int accountId, String email) {
        this.accountId = accountId;
        this.email = email;
    }

    // Helper: Terminate Token
    public boolean terminateSession(String token, int accountId, String tokenFile) throws IOException, SQLException {
        boolean result = database.deleteSession(token, accountId);
        boolean fileDeleted = Files.deleteIfExists(Paths.get(tokenFile));
        // If session deleted in DB and token file deleted (or didn't exist), return
        // true
        return result == true && fileDeleted == true;
    }

    // Helper: Generate Session Token
    public String generateSessionToken(int accountId) {
        return Jwts.builder()
                .subject(String.valueOf(accountId))
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24-hour expiration
                .signWith(SIGNING_KEY)
                .compact();
    }

    // Helper: Save Session
    public boolean saveSession(String token, String sessionId, int accountId, String tokenFile)
            throws IOException, SQLException {
        Files.write(Paths.get(tokenFile), token.getBytes());
        boolean result = database.insertSession(token, sessionId, accountId);
        return result;
    }

    // Getters & Setters
    public int getFailedLoginAttempts() {
        return this.failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int FailedLoginAttempts) {
        this.failedLoginAttempts = FailedLoginAttempts;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAccountId() {
        return this.accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}