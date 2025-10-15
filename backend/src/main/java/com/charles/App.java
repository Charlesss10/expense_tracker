package com.charles;

import java.io.IOException;
import java.sql.SQLException;

public class App {
	private static UserInterface userInterface;

	// Root entry into the application
	public static void main(String args[]) throws SQLException, ClassNotFoundException, IOException {
		UserAccountManager userAccountManager = new UserAccountManager();
		AuthManager authManager = new AuthManager(userAccountManager);
		Settings settings = new Settings(userAccountManager);
		TransactionManager transactionManager = new TransactionManager();
		ExpenseSummary expenseSummary = new ExpenseSummary(settings, transactionManager);
		ReportSummary reportSummary = new ReportSummary(settings, transactionManager);
		DataStorage dataStorage = new DataStorage(transactionManager);
		Database database = Database.getInstance();

		transactionManager.registerObserver(expenseSummary);
		transactionManager.registerObserver(reportSummary);

		userInterface = new Management(userAccountManager, authManager, transactionManager, expenseSummary,
				reportSummary, settings, dataStorage, database);
		userInterface.run();
	}
}