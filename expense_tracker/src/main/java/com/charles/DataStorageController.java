package com.charles;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data")
public class DataStorageController {
    private final DataStorage dataStorage;
    private final String storageFileLocation = "./transactions.csv";

    public DataStorageController(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveData(
            @RequestParam int accountId,
            @RequestParam(required = false, defaultValue = storageFileLocation) String filePath) {
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        if (filePath == null || filePath.isBlank()) {
            return ResponseEntity.badRequest().body("File path is required.");
        }
        try {
            dataStorage.saveData(filePath, accountId);
            return ResponseEntity.ok("Data saved to " + filePath);
        } catch (IOException | SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/load")
    public ResponseEntity<?> loadData(
            @RequestParam int accountId,
            @RequestParam(required = false, defaultValue = storageFileLocation) String filePath) {
        if (accountId <= 0) {
            return ResponseEntity.badRequest().body("Invalid account ID.");
        }
        if (filePath == null || filePath.isBlank()) {
            return ResponseEntity.badRequest().body("File path is required.");
        }
        try {
            dataStorage.loadData(filePath, accountId);
            return ResponseEntity.ok("Data loaded from " + filePath);
        } catch (IOException | SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}