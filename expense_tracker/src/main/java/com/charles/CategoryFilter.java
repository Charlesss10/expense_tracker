package com.charles;

import java.util.List;
import java.util.stream.Collectors;

//Filter by category
public class CategoryFilter implements FilterStrategy {
    @Override
    public List<Transaction> filter(
            Double amountFilterStart, Double amountFilterEnd,
            java.util.Date dateFilterStart, java.util.Date dateFilterEnd,
            String categoryFilter, String sourceFilter,
            List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> "EXPENSES".equalsIgnoreCase(t.getType())
                        && t.getCategory().equalsIgnoreCase(categoryFilter))
                .collect(Collectors.toList());
    }
}
