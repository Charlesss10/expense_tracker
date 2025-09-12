package com.charles;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.Year;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.mindrot.jbcrypt.BCrypt;

// Handles core functionalities of the software
public class Management implements UserInterface {
	private final UserAccountManager userAccountManager;
	private final AuthManager authManager;
	private final TransactionManager transactionManager;
	private final ExpenseSummary expenseSummary;
	private final ReportSummary reportSummary;
	private final Settings settings;
	private final DataStorage dataStorage;
	private final Database database;
	private final String TOKEN_FILE = "./expense_tracker/auth_token.txt";
	Scanner choice = new Scanner(System.in);

	public Management(UserAccountManager userAccountManager, AuthManager authManager,
			TransactionManager transactionManager, ExpenseSummary expenseSummary,
			ReportSummary reportSummary, Settings settings, DataStorage dataStorage, Database database)
			throws SQLException {
		this.userAccountManager = userAccountManager;
		this.authManager = authManager;
		this.transactionManager = transactionManager;
		this.expenseSummary = expenseSummary;
		this.reportSummary = reportSummary;
		this.settings = settings;
		this.dataStorage = dataStorage;
		this.database = database;
	}

	@Override
	public boolean userAccountManager(String userAccountManagerPrompt) throws SQLException {
		UserAccount userAccount;
		String firstName;
		String lastName;
		String username;
		String currencyChoice;
		Date birthday = null;
		String password;
		String email = null;
		Map<String, String> currencies = new HashMap<>();
		currencies.put("A", "Euro");
		currencies.put("B", "Dollar");
		String preferredCurrency = null;

		// Validate user input
		while (!userAccountManagerPrompt.equalsIgnoreCase("ADD") &&
				!userAccountManagerPrompt.equalsIgnoreCase("MODIFY") &&
				!userAccountManagerPrompt.equalsIgnoreCase("DELETE")) {
			System.out.println("Wrong option. Retry: ");
			userAccountManagerPrompt = choice.nextLine();
		}
		userAccountManagerPrompt = userAccountManagerPrompt.toUpperCase();

		switch (userAccountManagerPrompt) {
			// Create User Account
			case "ADD" -> {
				System.out.println("\nAdd Account");
				System.out.println("-----------");

				System.out.println("First Name: ");
				firstName = choice.nextLine();

				System.out.println("Last Name: ");
				lastName = choice.nextLine();

				System.out.println("Username: ");
				username = choice.nextLine();

				while (true) {
					System.out.println("Birthday(YYYY-MM-DD): ");
					try {
						String birthdayInput = choice.nextLine();
						birthday = Date.valueOf(birthdayInput);
						break;
					} catch (Exception e) {
						System.out.println("Invalid format. Please enter a valid date.");
					}
				}

				// Select currency
				System.out.println("Select Currency");
				System.out.println("---------------");

				for (Map.Entry<String, String> currency : currencies.entrySet()) {
					System.out.println(currency.getKey() + " - " + currency.getValue());
				}
				currencyChoice = choice.nextLine();

				// Validate user input
				while (!currencyChoice.equalsIgnoreCase("A") &&
						!currencyChoice.equalsIgnoreCase("B")) {
					System.out.println("Wrong option. Retry: ");
					currencyChoice = choice.nextLine();
				}
				currencyChoice = currencyChoice.toUpperCase();

				// Set currency
				for (Map.Entry<String, String> currency : currencies.entrySet()) {
					if (currencyChoice.equalsIgnoreCase(currency.getKey())) {
						preferredCurrency = currency.getValue();
					}
				}

				// Set Email
				do {
					System.out.println("Email: ");
					email = choice.nextLine();
					if (!email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
						System.out.println("Invalid email type. Enter a valid email");
					}
				} while (!email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"));

				// Set Password
				do {
					System.out.println("Password: ");
					password = choice.nextLine();
					if (password.length() < 5 || !password.matches(".*[0-9\\W].*")) {
						System.out.println(
								"Invalid password format. The password must be at least 5 characters long, contain both alphanumeric characters and at least one special character.");
					}
				} while (password.length() < 5 || !password.matches(".*[0-9\\W].*"));

				userAccount = new UserAccount(firstName, lastName, username, birthday, password, email,
						preferredCurrency);
				if (userAccountManager.addAccount(userAccount)) {
					return true;
				} else {
					System.out.println("Unable to add User Account");
				}
			}
			case "MODIFY" -> {
				String modifyChoice;
				String continueModifyChoice;

				int userAccountId = authManager.getAccountId();
				userAccount = userAccountManager.getUserAccount(userAccountId);

				System.out.println("\nModify Account");
				System.out.println("--------------");

				do {
					System.out.println("Which attribute would you like to modify: ");
					System.out.println("A. First Name");
					System.out.println("B. Last Name");
					System.out.println("C. Username");
					System.out.println("D. Birthday");
					System.out.println("E. Password");
					System.out.println("F. Email");

					modifyChoice = choice.nextLine();

					// Validate user input
					while (!modifyChoice.equalsIgnoreCase("A") &&
							!modifyChoice.equalsIgnoreCase("B") &&
							!modifyChoice.equalsIgnoreCase("C") &&
							!modifyChoice.equalsIgnoreCase("D") &&
							!modifyChoice.equalsIgnoreCase("E") &&
							!modifyChoice.equalsIgnoreCase("F")) {
						System.out.println("Wrong option. Retry: ");
						modifyChoice = choice.nextLine();
					}
					modifyChoice = modifyChoice.toUpperCase();

					switch (modifyChoice) {
						case "A" -> {
							System.out.println("First Name: ");
							firstName = choice.nextLine();
							userAccount.setFirstName(firstName);
							break;
						}
						case "B" -> {
							System.out.println("Last Name: ");
							lastName = choice.nextLine();
							userAccount.setLastName(lastName);
							break;
						}
						case "C" -> {
							System.out.println("Username: ");
							username = choice.nextLine();
							userAccount.setUsername(username);
							break;
						}
						case "D" -> {
							while (true) {
								System.out.println("Birthday(YYYY-MM-DD): ");
								try {
									String birthdayInput = choice.nextLine();
									birthday = Date.valueOf(birthdayInput);
									break;
								} catch (Exception e) {
									System.out.println("Invalid format. Please enter a valid date.");
								}
							}
							userAccount.setBirthday(birthday);
							break;
						}
						case "E" -> {
							do {
								System.out.println("Password: ");
								password = choice.nextLine();
								if (password.length() < 5 || !password.matches(".*[0-9\\W].*")) {
									System.out.println(
											"Invalid password format. The password must be at least 5 characters long, contain both alphanumeric characters and at least one special character.");
								}
							} while (password.length() < 5 || !password.matches(".*[0-9\\W].*"));

							userAccount.setPassword(password);
							try {
								authManager.terminateSession(authManager.getToken(), authManager.getAccountId(), TOKEN_FILE);
							} catch (IOException ex) {
							}
							break;
						}
						case "F" -> {
							do {
								System.out.println("Email: ");
								email = choice.nextLine();
								if (!email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
									System.out.println("Invalid email type. Enter a valid email");
								}
							} while (!email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"));
							userAccount.setEmail(email);
							break;
						}
					}
					userAccountManager.editUserAccount(userAccount);
					System.out.println("Would you like to modify any other attribute Y/N: ");
					continueModifyChoice = choice.nextLine();
				} while (continueModifyChoice.equalsIgnoreCase("Y"));
				return true;
			}

			case "DELETE" -> {
				break;
			}
			default -> System.out.println("Invalid Prompt.");
		}
		return false;
	}

	@Override
	public boolean authManager(String authManagerPrompt) throws SQLException, IOException {
		// Validate user input
		while (!authManagerPrompt.equalsIgnoreCase("LOGIN") &&
				!authManagerPrompt.equalsIgnoreCase("LOGOUT") &&
				!authManagerPrompt.equalsIgnoreCase("RESET")) {
			System.out.println("Wrong option. Retry: ");
			authManagerPrompt = choice.nextLine();
		}
		authManagerPrompt = authManagerPrompt.toUpperCase();

		switch (authManagerPrompt) {
			// Login to the Expense Tracker App
			case "LOGIN" -> {
				// Load token
				String token = authManager.loadSessionToken(TOKEN_FILE);
				if (token != null) {
					String accountIdStr = authManager.validateSessionToken(token);
					if (accountIdStr != null) {
						int accountId = Integer.parseInt(accountIdStr);
						authManager.setAccountId(accountId);
						System.out.println("Welcome back, "
								+ userAccountManager.getUserAccount(accountId).getFirstName() + "!");
						authManager.setToken(token);
						return true;
					} else {
						System.out.println("Session expired or invalid. Please log in again.");
					}
				} else {
					int loginAttempts = 0;
					String username;
					String password;
					System.out.println("Login");
					System.out.println("-----");
					do {
						System.out.println("Username: ");
						username = choice.nextLine();

						System.out.println("Password: ");
						password = choice.nextLine();

						token = authManager.login(username, password, TOKEN_FILE);

						if (token != null) {
							return true;
						}

						loginAttempts++;

					} while (loginAttempts < 3);
					authManager.setFailedLoginAttempts(loginAttempts);
					System.out.println("Login failed. Please try again later.");
				}
				break;
			}

			case "LOGOUT" -> {
				return authManager.logout(authManager.getToken(), authManager.getAccountId(), TOKEN_FILE);
			}

			case "RESET" -> {
				String email;
				String password;
				int emailAttempts = 0;

				System.out.println("Reset Password");
				System.out.println("--------------");

				do {
					System.out.println("Email: ");
					email = choice.nextLine();
					emailAttempts++;
					if (emailAttempts == 3) {
						break;
					}
					if (!email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
						System.out.println("Invalid email type. Enter a valid email");
					}
				} while (!email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"));

				if (authManager.validateEmail(email)) {
					int passwordAttempts = 0;
					System.out.println("New Password");
					System.out.println("------------");

					do {
						System.out.println("Password: ");
						password = choice.nextLine();
						passwordAttempts++;
						if (passwordAttempts == 3) {
							break;
						}
						if (password.length() < 5 || !password.matches(".*[0-9\\W].*")) {
							System.out.println(
									"Invalid password format. The password must be at least 5 characters long, contain both alphanumeric characters and at least one special character.");
						}
					} while (password.length() < 5 || !password.matches(".*[0-9\\W].*"));
					String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

					if (password.length() > 5 || password.matches(".*[0-9\\W].*")) {
						return authManager.resetPassword(email, hashedPassword);
					}
				}
				break;
			}

			default -> {
				System.out.println("Invalid Prompt.");
				return false;
			}
		}
		System.out.println("You have been exited from the application.");
		return false;
	}

	@Override
	public void transactionManager(String transactionManagerPrompt)
			throws ClassNotFoundException, SQLException, IOException {
		transactionManager.fetchTransactions(authManager.getAccountId());
		String continueChoice;
		String transactionChoice;

		// Income sources
		Map<Integer, String> sourceMap = new HashMap<>();
		sourceMap.put(1, "Salary/Wages");
		sourceMap.put(2, "Business Income");
		sourceMap.put(3, "Freelance/Consulting");
		sourceMap.put(4, "Rental Income");
		sourceMap.put(5, "Investment Income");
		sourceMap.put(6, "Royalties");
		sourceMap.put(7, "Government Benefits");
		sourceMap.put(8, "Inheritance/Gifts");
		sourceMap.put(9, "Other");

		// Expense Categories
		Map<Integer, String> categoryMap = new HashMap<>();
		categoryMap.put(1, "Housing (Rent/Mortgage)");
		categoryMap.put(2, "Utilities (Electricity, Water, Internet)");
		categoryMap.put(3, "Groceries");
		categoryMap.put(4, "Transportation");
		categoryMap.put(5, "Health (Insurance/Medical)");
		categoryMap.put(6, "Education");
		categoryMap.put(7, "Debt Repayment");
		categoryMap.put(8, "Entertainment");
		categoryMap.put(9, "Clothing");
		categoryMap.put(10, "Savings/Investments");
		categoryMap.put(11, "Gifts/Donations");
		categoryMap.put(12, "Other");

		// Validate user input
		while (!transactionManagerPrompt.equalsIgnoreCase("ADD") &&
				!transactionManagerPrompt.equalsIgnoreCase("MODIFY") &&
				!transactionManagerPrompt.equalsIgnoreCase("DELETE") &&
				!transactionManagerPrompt.equalsIgnoreCase("TOTALBALANCE") &&
				!transactionManagerPrompt.equalsIgnoreCase("RECENTTRANSACTIONS") &&
				!transactionManagerPrompt.equalsIgnoreCase("EXPENSESUMMARY") &&
				!transactionManagerPrompt.equalsIgnoreCase("HISTORY")) {
			System.out.println("Wrong option. Retry: ");
			transactionManagerPrompt = choice.nextLine();
		}
		transactionManagerPrompt = transactionManagerPrompt.toUpperCase();

		switch (transactionManagerPrompt) {
			// Add a transaction
			case "ADD" -> {
				Transaction transaction = new Transaction();
				int accountId = authManager.getAccountId();
				double amount;
				Date date;

				do {
					System.out.println("\nAdd Transaction");
					System.out.println("A. Income");
					System.out.println("B. Expenses");
					transactionChoice = choice.nextLine();

					// Validate user input
					while (!transactionChoice.equalsIgnoreCase("A") &&
							!transactionChoice.equalsIgnoreCase("B")) {
						System.out.println("Wrong option. Retry: ");
						transactionChoice = choice.nextLine();
					}
					transactionChoice = transactionChoice.toUpperCase();

					switch (transactionChoice) {
						case "A" -> {
							// Add Income
							int sourceId;
							System.out.println("\nAdd Income");

							// Set amount
							while (true) {
								System.out.println("Transaction amount: ");
								try {
									amount = choice.nextDouble();
									if (amount < 0) {
										System.out.println("Invalid input. Please enter a positive value.");
									} else {
										break;
									}
								} catch (Exception e) {
									System.out.println("Invalid input. Please enter a valid number.");
									choice.nextLine();
								}
							}
							choice.nextLine();
							transaction.setAmount(amount);

							// Set source from drop-down menu
							while (true) {
								System.out.println("Transaction source: ");
								for (Map.Entry<Integer, String> entry : sourceMap.entrySet()) {
									System.out.println(entry.getKey() + " - " + entry.getValue());
								}
								try {
									sourceId = choice.nextInt();
									if (sourceId < 0 || sourceId > 9) {
										System.out.println("Invalid input. Please enter a positive value.");
									} else {
										break;
									}
								} catch (Exception e) {
									System.out.println("Invalid input. Please enter a valid option.");
									choice.nextLine();
								}
							}
							choice.nextLine();

							String description;
							if (sourceId == 9) {
								System.out.println("Enter description (Press any key to skip):");
								description = choice.nextLine();
							} else {
								description = sourceMap.get(sourceId);
							}
							transaction.setDescription(description);
							transaction.setSource(sourceMap.get(sourceId));

							// Set date
							while (true) {
								System.out.println("Transaction date(YYYY-MM-DD): ");
								try {
									String dateInput = choice.nextLine();
									date = Date.valueOf(dateInput);
									break;
								} catch (Exception e) {
									System.out.println("Invalid input. Please enter a valid date.");
								}
							}
							transaction.setDate(date);
							transactionManager.addTransaction(transaction, "INCOME", accountId);
						}

						case "B" -> {
							// Add Expenses
							int categoryId;
							System.out.println("\nAdd Expenses");

							// Set amount
							while (true) {
								System.out.println("Transaction amount: ");
								try {
									amount = choice.nextDouble();
									if (amount < 0) {
										System.out.println("Invalid input. Please enter a positive value.");
									} else {
										break;
									}
								} catch (Exception e) {
									System.out.println("Invalid input. Please enter a valid number.");
									choice.nextLine(); // Clear the invalid input
								}
							}
							choice.nextLine();
							transaction.setAmount(amount);

							// Set category from drop-down menu
							while (true) {
								System.out.println("Transaction category: ");
								for (Map.Entry<Integer, String> entry : categoryMap.entrySet()) {
									System.out.println(entry.getKey() + " - " + entry.getValue());
								}
								try {
									categoryId = choice.nextInt();
									if (categoryId < 0 || categoryId > 12) {
										System.out.println("Invalid input. Please enter a positive value.");
									} else {
										break;
									}
								} catch (Exception e) {
									System.out.println("Invalid input. Please enter a valid option.");
									choice.nextLine();
								}
							}
							choice.nextLine();

							String description;
							if (categoryId == 12) {
								System.out.println("Enter description  (Press any key to skip):");
								description = choice.nextLine();
							} else {
								description = categoryMap.get(categoryId);
							}
							transaction.setDescription(description);
							transaction.setCategory(categoryMap.get(categoryId));

							// Set date
							while (true) {
								System.out.println("Transaction date(YYYY-MM-DD): ");
								try {
									String dateInput = choice.nextLine();
									date = Date.valueOf(dateInput);
									break;
								} catch (Exception e) {
									System.out.println("Invalid input. Please enter a valid date.");
								}
							}
							transaction.setDate(date);
							transactionManager.addTransaction(transaction, "EXPENSES", accountId);
						}
					}
					System.out.println("Would you like to enter another transaction? Y/N");
					continueChoice = choice.nextLine();
				} while (continueChoice.equalsIgnoreCase("Y"));
				break;
			}

			// Modify a transaction
			case "MODIFY" -> {
				String modifyChoice;
				String continueModifyChoice;
				Transaction transaction;
				String type;
				String transactionId;
				double amount;
				Date date;

				do {
					System.out.println("\nModify Transaction");
					System.out.println("A. Income");
					System.out.println("B. Expenses");
					transactionChoice = choice.nextLine();

					// Validate user input
					while (!transactionChoice.equalsIgnoreCase("A") &&
							!transactionChoice.equalsIgnoreCase("B")) {
						System.out.println("Wrong option. Retry: ");
						transactionChoice = choice.nextLine();
					}
					transactionChoice = transactionChoice.toUpperCase();

					// Modify Income
					switch (transactionChoice) {
						case "A" -> {
							int sourceId;
							System.out.println("\nModify Income");
							type = "INCOME";

							System.out.println("Enter TransactionId: ");
							transactionId = choice.nextLine();
							transaction = transactionManager.getTransaction(transactionId, type,
									authManager.getAccountId());

							if (transaction == null) {
								break;
							}

							choice.nextLine();

							do {
								System.out.println("Which attribute would you like to modify: ");
								System.out.println("A. Amount");
								System.out.println("B. Source");
								System.out.println("C. Date");
								modifyChoice = choice.nextLine();

								// Validate user input
								while (!modifyChoice.equalsIgnoreCase("A") &&
										!modifyChoice.equalsIgnoreCase("B") &&
										!modifyChoice.equalsIgnoreCase("C")) {
									System.out.println("Wrong option. Retry: ");
									modifyChoice = choice.nextLine();
								}
								modifyChoice = modifyChoice.toUpperCase();

								switch (modifyChoice) {

									// Modify Amount
									case "A" -> {
										while (true) {
											System.out.println("Set amount: ");
											try {
												amount = choice.nextDouble();
												if (amount < 0) {
													System.out.println("Invalid input. Please enter a positive value.");
												} else {
													break;
												}
											} catch (Exception e) {
												System.out.println("Invalid input. Please enter a valid number.");
												choice.nextLine();
											}
										}
										choice.nextLine();
										transaction.setAmount(amount);
										break;
									}

									// Modify Source
									case "B" -> {
										while (true) {
											System.out.println("Transaction source: ");
											for (Map.Entry<Integer, String> entry : sourceMap.entrySet()) {
												System.out.println(entry.getKey() + " - " + entry.getValue());
											}
											try {
												sourceId = choice.nextInt();
												if (sourceId < 0 || sourceId > 9) {
													System.out.println("Invalid input. Please enter a positive value.");
												} else {
													break;
												}
											} catch (Exception e) {
												System.out.println("Invalid input. Please enter a valid option.");
												choice.nextLine();
											}
										}
										choice.nextLine();

										String description;
										if (sourceId == 9) {
											System.out.println("Enter description:");
											description = choice.nextLine();
										} else {
											description = sourceMap.get(sourceId);
										}
										transaction.setDescription(description);
										transaction.setSource(sourceMap.get(sourceId));
										break;
									}

									// Modify Date
									case "C" -> {
										while (true) {
											System.out.println("Set date(YYYY-MM-DD): ");
											try {
												String dateInput = choice.nextLine();
												date = Date.valueOf(dateInput);
												break;
											} catch (Exception e) {
												System.out.println("Invalid input. Please enter a valid date.");
											}
										}
										transaction.setDate(date);
										break;
									}
								}
								boolean result = transactionManager.editTransaction(transaction,
										authManager.getAccountId());
								if (!result) {
									break;
								}
								transactionManager.getTransaction(transactionId, type, authManager.getAccountId());
								choice.nextLine();
								System.out.println("Would you like to modify any other attribute Y/N: ");
								continueModifyChoice = choice.nextLine();
							} while (continueModifyChoice.equalsIgnoreCase("Y"));
							break;
						}

						// Modify Expenses
						case "B" -> {
							int categoryId;
							System.out.println("\nModify Expenses");
							type = "EXPENSES";

							System.out.println("Enter TransactionId: ");
							transactionId = choice.nextLine();

							transaction = transactionManager.getTransaction(transactionId, type,
									authManager.getAccountId());
							choice.nextLine();

							if (transaction == null) {
								break;
							}

							do {
								System.out.println("Which attribute would you like to modify: ");
								System.out.println("A. Amount");
								System.out.println("B. Category");
								System.out.println("C. Date");
								modifyChoice = choice.nextLine();

								// Validate user input
								while (!modifyChoice.equalsIgnoreCase("A") &&
										!modifyChoice.equalsIgnoreCase("B") &&
										!modifyChoice.equalsIgnoreCase("C")) {
									System.out.println("Wrong option. Retry: ");
									modifyChoice = choice.nextLine();
								}
								modifyChoice = modifyChoice.toUpperCase();

								switch (modifyChoice) {

									// Modify Amount
									case "A" -> {
										while (true) {
											System.out.println("Set amount: ");
											try {
												amount = choice.nextDouble();
												if (amount < 0) {
													System.out.println("Invalid input. Please enter a positive value.");
												} else {
													break;
												}
											} catch (Exception e) {
												System.out.println("Invalid input. Please enter a valid number.");
												choice.nextLine();
											}
										}
										choice.nextLine();
										transaction.setAmount(amount);
										break;
									}

									// Modify Category
									case "B" -> {
										while (true) {
											System.out.println("Transaction category: ");
											for (Map.Entry<Integer, String> entry : categoryMap.entrySet()) {
												System.out.println(entry.getKey() + " - " + entry.getValue());
											}
											try {
												categoryId = choice.nextInt();
												if (categoryId < 0 || categoryId > 12) {
													System.out.println("Invalid input. Please enter a positive value.");
												} else {
													break;
												}
											} catch (Exception e) {
												System.out.println("Invalid input. Please enter a valid option.");
												choice.nextLine();
											}
										}
										choice.nextLine();

										String description;
										if (categoryId == 9) {
											System.out.println("Enter description:");
											description = choice.nextLine();
										} else {
											description = categoryMap.get(categoryId);
										}
										transaction.setDescription(description);
										transaction.setCategory(categoryMap.get(categoryId));
										break;
									}

									// Modify date
									case "C" -> {
										while (true) {
											System.out.println("Set date(YYYY-MM-DD): ");
											try {
												String dateInput = choice.nextLine();
												date = Date.valueOf(dateInput);
												break;
											} catch (Exception e) {
												System.out.println("Invalid input. Please enter a valid date.");
											}
										}
										transaction.setDate(date);
										break;
									}
								}
								transactionManager.editTransaction(transaction, authManager.getAccountId());
								if (transactionManager.getTransaction(transactionId, type,
										authManager.getAccountId()) == null) {
									break;
								}
								choice.nextLine();
								System.out.println("\nWould you like to modify any other attribute Y/N: ");
								continueModifyChoice = choice.nextLine();
							} while (continueModifyChoice.equalsIgnoreCase("Y"));
							break;
						}
					}
					System.out.println("Would you like to modify another transaction? Y/N");
					continueChoice = choice.nextLine();
				} while (continueChoice.equalsIgnoreCase("Y"));
				break;
			}

			// Delete a transaction
			case "DELETE" -> {
				Transaction transaction;
				String type;
				String transactionId;
				String deleteConfirmation;

				do {
					System.out.println("\nDelete Transaction");
					System.out.println("A. Income");
					System.out.println("B. Expenses");
					transactionChoice = choice.nextLine();

					// Validate user input
					while (!transactionChoice.equalsIgnoreCase("A") &&
							!transactionChoice.equalsIgnoreCase("B")) {
						System.out.println("Wrong option. Retry: ");
						transactionChoice = choice.nextLine();
					}
					transactionChoice = transactionChoice.toUpperCase();

					switch (transactionChoice) {
						// Delete Income
						case "A" -> {
							System.out.println("\nDelete Income");
							type = "INCOME";

							System.out.println("Enter TransactionId: ");
							transactionId = choice.nextLine();

							transaction = transactionManager.getTransaction(transactionId, type,
									authManager.getAccountId());
							choice.nextLine();

							if (transaction == null) {
								break;
							}

							// Deletion Confirmation
							System.out.println("Are you sure that you would like to delete this transaction? Y/N");
							deleteConfirmation = choice.nextLine();

							if (deleteConfirmation.equalsIgnoreCase("Y")) {
								transactionManager.deleteTransaction(transaction.getTransactionId(),
										transaction.getAccountId());
							}
							break;
						}

						// Delete Expenses
						case "B" -> {
							System.out.println("\nDelete Expenses");
							type = "EXPENSES";

							System.out.println("Enter TransactionId: ");
							transactionId = choice.nextLine();

							transaction = transactionManager.getTransaction(transactionId, type,
									authManager.getAccountId());
							choice.nextLine();

							if (transaction == null) {
								break;
							}

							// Deletion Confirmation
							System.out.println("Are you sure that you would like to delete this transaction? Y/N");
							deleteConfirmation = choice.nextLine();

							if (deleteConfirmation.equalsIgnoreCase("Y")) {
								transactionManager.deleteTransaction(transaction.getTransactionId(),
										transaction.getAccountId());
							}
							break;
						}
					}
					System.out.println("Would you like to delete another transaction? Y/N");
					continueChoice = choice.nextLine();
				} while (continueChoice.equalsIgnoreCase("Y"));
				break;
			}

			// View total balance
			case "TOTALBALANCE" -> {
				System.out.println("\nTOTAL BALANCE");
				System.out.println("-------------");

				this.transactionManager.getTotalBalance(authManager.getAccountId());
				System.out.println(
						"Total Balance: " + this.transactionManager.getTotalBalance(authManager.getAccountId()) + " "
								+ settings.getAccountCurrency(authManager.getAccountId()));
				System.out.println("Total Income: " + this.transactionManager.getTotalIncome() + " "
						+ settings.getAccountCurrency(authManager.getAccountId()));
				System.out.println("Total Expenses: " + this.transactionManager.getTotalExpenses() + " "
						+ settings.getAccountCurrency(authManager.getAccountId()));

				choice.nextLine();
				break;
			}

			// Display recent transactions with filtering functionalities
			case "RECENTTRANSACTIONS" -> {
				int accountId = authManager.getAccountId();
				List<Transaction> transactions = transactionManager.getRecentTransactions(accountId);

				System.out.println("\nRecent Transactions:");
				transactions.forEach(System.out::println);

				System.out.println("Would you like to filter transactions? (Y/N)");
				String filterChoice = choice.nextLine();
				while (filterChoice.equalsIgnoreCase("Y")) {
					System.out.println("Choose a filter criteria: ");
					System.out.println("A - Amount");
					System.out.println("B - Source");
					System.out.println("C - Category");
					System.out.println("D - Date");
					String filterCriteria = choice.nextLine().toUpperCase();

					FilterStrategy filterStrategy;
					Double amountStart = null, amountEnd = null;
					Date dateStart = null, dateEnd = null;
					String category = null, source = null;

					switch (filterCriteria) {
						case "A" -> {
							filterStrategy = new AmountFilter();
							System.out.print("Start amount: ");
							amountStart = choice.nextDouble();
							System.out.print("End amount: ");
							amountEnd = choice.nextDouble();
							choice.nextLine();
						}
						case "B" -> {
							filterStrategy = new SourceFilter();
							System.out.print("Enter Source: ");
							source = choice.nextLine();
						}
						case "C" -> {
							filterStrategy = new CategoryFilter();
							System.out.print("Enter Category: ");
							category = choice.nextLine();
						}
						case "D" -> {
							filterStrategy = new DateFilter();
							System.out.print("Start date (YYYY-MM-DD): ");
							dateStart = java.sql.Date.valueOf(choice.nextLine());
							System.out.print("End date (YYYY-MM-DD): ");
							dateEnd = java.sql.Date.valueOf(choice.nextLine());
						}
						default -> {
							System.out.println("Invalid filter criteria.");
							continue;
						}
					}

					if (filterStrategy != null) {
						List<Transaction> filtered = filterStrategy.filter(
								amountStart, amountEnd, dateStart, dateEnd, category, source, transactions);
						System.out.println("\nFiltered Transactions:");
						filtered.forEach(System.out::println);
					}

					System.out.println("Would you like to filter based on another criteria? (Y/N)");
					filterChoice = choice.nextLine();
				}
				System.out.println("Press any key to continue.");
				choice.nextLine();
				break;
			}

			case "EXPENSESUMMARY" -> {
				System.out.println("\nExpense Summary:");
				System.out.println("----------------\n");

				boolean result = expenseSummary.getExpensesSummary(authManager.getAccountId());
				if (result) {
					System.out.println("Total Expenses: " + this.expenseSummary.getTotalExpenses() + " "
							+ settings.getAccountCurrency(authManager.getAccountId()));
					System.out.println("\nHighest Category: " + this.expenseSummary.getHighestCategory());

					System.out.println("\nExpenses Percentage:");
					System.out.println("--------------------");
					for (Map.Entry<String, String> entry : this.expenseSummary.getExpensesPercentage().entrySet()) {
						System.out.println(entry.getKey() + " - " + entry.getValue() + "%");
					}

					System.out.println("\nExpenses by Category:");
					System.out.println("---------------------");
					for (Map.Entry<String, String> entry : this.expenseSummary.getExpensesByCategory().entrySet()) {
						System.out.println(entry.getKey() + " - " + entry.getValue() + " "
								+ settings.getAccountCurrency(authManager.getAccountId()));
					}
				}
				choice.nextLine();
				break;
			}

			case "HISTORY" -> {
				int accountId = authManager.getAccountId();
				List<Transaction> transactions = transactionManager.getTransactions(accountId);

				System.out.println("\nTransaction History: \n");
				transactions.forEach(System.out::println);
				choice.nextLine();
				break;
			}
		}
	}

	public void reportSummary() throws SQLException, IOException {
		String reportType;
		String continueReportChoice;
		String exportChoice;

		System.out.println("\nReport Summary:");
		System.out.println("---------------\n");

		do {
			System.out.println("Choose a report type: ");
			System.out.println("A - Monthly");
			System.out.println("B - Yearly");
			System.out.println("C - General");
			reportType = choice.nextLine();

			// Validate user input
			while (!reportType.equalsIgnoreCase("A") &&
					!reportType.equalsIgnoreCase("B") &&
					!reportType.equalsIgnoreCase("C")) {
				System.out.println("Wrong option. Retry: ");
				reportType = choice.nextLine();
			}
			reportType = reportType.toUpperCase();

			switch (reportType) {
				case "A" -> {
					System.out.println("Enter the target month (YYYY-MM): ");
					String targetMonth = null;
					while (true) {
						try {
							targetMonth = choice.nextLine();
							YearMonth.parse(targetMonth);
							break;
						} catch (Exception e) {
							System.out.println("Invalid input. Please enter a month.");
						}
					}

					ReportStrategy reportStrategy = new MonthlyReport();
					boolean reportResult = reportStrategy.generateReport(authManager.getAccountId(), targetMonth, null,
							this.reportSummary);

					if (reportResult) {
						System.out.println("Report Summary\n");
						System.out.println("Total Income: " + this.reportSummary.getTotalIncome() + " "
								+ settings.getAccountCurrency(this.authManager.getAccountId()));
						System.out.println("Total Expenses: " + this.reportSummary.getTotalExpenses() + " "
								+ settings.getAccountCurrency(this.authManager.getAccountId()));
						System.out.println("Total Balance: " + this.reportSummary.getTotalBalance() + " "
								+ settings.getAccountCurrency(this.authManager.getAccountId()));
						System.out.println("Highest Source: " + this.reportSummary.getHighestSource());
						System.out.println("Highest Category: " + this.reportSummary.getHighestCategory());

						if (!this.reportSummary.getIncomeBySource().isEmpty()) {
							System.out.println("\nIncome by Source:");
							System.out.println("-----------------");
							for (Map.Entry<String, String> entry : this.reportSummary.getIncomeBySource().entrySet()) {
								for (Map.Entry<String, String> percentage : this.reportSummary.getIncomeBySource()
										.entrySet()) {
									if (entry.getKey().equalsIgnoreCase(percentage.getKey()))
										System.out.println(
												entry.getKey() + " - " + entry.getValue() + " (" + percentage.getValue()
														+ "%)");
								}
							}
						}

						if (!this.reportSummary.getExpensesByCategory().isEmpty()) {
							System.out.println("\nExpenses by Category:");
							System.out.println("---------------------");
							for (Map.Entry<String, String> entry : this.reportSummary.getExpensesByCategory()
									.entrySet()) {
								for (Map.Entry<String, String> percentage : this.reportSummary.getExpensesByCategory()
										.entrySet()) {
									if (entry.getKey().equalsIgnoreCase(percentage.getKey()))
										System.out.println(
												entry.getKey() + " - " + entry.getValue() + " (" + percentage.getValue()
														+ "%)");
								}
							}
						}
						choice.nextLine();
						System.out.println("Would you like to export to .csv Y/N: ");
						exportChoice = choice.nextLine();

						if (exportChoice.equalsIgnoreCase("Y")) {
							this.reportSummary.exportToCSV(targetMonth + "_report_summary.csv");
						}
					}
					break;
				}
				case "B" -> {
					System.out.println("Enter the target year (YYYY): ");
					String targetYear = null;
					while (true) {
						try {
							targetYear = choice.nextLine();
							Year.parse(targetYear);
							break;
						} catch (Exception e) {
							System.out.println("Invalid input. Please enter a valid year.");
						}
					}

					ReportStrategy reportStrategy = new YearlyReport();
					boolean reportResult = reportStrategy.generateReport(authManager.getAccountId(), null, targetYear,
							this.reportSummary);
					if (reportResult) {
						System.out.println("Report Summary\n");
						System.out.println("Total Income: " + this.reportSummary.getTotalIncome() + " "
								+ settings.getAccountCurrency(this.authManager.getAccountId()));
						System.out.println("Total Expenses: " + this.reportSummary.getTotalExpenses() + " "
								+ settings.getAccountCurrency(this.authManager.getAccountId()));
						System.out.println("Total Balance: " + this.reportSummary.getTotalBalance() + " "
								+ settings.getAccountCurrency(this.authManager.getAccountId()));
						System.out.println("Highest Source: " + this.reportSummary.getHighestSource());
						System.out.println("Highest Category: " + this.reportSummary.getHighestCategory());

						if (!this.reportSummary.getIncomeBySource().isEmpty()) {
							System.out.println("\nIncome by Source:");
							System.out.println("-----------------");
							for (Map.Entry<String, String> entry : this.reportSummary.getIncomeBySource().entrySet()) {
								for (Map.Entry<String, String> percentage : this.reportSummary.getIncomeBySource()
										.entrySet()) {
									if (entry.getKey().equalsIgnoreCase(percentage.getKey()))
										System.out.println(
												entry.getKey() + " - " + entry.getValue() + " (" + percentage.getValue()
														+ "%)");
								}
							}
						}

						if (!this.reportSummary.getExpensesByCategory().isEmpty()) {
							System.out.println("\nExpenses by Category:");
							System.out.println("---------------------");
							for (Map.Entry<String, String> entry : this.reportSummary.getExpensesByCategory()
									.entrySet()) {
								for (Map.Entry<String, String> percentage : this.reportSummary.getExpensesByCategory()
										.entrySet()) {
									if (entry.getKey().equalsIgnoreCase(percentage.getKey()))
										System.out.println(
												entry.getKey() + " - " + entry.getValue() + " (" + percentage.getValue()
														+ "%)");
								}
							}
						}
						choice.nextLine();
						System.out.println("Would you like to export to .csv Y/N: ");
						exportChoice = choice.nextLine();

						if (exportChoice.equalsIgnoreCase("Y")) {
							this.reportSummary.exportToCSV(targetYear + "_report_summary.csv");
						}
					}
					break;
				}
				case "C" -> {
					boolean reportResult = reportSummary.generateReportSummary(authManager.getAccountId(), null, null);
					if (reportResult) {
						System.out.println("Report Summary\n");
						System.out.println("Total Income: " + this.reportSummary.getTotalIncome() + " "
								+ settings.getAccountCurrency(this.authManager.getAccountId()));
						System.out.println("Total Expenses: " + this.reportSummary.getTotalExpenses() + " "
								+ settings.getAccountCurrency(this.authManager.getAccountId()));
						System.out.println("Total Balance: " + this.reportSummary.getTotalBalance() + " "
								+ settings.getAccountCurrency(this.authManager.getAccountId()));
						System.out.println("Highest Source: " + this.reportSummary.getHighestSource());
						System.out.println("Highest Category: " + this.reportSummary.getHighestCategory());

						if (!this.reportSummary.getIncomeBySource().isEmpty()) {
							System.out.println("\nIncome by Source:");
							System.out.println("-----------------");
							for (Map.Entry<String, String> entry : this.reportSummary.getIncomeBySource().entrySet()) {
								for (Map.Entry<String, String> percentage : this.reportSummary.getIncomeBySource()
										.entrySet()) {
									if (entry.getKey().equalsIgnoreCase(percentage.getKey()))
										System.out.println(
												entry.getKey() + " - " + entry.getValue() + " (" + percentage.getValue()
														+ "%)");
								}
							}
						}

						if (!this.reportSummary.getExpensesByCategory().isEmpty()) {
							System.out.println("\nExpenses by Category:");
							System.out.println("---------------------");
							for (Map.Entry<String, String> entry : this.reportSummary.getExpensesByCategory()
									.entrySet()) {
								for (Map.Entry<String, String> percentage : this.reportSummary.getExpensesByCategory()
										.entrySet()) {
									if (entry.getKey().equalsIgnoreCase(percentage.getKey()))
										System.out.println(
												entry.getKey() + " - " + entry.getValue() + " (" + percentage.getValue()
														+ "%)");
								}
							}
						}
						choice.nextLine();
						System.out.println("Would you like to export to .csv Y/N: ");
						exportChoice = choice.nextLine();

						if (exportChoice.equalsIgnoreCase("Y")) {
							this.reportSummary.exportToCSV("general_report_summary.csv");
						}
					}
					break;
				}
				default -> {
					System.out.println("Invalid report type");
				}
			}

			System.out.println("Would you like to generate another report Y/N: ");
			continueReportChoice = choice.nextLine();
		} while (continueReportChoice.equalsIgnoreCase("Y"));
	}

	@Override
	public void settings() throws SQLException {
		int accountId = authManager.getAccountId();
		String currencyChoice;
		Map<String, String> currencies = new HashMap<>();
		currencies.put("A", "Euro");
		currencies.put("B", "Dollar");
		String newCurrency = null;

		System.out.println("\nSettings:");
		System.out.println("---------\n");

		System.out.println("Current Currency: " + settings.getAccountCurrency(accountId));
		System.out.println("Would you like to change your currency? (Y/N)");
		String changeChoice = choice.nextLine();

		if (changeChoice.equalsIgnoreCase("Y")) {
			// Select currency
			System.out.println("Select Currency");
			System.out.println("---------------");

			for (Map.Entry<String, String> currency : currencies.entrySet()) {
				System.out.println(currency.getKey() + " - " + currency.getValue());
			}
			currencyChoice = choice.nextLine();

			// Validate user input
			while (!currencyChoice.equalsIgnoreCase("A") &&
					!currencyChoice.equalsIgnoreCase("B")) {
				System.out.println("Wrong option. Retry: ");
				currencyChoice = choice.nextLine();
			}
			currencyChoice = currencyChoice.toUpperCase();

			// Set currency
			for (Map.Entry<String, String> currency : currencies.entrySet()) {
				if (currencyChoice.equalsIgnoreCase(currency.getKey())) {
					newCurrency = currency.getValue();
				}
			}
			boolean result = settings.changeAccountCurrency(accountId, newCurrency);
			if (result) {
				System.out.println("Currency updated to: " + newCurrency);
			} else {
				System.out.println("Currency was not updated");
			}
		}
		System.out.println("\nPress any key to continue to the main menu.");
		choice.nextLine();
	}

	@Override
	public void dataStorage() throws SQLException {
		String dataChoice;
		System.out.println("A. Save Data");
		System.out.println("B. Load Data");
		dataChoice = choice.nextLine();

		// Validate user input
		while (!dataChoice.equalsIgnoreCase("A") &&
				!dataChoice.equalsIgnoreCase("B")) {
			System.out.println("Wrong option. Retry: ");
			dataChoice = choice.nextLine();
		}
		dataChoice = dataChoice.toUpperCase();

		switch (dataChoice) {
			case "A" -> {
				try {
					try {
						String CSV_FILE_PATH = "./expense_tracker/transactions.csv";
						dataStorage.saveData(CSV_FILE_PATH, authManager.getAccountId());
					} catch (SQLException ex) {
					}
				} catch (IOException e) {
				}
				break;
			}
			case "B" -> {
				try {
					String CSV_FILE_PATH = "./expense_tracker/transactions.csv";
					dataStorage.loadData(CSV_FILE_PATH, authManager.getAccountId());
				} catch (IOException ex) {
				}
				break;
			}
		}
	}

	@Override
	public void run() throws ClassNotFoundException, SQLException, IOException {
		String entryChoice;
		int choiceAttempts = 0;
		System.out.println("A - Login");
		System.out.println("B - Forgot Password");
		System.out.println("C - Create Account");
		entryChoice = choice.nextLine();

		// Validate user input
		while (!entryChoice.equalsIgnoreCase("A") &&
				!entryChoice.equalsIgnoreCase("B") &&
				!entryChoice.equalsIgnoreCase("C")) {
			choiceAttempts++;
			if (choiceAttempts == 3) {
				break;
			}
			System.out.println("Wrong option. Retry: ");
			entryChoice = choice.nextLine();
		}
		entryChoice = entryChoice.toUpperCase();

		switch (entryChoice) {
			case "A" -> {
				expenseTracker();
				break;
			}
			case "B" -> {
				authManager("LOGOUT");
				boolean passwordReset = authManager("RESET");
				if (passwordReset) {
					expenseTracker();
				} else {
					System.out.println("Unable to reset password");
				}
				break;
			}
			case "C" -> {
				authManager("LOGOUT");
				boolean accountCreation = userAccountManager("ADD");
				if (accountCreation) {
					expenseTracker();
				} else {
					System.out.println("Unable to create account");
				}
				break;
			}
		}
	}

	public void expenseTracker() throws SQLException, ClassNotFoundException, IOException {
		boolean login = authManager("LOGIN");

		if (login == false && authManager.getFailedLoginAttempts() == 3) {
			boolean resetPassword = authManager("RESET");
			if (resetPassword) {
				expenseTracker();
			} else {
				System.out.println("Unable to reset password");
			}
		} else {
			while (login) {
				// Main Page
				String mainMenuChoice;
				int choiceAttempts = 0;

				System.out.println("Expense-Tracker");
				System.out.println("---------------");

				System.out.println("A - View Total Balance");
				System.out.println("B - Transaction Manager");
				System.out.println("C - Generate Report Summary");
				System.out.println("D - Data Storage");
				System.out.println("E - Account Manager");
				System.out.println("F - Settings");
				System.out.println("G - Logout");
				mainMenuChoice = choice.nextLine();

				// Validate user input
				while (!mainMenuChoice.equalsIgnoreCase("A") &&
						!mainMenuChoice.equalsIgnoreCase("B") &&
						!mainMenuChoice.equalsIgnoreCase("C") &&
						!mainMenuChoice.equalsIgnoreCase("D") &&
						!mainMenuChoice.equalsIgnoreCase("E") &&
						!mainMenuChoice.equalsIgnoreCase("F") &&
						!mainMenuChoice.equalsIgnoreCase("G")) {
					choiceAttempts++;
					if (choiceAttempts == 3) {
						break;
					}
					System.out.println("Wrong option. Retry: ");
					mainMenuChoice = choice.nextLine();
				}
				mainMenuChoice = mainMenuChoice.toUpperCase();

				switch (mainMenuChoice) {
					case "A" -> {
						transactionManager("TOTALBALANCE");
						break;
					}

					// Transaction Manager Page
					case "B" -> {
						String transactionManagerChoice;

						System.out.println("\nTransaction Manager");
						System.out.println("-------------------");

						System.out.println("A - View Recent Transactions");
						System.out.println("B - View Expense Summary");
						System.out.println("C - Add Transaction");
						System.out.println("D - Edit Transaction");
						System.out.println("E - Delete Transaction");
						System.out.println("F - View Transaction History");
						transactionManagerChoice = choice.nextLine();

						choiceAttempts = 0;
						// Validate user input
						while (!transactionManagerChoice.equalsIgnoreCase("A") &&
								!transactionManagerChoice.equalsIgnoreCase("B") &&
								!transactionManagerChoice.equalsIgnoreCase("C") &&
								!transactionManagerChoice.equalsIgnoreCase("D") &&
								!transactionManagerChoice.equalsIgnoreCase("E") &&
								!transactionManagerChoice.equalsIgnoreCase("F")) {
							choiceAttempts++;
							if (choiceAttempts == 3) {
								break;
							}
							System.out.println("Wrong option. Retry: ");
							transactionManagerChoice = choice.nextLine();
						}
						transactionManagerChoice = transactionManagerChoice.toUpperCase();

						switch (transactionManagerChoice) {
							case "A" -> {
								transactionManager("RECENTTRANSACTIONS");
								break;
							}

							case "B" -> {
								transactionManager("EXPENSESUMMARY");
								break;
							}

							case "C" -> {
								transactionManager("ADD");
								break;
							}

							case "D" -> {
								transactionManager("MODIFY");
								break;
							}

							case "E" -> {
								transactionManager("DELETE");
								break;
							}

							case "F" -> {
								transactionManager("HISTORY");
								break;
							}
						}
						break;
					}

					case "C" -> {
						reportSummary();
						break;
					}

					case "D" -> {
						dataStorage();
						break;
					}

					case "E" -> {
						userAccountManager("MODIFY");
						break;
					}

					case "F" -> {
						settings();
						break;
					}

					case "G" -> {
						authManager("LOGOUT");
						login = false;
						System.out.println("You have been logged out.");
						break;
					}

					default -> {
						authManager("LOGOUT");
						login = false;
						System.out.println("You have been logged out.");
					}
				}
			}
		}
		database.closeConnection();
	}
}