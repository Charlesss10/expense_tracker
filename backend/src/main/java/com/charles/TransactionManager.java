package com.charles;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service("appTransactionManager")
public class TransactionManager implements Subject {
	private List<Transaction> transactions = new ArrayList<>();
	private final Database database = Database.getInstance();
	private final List<Observer> observers = new ArrayList<>(); // List of observers
	private double totalExpenses;
	private double totalIncome;

	public TransactionManager() throws SQLException {
	}

	// Add Transaction
	public boolean addTransaction(Transaction transaction, String type, int accountId)
			throws ClassNotFoundException, SQLException, IOException {
		// Capitalize Type
		type = type.toUpperCase();

		// Set type
		transaction.setType(type);

		// Set transactionId
		String transactionId = UUID.randomUUID().toString();
		transaction.setTransactionId(transactionId);

		// Add Transaction into the database
		if ("INCOME".equals(transaction.getType()) || "EXPENSES".equals(transaction.getType())) {
			boolean result = database.insertTransaction(transaction, accountId);
			if (result) {
				System.out.println("\nTransaction Added Successfully\n");
				fetchTransactions(accountId);
				notifyObservers();
				return true;
			} else {
				return false;
			}
		} else {
			System.out.println("Invalid Type: " + transaction.getType());
			return false;
		}
	}

	// Edit Transaction
	public boolean editTransaction(Transaction transaction, int accountId) throws SQLException {
		// Capitalize Type
		transaction.setType(transaction.getType().toUpperCase());

		// Retrieve all transactions from Database
		this.transactions = getTransactions(accountId);

		for (Transaction validTransaction : this.transactions) {
			if (validTransaction.getTransactionId().equals(transaction.getTransactionId())) {
				switch (validTransaction.getType()) {
					case "INCOME" -> {
						validTransaction.setAmount(transaction.getAmount());
						validTransaction.setSource(transaction.getSource());
						validTransaction.setDescription(transaction.getDescription());
						validTransaction.setDate(transaction.getDate());
						boolean result = database.updateTransaction(transaction);
						if (result) {
							System.out.println("Transaction modified successfully!\n");
							fetchTransactions(accountId);
							notifyObservers();
							return true;
						} else {
							return false;
						}
					}
					case "EXPENSES" -> {
						validTransaction.setAmount(transaction.getAmount());
						validTransaction.setCategory(transaction.getCategory());
						validTransaction.setDescription(transaction.getDescription());
						validTransaction.setDate(transaction.getDate());
						boolean result = database.updateTransaction(transaction);
						if (result) {
							System.out.println("Transaction modified successfully!\n");
							fetchTransactions(accountId);
							notifyObservers();
							return true;
						} else {
							return false;
						}
					}
					default -> {
						System.out.println("Invalid Type: " + transaction.getType());
						return false;
					}
				}
			}
		}
		System.out.println("Transaction not found");
		return false;
	}

	// Delete Transaction
	public boolean deleteTransaction(String transactionId, int accountId) throws SQLException {
		// Retrieve all transactions from Database
		this.transactions = getTransactions(accountId);

		for (Transaction validTransaction : this.transactions) {
			if (validTransaction.getTransactionId().equals(transactionId)
					&& validTransaction.getAccountId() == accountId) {
				if ("INCOME".equals(validTransaction.getType()) || "EXPENSES".equals(validTransaction.getType())) {
					boolean cacheResult = transactions.remove(validTransaction);
					boolean dbResult = database.deleteTransaction(validTransaction);
					if (cacheResult == true && dbResult == true) {
						System.out.println("Transaction deleted successfully!\n");
						fetchTransactions(accountId);
						notifyObservers();
						return true;
					}
				} else {
					System.out.println("Invalid Type: " + validTransaction.getType());
					return false;
				}
			}

		}
		System.out.println("Transaction not found\n");
		return false;
	}

	// Get Total Balance
	public double getTotalBalance(int accountId) throws SQLException {
		this.totalIncome = 0.0;
		this.totalExpenses = 0.0;
		double totalBalance;

		// Retrieve all transactions from Database
		this.transactions = getTransactions(accountId);

		for (Transaction transaction : this.transactions) {
			switch (transaction.getType()) {
				case "INCOME" -> {
					this.totalIncome = this.totalIncome + transaction.getAmount();
					break;
				}
				case "EXPENSES" -> {
					this.totalExpenses = this.totalExpenses + transaction.getAmount();
					break;
				}
				default -> System.out.println("Invalid Type.");
			}
		}
		totalBalance = this.totalIncome - this.totalExpenses;
		this.totalIncome = Math.round(this.totalIncome * 100.0) / 100.0;
		this.totalExpenses = Math.round(this.totalExpenses * 100.0) / 100.0;
		return Math.round(totalBalance * 100.0) / 100.0;
	}

	// Get recent transactions
	public List<Transaction> getRecentTransactions(int accountId) throws SQLException {
		// Fetch all transactions first
		List<Transaction> allTransactions = getTransactions(accountId);

		// Default: filter transactions from the past 6 months
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.add(java.util.Calendar.MONTH, -6);
		java.util.Date sixMonthsAgo = cal.getTime();

		return allTransactions.stream()
				.filter(t -> t.getDate().after(sixMonthsAgo))
				.collect(java.util.stream.Collectors.toList());
	}

	// Get transaction
	public Transaction getTransaction(String transactionId, String type, int accountId) throws SQLException {

		// Capitalize Type
		type = type.toUpperCase();

		// Retrieve all transactions from Database
		fetchTransactions(accountId);

		Transaction transaction = null;
		for (Transaction validTransaction : this.transactions) {
			if (validTransaction.getTransactionId().equals(transactionId)) {
				transaction = validTransaction;
			}
		}
		if (transaction != null) {
			switch (type) {
				case "INCOME" -> {
					return transaction;
				}
				case "EXPENSES" -> {
					return transaction;
				}
				default -> {
					System.out.println("Invalid Type: " + type);
					return null;
				}
			}
		}
		return null;
	}

	// Helper: Verify transaction
	public boolean verifyTransaction(String transactionId, String type) throws SQLException {

		// Validate transaction type
		if (!type.equalsIgnoreCase("INCOME") && !type.equalsIgnoreCase("EXPENSES")) {
			System.out.println("Invalid type: " + type);
			return false;
		}

		return database.verifyTransaction(transactionId, type);
	}

	// Helper: Fetch transactions into cache
	public void fetchTransactions(int accountId) throws SQLException {
		this.transactions = getTransactions(accountId);
	}

	// Helper: Fetch all transactions from Database
	public List<Transaction> getTransactions(int accountId) throws SQLException {
		return database.fetchTransactions(accountId);
	}

	// Getters
	public double getTotalExpenses() {
		return this.totalExpenses;
	}

	public double getTotalIncome() {
		return this.totalIncome;
	}

	@Override
	public void registerObserver(Observer o) {
		observers.add(o);
	}

	@Override
	public void removeObserver(Observer o) {
		observers.remove(o);
	}

	@Override
	public void notifyObservers() {
		for (Observer observer : this.observers) {
			observer.update();
		}
	}
}