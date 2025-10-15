package com.charles;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthManager authManager;
    private final String TOKEN_FILE = "./auth_token.txt";

    public AuthController(AuthManager authManager) {
        this.authManager = authManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.getUsername() == null || loginRequest.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("Username is required.");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required.");
        }
        try {
            String token = authManager.login(loginRequest.getUsername(), loginRequest.getPassword(), TOKEN_FILE);
            if (token != null) {
                int accountId = authManager.getAccountId();
                return ResponseEntity.ok(new LoginResponse(token, accountId));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
            }
        } catch (IOException | SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login error: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int accountId) {
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        String token = authHeader.replace("Bearer ", "");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("Token is required.");
        }
        // Validate token before logout
        if (!authManager.isTokenValid(token, accountId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
        }
        try {
            boolean result = authManager.logout(token, accountId, TOKEN_FILE);
            if (result) {
                return ResponseEntity.ok("Logged out successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Logout failed.");
            }
        } catch (IOException | SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logout error: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body("New password is required.");
        }
        try {
            boolean result = authManager.resetPassword(request.getEmail(), request.getNewPassword());
            if (result) {
                return ResponseEntity.ok("Password reset successful.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found.");
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Reset error: " + e.getMessage());
        }
    }
}