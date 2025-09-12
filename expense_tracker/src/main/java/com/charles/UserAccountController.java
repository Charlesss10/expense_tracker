package com.charles;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserAccountController {
    private final UserAccountManager userAccountManager;

    public UserAccountController(UserAccountManager userAccountManager) {
        this.userAccountManager = userAccountManager;
    }

    @PostMapping
    public ResponseEntity<?> addUserAccount(@RequestBody UserAccount userAccount) {
        if (userAccount.getEmail() == null || userAccount.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        if (userAccount.getUsername() == null || userAccount.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("Username is required.");
        }
        if (userAccount.getPassword() == null || userAccount.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required.");
        }
        try {
            boolean result = userAccountManager.addAccount(userAccount);
            if (result) {
                return ResponseEntity.status(HttpStatus.CREATED).body("User account created successfully!");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User account not created");
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating user account: " + e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> editUserAccount(@RequestParam int accountId, @RequestBody UserAccount userAccount) {
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        try {
            userAccount.setAccountId(accountId);
            boolean result = userAccountManager.editUserAccount(userAccount);
            if (result) {
                return ResponseEntity.ok("User account updated successfully!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User account not found or invalid data.");
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user account: " + e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserAccount(@RequestParam int accountId) {
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        try {
            boolean success = userAccountManager.deleteUserAccount(accountId);
            if (success) {
                return ResponseEntity.ok("User account deleted successfully!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User account not found.");
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user account: " + e.getMessage());
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getUserAccount(@PathVariable int accountId) throws SQLException {
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        UserAccount user = userAccountManager.getUserAccount(accountId);
        if (user != null) {
            return ResponseEntity.ok(new UserAccountDTO(user));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUserAccounts() {
        try {
            List<UserAccount> accounts = userAccountManager.getUserAccounts();
            List<UserAccountDTO> dtos = accounts.stream()
                    .map(UserAccountDTO::new)
                    .toList();
            return ResponseEntity.ok(dtos);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching user accounts: " + e.getMessage());
        }
    }
}