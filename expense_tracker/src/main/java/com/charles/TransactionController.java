package com.charles;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionManager transactionManager;
    private final Settings settings;

    public TransactionController(TransactionManager transactionManager, Settings settings) {
        this.transactionManager = transactionManager;
        this.settings = settings;
    }

    // Add Transaction
    @PostMapping
    public ResponseEntity<?> addTransaction(@RequestBody TransactionRequest request) {
        if (request.getTransaction() == null || request.getType() == null || request.getAccountId() <= 0) {
            return ResponseEntity.badRequest().body("Missing transaction, type, or invalid accountId.");
        }
        try {
            boolean result = transactionManager.addTransaction(request.getTransaction(), request.getType(),
                    request.getAccountId());
            if (result) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Transaction Added Successfully");
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body("Transaction Failed");
            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding transaction", e);
        }
    }

    // Edit Transaction
    @PutMapping
    public ResponseEntity<?> editTransaction(@RequestBody Transaction transaction, @RequestParam int accountId) {
        if (transaction == null || accountId <= 0) {
            return ResponseEntity.badRequest().body("Missing transaction or invalid accountId.");
        }
        try {
            boolean result = transactionManager.editTransaction(transaction, accountId);
            if (result) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Transaction Modified Successfully");
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body("Transaction not found or invalid type");
            }
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error editing transaction", e);
        }
    }

    // Delete Transaction
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<?> deleteTransaction(@PathVariable String transactionId, @RequestParam int accountId) {
        if (transactionId == null || transactionId.isBlank() || accountId <= 0) {
            return ResponseEntity.badRequest().body("Missing or invalid transactionId/accountId.");
        }
        try {
            boolean result = transactionManager.deleteTransaction(transactionId, accountId);
            if (result) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Transaction Deleted Successfully");
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body("Transaction not found or invalid type");
            }
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting transaction", e);
        }
    }

    // Get Total Balance
    @GetMapping("/balance")
    public ResponseEntity<?> getTotalBalance(@RequestParam int accountId) {
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        try {
            double totalBalance = this.transactionManager.getTotalBalance(accountId);
            double totalIncome = this.transactionManager.getTotalIncome();
            double totalExpenses = this.transactionManager.getTotalExpenses();
            String currency = settings.getAccountCurrency(accountId);

            return ResponseEntity.ok(new BalanceResponse(totalBalance, totalIncome, totalExpenses, currency));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error: " + e.getMessage());
        }
    }

    // Get Recent Transactions
    @GetMapping("/recent")
    public List<Transaction> getRecentTransactions(
            @RequestParam int accountId,
            @RequestParam(required = false) Double amountStart,
            @RequestParam(required = false) Double amountEnd,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.util.Date dateStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.util.Date dateEnd,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String source) {
        try {
            // Fetch all transactions first
            List<Transaction> allTransactions = transactionManager.getTransactions(accountId);

            // Choose strategy based on which filter is present (example logic)
            FilterStrategy strategy = null;
            if (dateStart != null && dateEnd != null) {
                strategy = new DateFilter();
            } else if (amountStart != null && amountEnd != null) {
                strategy = new AmountFilter();
            } else if (category != null) {
                strategy = new CategoryFilter();
            } else if (source != null) {
                strategy = new SourceFilter();
            }

            if (strategy != null) {
                // The strategy will filter and possibly print or return results
                // You may want to adapt your FilterStrategy interface to return a filtered list
                return strategy.filter(
                        amountStart, amountEnd,
                        dateStart, dateEnd,
                        category, source,
                        allTransactions);
            } else {
                // Default: filter transactions from the past 6 months
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MONTH, -6);
                java.util.Date sixMonthsAgo = cal.getTime();

                return allTransactions.stream()
                        .filter(t -> t.getDate().after(sixMonthsAgo))
                        .collect(java.util.stream.Collectors.toList());
            }
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error", e);
        }
    }

    // Get Transaction History
    @GetMapping("/history")
    public List<Transaction> getTransactionHistory(@RequestParam int accountId) {
        try {
            // Fetch all transactions for the account
            return getAllTransactions(accountId);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error", e);
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransaction(
            @PathVariable String transactionId,
            @RequestParam String type,
            @RequestParam int accountId) {
        if (transactionId == null || transactionId.isBlank() || type == null || type.isBlank() || accountId <= 0) {
            return ResponseEntity.badRequest().body("Missing or invalid transactionId/type/accountId.");
        }
        try {
            Transaction transaction = transactionManager.getTransaction(transactionId, type, accountId);
            if (transaction != null) {
                return ResponseEntity.ok(transaction);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found");
            }
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error", e);
        }
    }

    // Helper: Get All Transactions
    public List<Transaction> getAllTransactions(int accountId) throws SQLException {
        return transactionManager.getTransactions(accountId);
    }

    @GetMapping("/test")
    public String test() {
        return "TransactionController is working!";
    }
}