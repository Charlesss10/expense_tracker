package com.charles;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportSummary reportSummary;
    private final AuthManager authManager;
    private final Settings settings;

    public ReportController(ReportSummary reportSummary, AuthManager authManager, Settings settings) {
        this.reportSummary = reportSummary;
        this.authManager = authManager;
        this.settings = settings;
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReport(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int accountId,
            @RequestParam String targetMonth) {
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
        if (targetMonth == null || targetMonth.isBlank() || !targetMonth.matches("\\d{4}-\\d{2}")) {
            return ResponseEntity.badRequest().body("Invalid targetMonth format. Use YYYY-MM.");
        }
        try {
            ReportStrategy reportStrategy = new MonthlyReport();
            reportStrategy.generateReport(accountId, targetMonth, null, this.reportSummary);
            var data = reportSummary.getReportData();
            this.reportSummary.clearFilter();
            if (data == null || data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No report data found.");
            }
            java.util.Map<String, Object> response = new java.util.HashMap<>(data);
            response.put("currency", settings.getAccountCurrency(accountId));
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating report: " + e.getMessage());
        }
    }

    @GetMapping("/yearly")
    public ResponseEntity<?> getYearlyReport(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int accountId,
            @RequestParam String targetYear) {
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
        if (targetYear == null || targetYear.isBlank() || !targetYear.matches("\\d{4}")) {
            return ResponseEntity.badRequest().body("Invalid targetYear format. Use YYYY.");
        }
        try {
            ReportStrategy reportStrategy = new YearlyReport();
            reportStrategy.generateReport(accountId, null, targetYear, this.reportSummary);
            var data = reportSummary.getReportData();
            this.reportSummary.clearFilter();
            if (data == null || data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No report data found.");
            }
            java.util.Map<String, Object> response = new java.util.HashMap<>(data);
            response.put("currency", settings.getAccountCurrency(accountId));
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating report: " + e.getMessage());
        }

    }

    @GetMapping("/general")
    public ResponseEntity<?> getGeneralReport(@RequestHeader("Authorization") String authHeader,
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
            reportSummary.generateReportSummary(accountId, null, null);
            var data = reportSummary.getReportData();
            this.reportSummary.clearFilter();
            if (data == null || data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No report data found.");
            }
            java.util.Map<String, Object> response = new java.util.HashMap<>(data);
            response.put("currency", settings.getAccountCurrency(accountId));
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating report: " + e.getMessage());
        }
    }

    @PostMapping("/export-csv")
    public ResponseEntity<?> exportReportToCSV(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int accountId,
            @RequestParam(required = false) String targetMonth,
            @RequestParam(required = false) String targetYear) {
        String token = authHeader.replace("Bearer ", "");
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        if (!authManager.isTokenValid(token, accountId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
        }

        try {
            // Choose report strategy
            ReportStrategy reportStrategy;
            if (targetMonth != null && !targetMonth.isBlank()) {
                reportStrategy = new MonthlyReport();
            } else if (targetYear != null && !targetYear.isBlank()) {
                reportStrategy = new YearlyReport();
            } else {
                // General summary
                reportSummary.clearFilter();
                reportSummary.generateReportSummary(accountId, null, null);
                reportStrategy = null;
            }

            boolean generated;
            if (reportStrategy != null) {
                reportSummary.clearFilter();
                generated = reportStrategy.generateReport(accountId, targetMonth, targetYear, reportSummary);
            } else {
                generated = true; // Already generated above
            }

            if (!generated) {
                return ResponseEntity.badRequest().body("No transactions found for the given filters.");
            }

            // Export to CSV
            String filePath = "report_summary_" + accountId + ".csv";
            reportSummary.exportToCSV(filePath);
            reportSummary.clearFilter();

            // Return the file as a download
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            byte[] csvBytes = java.nio.file.Files.readAllBytes(path);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=report_summary.csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                    .body(csvBytes);

        } catch (IOException | SQLException e) {
            return ResponseEntity.status(500).body("Error exporting report: " + e.getMessage());
        }
    }
}