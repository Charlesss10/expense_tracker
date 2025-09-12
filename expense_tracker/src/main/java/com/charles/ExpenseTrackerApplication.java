package com.charles;

import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class ExpenseTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }

    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        try {
            Database.getInstance().closeConnection();
        } catch (SQLException e) {
        }
    }
}