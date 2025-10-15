package com.charles;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/data")
public class DataStorageController {
    private final DataStorage dataStorage;
    private final AuthManager authManager;

    public DataStorageController(DataStorage dataStorage, AuthManager authManager) {
        this.dataStorage = dataStorage;
        this.authManager = authManager;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveData(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int accountId) {
        String token = authHeader.replace("Bearer ", "");
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        // Validate token
        if (!authManager.isTokenValid(token, accountId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
        }
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        try {
            // Generate CSV content
            String csvContent = dataStorage.exportTransactionsAsCSV(accountId);

            // Return as downloadable file
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=transactions.csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csvContent);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/load")
    public ResponseEntity<?> loadData(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int accountId,
            @RequestParam("file") MultipartFile file) {
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
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required.");
        }
        try {
            // Pass the file's InputStream to your dataStorage logic
            dataStorage.loadData(file.getInputStream(), accountId);
            return ResponseEntity.ok("Data loaded from uploaded file.");
        } catch (IOException | SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}