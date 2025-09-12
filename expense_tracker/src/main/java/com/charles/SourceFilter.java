package com.charles;

import java.util.List;
import java.util.stream.Collectors;

//Filter by source
public class SourceFilter implements FilterStrategy {

    @Override
    public List<Transaction> filter(
            Double amountFilterStart, Double amountFilterEnd,
            java.util.Date dateFilterStart, java.util.Date dateFilterEnd,
            String categoryFilter, String sourceFilter,
            List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getType()) && t.getSource().equalsIgnoreCase(sourceFilter))
                .collect(Collectors.toList());
    }
}
