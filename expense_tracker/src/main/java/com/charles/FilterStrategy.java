package com.charles;
import java.util.List;

//Strategy Design Pattern: Interface that defines a common filter function which is implemented by other classes
public interface FilterStrategy {
    List<Transaction> filter(
        Double amountFilterStart, Double amountFilterEnd,
        java.util.Date dateFilterStart, java.util.Date dateFilterEnd,
        String categoryFilter, String sourceFilter,
        List<Transaction> transactions
    );
}