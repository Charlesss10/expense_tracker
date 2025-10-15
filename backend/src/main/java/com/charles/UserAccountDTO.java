package com.charles;

import java.sql.Date;

public class UserAccountDTO {
    private String firstName;
    private String lastName;
    private String username;
    private Date birthday;
    private String currency;
    private String email;
    private int accountId;

    public UserAccountDTO(UserAccount ua) {
        this.firstName = ua.getFirstName();
        this.lastName = ua.getLastName();
        this.username = ua.getUsername();
        this.birthday = ua.getBirthday();
        this.currency = ua.getCurrency();
        this.email = ua.getEmail();
        this.accountId = ua.getAccountId();
    }
    // getters and setters...

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}