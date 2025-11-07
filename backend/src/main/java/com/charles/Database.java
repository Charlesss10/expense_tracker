package com.charles;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

//Singleton database
public class Database {
	private static Database instance;
	private final Connection con;

	// Create database connection
	private Database() throws ClassNotFoundException, SQLException {
		// MySQL
		/*
		 * Class.forName("com.mysql.cj.jdbc.Driver");
		 * String sqlUsername = System.getenv("MYSQL_USER");
		 * String sqlPassword = System.getenv("MYSQL_PASSWORD");
		 * String sqlHost = System.getenv("MYSQL_HOST");
		 * String sqlPort = System.getenv("MYSQL_PORT");
		 * String sqlDb = System.getenv("MYSQL_DB");
		 * String url = String.format(
		 * "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
		 * sqlHost, sqlPort, sqlDb
		 * );
		 */

		// PostGres
		Class.forName("org.postgresql.Driver");
		String sqlUsername = System.getenv("PG_USER");
		String sqlPassword = System.getenv("PG_PASSWORD");
		String sqlHost = System.getenv("PG_HOST");
		String sqlPort = System.getenv("PG_PORT");
		String sqlDb = System.getenv("PG_DB");

		// Render Managed Postgres typically requires SSL:
		String url = String.format(
				"jdbc:postgresql://%s:%s/%s?sslmode=require", // Uncomment for Render deployment
				 //"jdbc:postgresql://%s:%s/%s?sslmode=disable",
				sqlHost, sqlPort, sqlDb);

		this.con = DriverManager.getConnection(url, sqlUsername, sqlPassword);
		System.out.println("Database connection created successfully to: " + sqlHost + ":" + sqlPort + "/" + sqlDb);
	}

	// Static access point
	@SuppressWarnings("CallToPrintStackTrace")
	public static Database getInstance() {
		if (instance == null) {
			try {
				instance = new Database();
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	// Close database connection
	public void closeConnection() throws SQLException {
		con.close();
		instance = null;
		System.out.println("Database Connection closed.\n");
	}

	// Verify transaction from database
	public boolean verifyTransaction(String transactionId, String type) throws SQLException {
		// Validate transaction type
		if (!type.equalsIgnoreCase("INCOME") && !type.equalsIgnoreCase("EXPENSES")) {
			System.out.println("Invalid type: " + type);
			return false;
		}
		type = type.toUpperCase();

		String sql;
		switch (type) {
			case "INCOME" -> sql = "SELECT * FROM income WHERE transaction_id = ?";
			case "EXPENSES" -> sql = "SELECT * FROM expenses WHERE transaction_id = ?";
			default -> {
				System.out.println("Invalid Type: " + type);
				return false;
			}
		}

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, transactionId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					System.out.println("Transaction Verification Succeeded");
					return true;
				}
			}
		}
		return false;
	}

	// Fetch all transactions
	public List<Transaction> fetchTransactions(int accountId) throws SQLException {
		List<Transaction> transactions = new ArrayList<>();

		String incomeSql = "SELECT * FROM income WHERE account_id = ?";
		String expensesSql = "SELECT * FROM expenses WHERE account_id = ?";

		try (
				PreparedStatement incomeStmt = con.prepareStatement(incomeSql);
				PreparedStatement expensesStmt = con.prepareStatement(expensesSql)) {
			incomeStmt.setInt(1, accountId);
			try (ResultSet incomeSelectQuery = incomeStmt.executeQuery()) {
				while (incomeSelectQuery.next()) {
					Transaction incomeTransaction = new Transaction();
					incomeTransaction.setAccountId(incomeSelectQuery.getInt(1));
					incomeTransaction.setTransactionId(incomeSelectQuery.getString(2));
					incomeTransaction.setType(incomeSelectQuery.getString(3));
					incomeTransaction.setAmount(incomeSelectQuery.getDouble(4));
					incomeTransaction.setSource(incomeSelectQuery.getString(5));
					incomeTransaction.setDescription(incomeSelectQuery.getString(6));
					incomeTransaction.setDate(incomeSelectQuery.getDate(7));
					incomeTransaction.setSystem_date(incomeSelectQuery.getTimestamp(8));
					transactions.add(incomeTransaction);
				}
			}

			expensesStmt.setInt(1, accountId);
			try (ResultSet expensesSelectQuery = expensesStmt.executeQuery()) {
				while (expensesSelectQuery.next()) {
					Transaction expensesTransaction = new Transaction();
					expensesTransaction.setAccountId(expensesSelectQuery.getInt(1));
					expensesTransaction.setTransactionId(expensesSelectQuery.getString(2));
					expensesTransaction.setType(expensesSelectQuery.getString(3));
					expensesTransaction.setAmount(expensesSelectQuery.getDouble(4));
					expensesTransaction.setCategory(expensesSelectQuery.getString(5));
					expensesTransaction.setDescription(expensesSelectQuery.getString(6));
					expensesTransaction.setDate(expensesSelectQuery.getDate(7));
					expensesTransaction.setSystem_date(expensesSelectQuery.getTimestamp(8));
					transactions.add(expensesTransaction);
				}
			}
		}
		return transactions;
	}

	// Insert transaction into database
	public boolean insertTransaction(Transaction transaction, int accountId) throws SQLException {
		String transactionId = transaction.getTransactionId();
		String type = transaction.getType();
		double amount = transaction.getAmount();
		String source = transaction.getSource();
		String category = transaction.getCategory();
		String description = transaction.getDescription();
		Date date = transaction.getDate();

		if (!verifyTransaction(transactionId, type)) {
			switch (type) {
				case "INCOME" -> {
					source = source != null ? source.toUpperCase() : null;
					description = description != null ? description.toUpperCase() : null;
					String sql = "INSERT INTO income(account_id, transaction_id, type, amount, source, description, date) VALUES (?, ?, ?, ?, ?, ?, ?)";
					try (PreparedStatement pstmt = con.prepareStatement(sql)) {
						pstmt.setInt(1, accountId);
						pstmt.setString(2, transactionId);
						pstmt.setString(3, type);
						pstmt.setDouble(4, amount);
						pstmt.setString(5, source);
						pstmt.setString(6, description);
						pstmt.setDate(7, date);
						pstmt.executeUpdate();
					}
					return true;
				}
				case "EXPENSES" -> {
					category = category != null ? category.toUpperCase() : null;
					description = description != null ? description.toUpperCase() : null;
					String sql = "INSERT INTO expenses(account_id, transaction_id, type, amount, category, description, date) VALUES (?, ?, ?, ?, ?, ?, ?)";
					try (PreparedStatement pstmt = con.prepareStatement(sql)) {
						pstmt.setInt(1, accountId);
						pstmt.setString(2, transactionId);
						pstmt.setString(3, type);
						pstmt.setDouble(4, amount);
						pstmt.setString(5, category);
						pstmt.setString(6, description);
						pstmt.setDate(7, date);
						pstmt.executeUpdate();
					}
					return true;
				}
				default -> {
					System.out.println("Invalid Type: " + transaction.getType());
					return false;
				}
			}
		}
		return false;
	}

	// Update transaction in database
	public boolean updateTransaction(Transaction transaction) throws SQLException {
		String transactionId = transaction.getTransactionId();
		String type = transaction.getType();

		// Fetch current transaction from DB
		Transaction current = null;
		String selectSql;
		if (verifyTransaction(transactionId, type)) {
			switch (type) {
				case "INCOME" ->
					selectSql = "SELECT amount, source, description, date FROM income WHERE transaction_id = ?";
				case "EXPENSES" ->
					selectSql = "SELECT amount, category, description, date FROM expenses WHERE transaction_id = ?";
				default -> {
					System.out.println("Invalid Type: " + transaction.getType());
					return false;
				}
			}

			try (PreparedStatement selectStmt = con.prepareStatement(selectSql)) {
				selectStmt.setString(1, transactionId);
				try (ResultSet rs = selectStmt.executeQuery()) {
					if (rs.next()) {
						current = new Transaction();
						current.setAmount(rs.getDouble("amount"));
						if (type.equals("INCOME")) {
							current.setSource(rs.getString("source"));
						} else {
							current.setCategory(rs.getString("category"));
						}
						current.setDescription(rs.getString("description"));
						current.setDate(rs.getDate("date"));
					}
				}
			}

			if (current != null) {
				double amount = transaction.getAmount() != 0.0 ? transaction.getAmount() : current.getAmount();
				String description = transaction.getDescription() != null && !transaction.getDescription().isBlank()
						? transaction.getDescription().toUpperCase()
						: current.getDescription();
				Date date = transaction.getDate() != null ? transaction.getDate() : current.getDate();

				if (type.equals("INCOME")) {
					String source = transaction.getSource() != null && !transaction.getSource().isBlank()
							? transaction.getSource().toUpperCase()
							: current.getSource();

					String updateSql = "UPDATE income SET amount = ?, source = ?, description = ?, date = ? WHERE transaction_id = ?";
					try (PreparedStatement updateStmt = con.prepareStatement(updateSql)) {
						updateStmt.setDouble(1, amount);
						updateStmt.setString(2, source);
						updateStmt.setString(3, description);
						updateStmt.setDate(4, date);
						updateStmt.setString(5, transactionId);
						updateStmt.executeUpdate();
					}
					return true;
				} else if (type.equals("EXPENSES")) {
					String category = transaction.getCategory() != null && !transaction.getCategory().isBlank()
							? transaction.getCategory().toUpperCase()
							: current.getCategory();

					String updateSql = "UPDATE expenses SET amount = ?, category = ?, description = ?, date = ? WHERE transaction_id = ?";
					try (PreparedStatement updateStmt = con.prepareStatement(updateSql)) {
						updateStmt.setDouble(1, amount);
						updateStmt.setString(2, category);
						updateStmt.setString(3, description);
						updateStmt.setDate(4, date);
						updateStmt.setString(5, transactionId);
						updateStmt.executeUpdate();
					}
					return true;
				}
			} else {
				System.out.println("No current transaction found for transaction_id: " + transactionId);
				return false;
			}
		}
		System.out.println("Transaction does not exist");
		return false;
	}

	// Delete transaction in database
	public boolean deleteTransaction(Transaction transaction) throws SQLException {
		String transactionId = transaction.getTransactionId();
		String type = transaction.getType();

		if (verifyTransaction(transactionId, type)) {
			String sql;
			switch (type) {
				case "INCOME" -> sql = "DELETE FROM income WHERE transaction_id = ?";
				case "EXPENSES" -> sql = "DELETE FROM expenses WHERE transaction_id = ?";
				default -> {
					System.out.println("Invalid Type: " + transaction.getType());
					return false;
				}
			}
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				pstmt.setString(1, transactionId);
				pstmt.executeUpdate();
			}
			return true;
		}
		System.out.print("Transaction bnot found");
		return false;
	}

	// Verify user account in database
	public boolean verifyUserAccount(int accountId) throws SQLException {
		String sql = "SELECT * FROM user_account WHERE account_id = ?";
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, accountId);
			try (ResultSet accountSelectQuery = pstmt.executeQuery()) {
				if (accountSelectQuery.next()) {
					return true;
				}
				System.out.println("User account Id not found.\n");
			}
		}
		return false;
	}

	// Verify User account by username in database
	public boolean verifyAccountByUsername(String username) throws SQLException {
		String sql = "SELECT * FROM user_account WHERE username = ?";
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, username);
			try (ResultSet accountSelectQuery = pstmt.executeQuery()) {
				if (accountSelectQuery.next()) {
					System.out.println("User account username found.\n");
					return true;
				}
				System.out.println("User account username not found.\n");
			}
		}
		return false;
	}

	// Get user account in database
	public UserAccount getUserAccount(int accountId) throws SQLException {
		UserAccount userAccount = null;
		String sql = "SELECT * FROM user_account WHERE account_id = ?";
		if (verifyUserAccount(accountId)) {
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				pstmt.setInt(1, accountId);
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						String firstName = rs.getString(2);
						String lastName = rs.getString(3);
						String username = rs.getString(4);
						Date birthday = rs.getDate(5);
						String currency = rs.getString(6);
						String password = rs.getString(7);
						String email = rs.getString(8);
						// Currency symbol conversion
						if (currency != null) {
							if (currency.equalsIgnoreCase("EURO")) {
								currency = "€";
							} else if (currency.equalsIgnoreCase("DOLLAR")) {
								currency = "$";
							}
						}
						userAccount = new UserAccount(firstName, lastName, username, birthday, password, email,
								currency);
						userAccount.setAccountId(accountId);
					}
				}
			}
		}
		return userAccount;
	}

	// Fetch all user accounts
	public List<UserAccount> fetchUserAccounts() throws SQLException {
		List<UserAccount> userAccounts = new ArrayList<>();
		String sql = "SELECT * FROM user_account";
		try (PreparedStatement pstmt = con.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				int accountId = rs.getInt(1);
				String firstName = rs.getString(2);
				String lastName = rs.getString(3);
				String username = rs.getString(4);
				Date birthday = rs.getDate(5);
				String currency = rs.getString(6);
				String password = rs.getString(7);
				String email = rs.getString(8);
				// Currency symbol conversion
				if (currency != null) {
					if (currency.equalsIgnoreCase("EURO")) {
						currency = "€";
					} else if (currency.equalsIgnoreCase("DOLLAR")) {
						currency = "$";
					}
				}
				UserAccount userAccount = new UserAccount(firstName, lastName, username, birthday, password, email,
						currency);
				userAccount.setAccountId(accountId);
				userAccounts.add(userAccount);
			}
		}
		return userAccounts;
	}

	// Insert user account into database
	public boolean insertAccount(UserAccount userAccount) throws SQLException {
		String firstName = userAccount.getFirstName();
		String lastName = userAccount.getLastName();
		String username = userAccount.getUsername();
		Date birthday = userAccount.getBirthday();
		String password = userAccount.getPassword();
		String email = userAccount.getEmail();
		String currency = userAccount.getCurrency();

		// Hash Password
		String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

		// Capitalize Currency
		String capCurrency = currency.toUpperCase();

		String sql = "INSERT INTO user_account(first_name, last_name, username, birthday, currency, password, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, firstName);
			pstmt.setString(2, lastName);
			pstmt.setString(3, username);
			pstmt.setDate(4, birthday);
			pstmt.setString(5, capCurrency);
			pstmt.setString(6, hashedPassword);
			pstmt.setString(7, email);
			pstmt.executeUpdate();
		}
		System.out.println("Account added Successfully!\n");
		return true;
	}

	// Update user account in database
	public boolean updateAccount(UserAccount userAccount) throws SQLException {
		int accountId = userAccount.getAccountId();

		// Fetch current user account from DB
		UserAccount current = getUserAccount(accountId);

		if (userAccount.getCurrency() != null) {
			System.out.println("Currency cannot be updated. Update in Settings");
		}

		if (verifyUserAccount(accountId) && current != null) {
			String firstName = userAccount.getFirstName() != null && !userAccount.getFirstName().isBlank()
					? userAccount.getFirstName()
					: current.getFirstName();
			String lastName = userAccount.getLastName() != null && !userAccount.getLastName().isBlank()
					? userAccount.getLastName()
					: current.getLastName();
			String username = userAccount.getUsername() != null && !userAccount.getUsername().isBlank()
					? userAccount.getUsername()
					: current.getUsername();
			Date birthday = userAccount.getBirthday() != null
					? userAccount.getBirthday()
					: current.getBirthday();
			String password = userAccount.getPassword() != null && !userAccount.getPassword().isBlank()
					? BCrypt.hashpw(userAccount.getPassword(), BCrypt.gensalt(12))
					: current.getPassword();
			String email = userAccount.getEmail() != null && !userAccount.getEmail().isBlank()
					? userAccount.getEmail()
					: current.getEmail();

			String sql = "UPDATE user_account SET first_name = ?, last_name = ?, username = ?, birthday = ?, password = ?, email = ? WHERE account_id = ?";
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				pstmt.setString(1, firstName);
				pstmt.setString(2, lastName);
				pstmt.setString(3, username);
				pstmt.setDate(4, birthday);
				pstmt.setString(5, password);
				pstmt.setString(6, email);
				pstmt.setInt(7, accountId);
				pstmt.executeUpdate();
			}
			System.out.println("Account Updated Successfully");
			return true;
		}
		System.out.println("Account does not exist");
		return false;
	}

	// Delete user account in database
	public boolean deleteUserAccount(int accountId) throws SQLException {
		if (verifyUserAccount(accountId)) {
			boolean result = deleteSessions(accountId); // Remove all sessions first
			if (result) {
				String sql = "DELETE FROM user_account WHERE account_id = ?";
				try (PreparedStatement pstmt = con.prepareStatement(sql)) {
					pstmt.setInt(1, accountId);
					pstmt.executeUpdate();
				}
				System.out.println("Account deleted from database");
				return true;
			}
		}
		System.out.println("Account does not exist");
		return false;
	}

	public boolean updateAccountCurrency(int accountId, String newCurrency) throws SQLException {
		// Capitalize Currency
		String capCurrency = newCurrency.toUpperCase();

		String sql = "UPDATE user_account SET currency = ? WHERE account_id = ?";
		if (verifyUserAccount(accountId)) {
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				pstmt.setString(1, capCurrency);
				pstmt.setInt(2, accountId);
				pstmt.executeUpdate();
			}
			System.out.println("Currency updated successfully for account_id: " + accountId);
			return true;
		}
		System.out.println("User Account does not exist");
		return false;
	}

	public boolean updateAccountPassword(int accountId, String password) throws SQLException {
		// Hash Password
		String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

		String sql = "UPDATE user_account SET password = ? WHERE account_id = ?";
		if (verifyUserAccount(accountId)) {
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				pstmt.setString(1, hashedPassword);
				pstmt.setInt(2, accountId);
				pstmt.executeUpdate();
			}
			System.out.println("Account password successfully changed");
			return true;
		}
		System.out.println("User Account does not exist");
		return false;
	}

	public boolean insertSession(String token, String sessionId, int accountId) throws SQLException {
		if (verifyUserAccount(accountId)) {
			String sql = "INSERT INTO sessions(session_id, account_id, token) VALUES (?, ?, ?)";
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				pstmt.setString(1, sessionId);
				pstmt.setInt(2, accountId);
				pstmt.setString(3, token);
				pstmt.executeUpdate();
			}
			System.out.println("Session Started");
			return true;
		}
		System.out.println("User Account does not exist");
		return false;
	}

	// Helper: Delete all User sessions
	public boolean deleteSessions(int accountId) throws SQLException {
		String sql = "DELETE FROM sessions WHERE account_id = ?";
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, accountId);
			pstmt.executeUpdate();
		}
		return true;
	}

	// Helper: Delete user session
	public boolean deleteSession(String token, int accountId) throws SQLException {
		String sql = "DELETE FROM sessions WHERE token = ? AND account_id = ?";
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, token);
			pstmt.setInt(2, accountId);
			pstmt.executeUpdate();
		}
		return true;
	}
}