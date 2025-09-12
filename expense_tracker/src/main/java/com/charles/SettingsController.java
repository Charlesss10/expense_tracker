package com.charles;

import java.sql.SQLException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final Settings settings;

    public SettingsController(Settings settings) {
        this.settings = settings;
    }

    @PostMapping("/currency")
    public ResponseEntity<?> changeCurrency(@RequestBody CurrencyRequest req) {
        if (req.getAccountId() <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        if (req.getNewCurrency() == null || req.getNewCurrency().isBlank()) {
            return ResponseEntity.badRequest().body("Currency is required.");
        }
        String currency = req.getNewCurrency().trim().toLowerCase();
        if (!currency.equals("euro") && !currency.equals("dollar")) {
            return ResponseEntity.badRequest().body("Currency must be either Euro or Dollar.");
        }
        try {
            boolean result = settings.changeAccountCurrency(req.getAccountId(), req.getNewCurrency());
            if (result) {
                return ResponseEntity.ok("Currency updated.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Currency update failed");
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/currency")
    public ResponseEntity<?> getCurrency(@RequestParam int accountId) {
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        try {
            String currency = settings.getAccountCurrency(accountId);
            if (currency != null) {
                return ResponseEntity.ok(Map.of("currency", currency));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found.");
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}