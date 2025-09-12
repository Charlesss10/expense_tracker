package com.charles;

import java.sql.SQLException;

public abstract class TransactionList implements Observer {
    protected final Database database = Database.getInstance();
    protected Settings settings;
    protected TransactionManager transactionManager;

    public TransactionList(Settings settings, TransactionManager transactionManager) throws SQLException {
        this.settings = settings;
        this.transactionManager = transactionManager;
    }
}