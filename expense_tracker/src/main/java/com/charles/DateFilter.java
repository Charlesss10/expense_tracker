package com.charles;

import java.util.List;
import java.util.stream.Collectors;

//Filter transaction by date
public class DateFilter implements FilterStrategy {
    @Override
    public List<Transaction> filter(
            Double amountFilterStart, Double amountFilterEnd,
            java.util.Date dateFilterStart, java.util.Date dateFilterEnd,
            String categoryFilter, String sourceFilter,
            List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getDate().compareTo(dateFilterStart) >= 0 && t.getDate().compareTo(dateFilterEnd) <= 0)
                .collect(Collectors.toList());
    }
}