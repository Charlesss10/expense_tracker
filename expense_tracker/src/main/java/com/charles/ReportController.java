package com.charles;

import java.sql.SQLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportSummary reportSummary;

    public ReportController(ReportSummary reportSummary) {
        this.reportSummary = reportSummary;
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReport(@RequestParam int accountId, @RequestParam String targetMonth) {
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        if (targetMonth == null || targetMonth.isBlank() || !targetMonth.matches("\\d{4}-\\d{2}")) {
            return ResponseEntity.badRequest().body("Invalid targetMonth format. Use YYYY-MM.");
        }
        try {
            ReportStrategy reportStrategy = new MonthlyReport();
            this.reportSummary.clearFilter();
            reportStrategy.generateReport(accountId, targetMonth, null, this.reportSummary);
            var data = reportSummary.getReportData();
            if (data == null || data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No report data found.");
            }
            return ResponseEntity.ok(data);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating report: " + e.getMessage());
        }
    }

    @GetMapping("/yearly")
    public ResponseEntity<?> getYearlyReport(@RequestParam int accountId, @RequestParam String targetYear) {
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        if (targetYear == null || targetYear.isBlank() || !targetYear.matches("\\d{4}")) {
            return ResponseEntity.badRequest().body("Invalid targetYear format. Use YYYY.");
        }
        try {
            ReportStrategy reportStrategy = new YearlyReport();
            this.reportSummary.clearFilter();
            reportStrategy.generateReport(accountId, null, targetYear, this.reportSummary);
            var data = reportSummary.getReportData();
            if (data == null || data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No report data found.");
            }
            return ResponseEntity.ok(data);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating report: " + e.getMessage());
        }
    }

    @GetMapping("/general")
    public ResponseEntity<?> getGeneralReport(@RequestParam int accountId) {
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        try {
            reportSummary.generateReportSummary(accountId, null, null);
            this.reportSummary.clearFilter();
            var data = reportSummary.getReportData();
            if (data == null || data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No report data found.");
            }
            return ResponseEntity.ok(data);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating report: " + e.getMessage());
        }
    }
}