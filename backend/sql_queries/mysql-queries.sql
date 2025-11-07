-- ===============================
-- DROP in dependency-safe order
-- ===============================
DROP TRIGGER IF EXISTS set_expires_at;
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS income;
DROP TABLE IF EXISTS user_account;

-- ===============================
-- user_account
-- ===============================
CREATE TABLE IF NOT EXISTS user_account (
  account_id INT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(20) NOT NULL,
  last_name  VARCHAR(20) NOT NULL,
  username   VARCHAR(20) UNIQUE NOT NULL,
  birthday   DATE NOT NULL,
  currency   VARCHAR(10) NOT NULL,
  password   VARCHAR(100) NOT NULL,
  email      VARCHAR(50) UNIQUE NOT NULL,
  system_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===============================
-- income
-- ===============================
CREATE TABLE IF NOT EXISTS income (
  account_id INT NOT NULL,
  transaction_id CHAR(36) PRIMARY KEY,
  type VARCHAR(10) NOT NULL,
  amount DOUBLE NOT NULL,
  source VARCHAR(50) NOT NULL,
  description VARCHAR(100) NOT NULL,
  date DATE NOT NULL,
  system_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT income_accountId_fkey
    FOREIGN KEY (account_id)
    REFERENCES user_account(account_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

-- ===============================
-- expenses
-- ===============================
CREATE TABLE IF NOT EXISTS expenses (
  account_id INT NOT NULL,
  transaction_id CHAR(36) PRIMARY KEY,
  type VARCHAR(10) NOT NULL,
  amount DOUBLE NOT NULL,
  category VARCHAR(50) NOT NULL,
  description VARCHAR(100) NOT NULL,
  date DATE NOT NULL,
  system_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT expenses_accountId_fkey
    FOREIGN KEY (account_id)
    REFERENCES user_account(account_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

-- ===============================
-- sessions (with token column)
-- ===============================
CREATE TABLE IF NOT EXISTS sessions (
  session_id CHAR(36) PRIMARY KEY,
  account_id INT NOT NULL,
  token VARCHAR(255) UNIQUE NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at DATETIME,
  CONSTRAINT sessions_accountId_fkey
    FOREIGN KEY (account_id)
    REFERENCES user_account(account_id)
    ON DELETE CASCADE
);

-- ===============================
-- Trigger: set_expires_at
-- ===============================
DELIMITER //
CREATE TRIGGER set_expires_at
BEFORE INSERT ON sessions
FOR EACH ROW
BEGIN
  IF NEW.expires_at IS NULL THEN
    SET NEW.expires_at = DATE_ADD(NEW.created_at, INTERVAL 24 HOUR);
  END IF;
END;
//
DELIMITER ;

-- ===============================
-- Sample selects
-- ===============================
SELECT * FROM user_account;
SELECT * FROM income;
SELECT * FROM expenses;
SELECT * FROM sessions;