package com.charles;

import java.sql.SQLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseSummaryController {
    private final ExpenseSummary expenseSummary;

    public ExpenseSummaryController(ExpenseSummary expenseSummary) {
        this.expenseSummary = expenseSummary;
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getExpenseSummary(@RequestParam int accountId) {
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        try {
            var summary = expenseSummary.getExpensesSummaryData(accountId);
            if (summary == null || summary.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No expense summary found for this account.");
            }
            return ResponseEntity.ok(summary);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating expense summary: " + e.getMessage());
        }
    }
}