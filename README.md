# expense-tracker

## Technologies Used
[![Maven](https://img.shields.io/badge/Maven-%23C71A36.svg?logo=apache-maven&logoColor=white)](https://maven.apache.org/)
- Programming Language: Java
- Dependency Management: Apache Maven
- Database: Postgres SQL

## Features
- Add, edit, and delete transaction: Users can manage individual transactions with full CRUD (Create, Read, Update, Delete) operations
- View Total Balance: Get an instant summary of your account's financial status
- Expense Summary: Users can view a detailed summary of their expenses in a categorized manner
- View Recent Transactions with filter options (date, amount, source and category)
- Data Storage: Users can import and export a bulk list of transactions

## Setup Instructions
1. Clone the repository: 
- git clone https://github.com/yourusername/expense-tracker.git
- Navigate to the project directory: cd expense-tracker

2. Set up the datatbase: 
- Ensure you have Postgres SQL installed on your system.
- Create a new Postgres schema for the application.
- Navigate to the sql-queries folder in the repository and execute the provided SQL file to create the necessary tables: expense_tracker/backend/sql_queries/postgre-queries.sql

3. Configure the environment variables: Set up the following environment variables with your Postgres credentials:
- Windows: Search for "Environment Variables" and add the following:
  - PG_USER=yourusername
  - PG_PASSWORD=yourpassword
  - PG_HOST=yourhostname
  - PG_PORT=yourport
  - PG_DB=expense_tracker

- Mac/Linux: Add these lines to your .bashrc or .zshrc file:
  - export PG_USER=yourusername
  - export PG_PASSWORD=yourpassword
  - export PG_HOST=yourhostname
  - export PG_PORT=yourport
  - export PG_DB=expense_tracker
- Then, reload the file: source ~/.bashrc

## Springboot
1. Run springboot on console: 
mvn spring-boot:run

## React
1. Run react on console:
npm start

## Contributing
Contributions are welcome! Please feel free to fork the repository, open issues, or submit pull requests.

## Notes
- You cannot log into 2 devices simultaneously
- Retry logic implemented for Neo database