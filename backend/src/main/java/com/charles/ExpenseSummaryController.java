package com.charles;

import java.sql.SQLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseSummaryController {
    private final ExpenseSummary expenseSummary;
    private final AuthManager authManager;
    private final Settings settings;

    public ExpenseSummaryController(ExpenseSummary expenseSummary, AuthManager authManager, Settings settings) {
        this.expenseSummary = expenseSummary;
        this.authManager = authManager;
        this.settings = settings;
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getExpenseSummary(@RequestHeader("Authorization") String authHeader,
            @RequestParam int accountId) {
        String token = authHeader.replace("Bearer ", "");
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        if (!authManager.isTokenValid(token, accountId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
        }
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        try {
            var summary = expenseSummary.getExpensesSummaryData(accountId);
            if (summary == null || summary.isEmpty()) {
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("message", "No expense summary found for this account.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            summary.put("currency", settings.getAccountCurrency(accountId));
            return ResponseEntity.ok(summary);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating expense summary: " + e.getMessage());
        }
    }
}