package com.charles;

public interface ReportStrategy {
    public boolean generateReport(int accountId, String targetMonth, String targetYear, ReportSummary reportSummary);
}