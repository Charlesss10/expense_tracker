package com.charles;

import java.sql.SQLException;

public class MonthlyReport implements ReportStrategy {

    @Override
    public boolean generateReport(int accountId, String targetMonth, String targetYear, ReportSummary reportSummary) {
        try {
            return reportSummary.generateReportSummary(accountId, targetMonth, targetYear);
        } catch (SQLException ex) {
            return false;
        }
    }
}