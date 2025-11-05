-- (Optional) keep everything in the public schema
-- CREATE SCHEMA expense_tracker;
-- SET search_path TO expense_tracker;

-- Drop old tables (in the right dependency order)
DROP TABLE IF EXISTS sessions CASCADE;
DROP TABLE IF EXISTS expenses CASCADE;
DROP TABLE IF EXISTS income CASCADE;
DROP TABLE IF EXISTS user_account CASCADE;

-- user account table
CREATE TABLE IF NOT EXISTS user_account (
    account_id SERIAL PRIMARY KEY,
    first_name VARCHAR(20) NOT NULL,
    last_name VARCHAR(20) NOT NULL,
    username VARCHAR(20) UNIQUE NOT NULL,
    birthday DATE NOT NULL,
    currency VARCHAR(10) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    system_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- income table
CREATE TABLE IF NOT EXISTS income (
    account_id INT NOT NULL REFERENCES user_account(account_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    transaction_id CHAR(36) PRIMARY KEY,
    type VARCHAR(10) NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    source VARCHAR(50) NOT NULL,
    description VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    system_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- expenses table
CREATE TABLE IF NOT EXISTS expenses (
    account_id INT NOT NULL REFERENCES user_account(account_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    transaction_id CHAR(36) PRIMARY KEY,
    type VARCHAR(10) NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    system_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- sessions table
CREATE TABLE IF NOT EXISTS sessions (
    session_id CHAR(36) PRIMARY KEY,
    account_id INT NOT NULL REFERENCES user_account(account_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ
);

-- trigger function to set expires_at
CREATE OR REPLACE FUNCTION set_expires_at()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.expires_at IS NULL THEN
    NEW.expires_at := NEW.created_at + INTERVAL '24 hours';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- trigger definition
DROP TRIGGER IF EXISTS set_expires_at ON sessions;
CREATE TRIGGER set_expires_at
BEFORE INSERT ON sessions
FOR EACH ROW
EXECUTE FUNCTION set_expires_at();
