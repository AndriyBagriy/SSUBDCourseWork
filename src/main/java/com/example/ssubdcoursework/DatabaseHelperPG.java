package com.example.ssubdcoursework;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class DatabaseHelperPG {
    private static final String DB_URL = "jdbc:postgresql://localhost:5433/finances";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "admin";
    private Connection connection;
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public ResultSet executeQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public int executeUpdate(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeUpdate(query);
    }

    public ObservableList<Accounts> getAccounts() throws SQLException{
        ObservableList<Accounts> data = FXCollections.observableArrayList();
        ResultSet rs = executeQuery("SELECT* FROM Accounts");
        while(rs.next()){
            Accounts account = new Accounts();
            account.set_id(rs.getInt("id"));
            account.setAccount_name(rs.getString("account_name"));
            account.setAccount_type (rs.getInt("account_type")  );
            account.setMoney(rs.getDouble("account_money"));
            account.setCurrency_id(rs.getInt("currency_id"));
            data.add(account);
        }
        return data;
    }

    public ObservableList<Categories> getCategories() throws SQLException {
        ObservableList<Categories> data = FXCollections.observableArrayList();
        ResultSet rs = executeQuery("SELECT* FROM Categories");
        while(rs.next()){
            Categories category = new Categories();
            category.set_id(rs.getInt("id"));
            category.setCategory_name(rs.getString("category_name"));
            category.setType (rs.getString("category_type"));
            category.setImg_path(rs.getString("img_path"));
            data.add(category);
        }
        return data;
    }

    public ObservableList<Transactions> getTransactions() throws SQLException {
        ObservableList<Transactions> data = FXCollections.observableArrayList();
        ResultSet rs = executeQuery("SELECT* FROM Transactions");
        while(rs.next()){
            Transactions transaction = new Transactions();
            transaction.set_id(rs.getInt("id"));
            transaction.setCategory_id(rs.getInt("category_id"));
            transaction.setAmount(rs.getInt("amount"));
            transaction.setDate (rs.getDate("transact_date"));
            transaction.setAccount_id(rs.getInt("account_id"));
            data.add(transaction);
        }
        return data;
    }

    public ObservableList<AccountsType> getAccountType() throws SQLException{
        ObservableList<AccountsType> data = FXCollections.observableArrayList();
        ResultSet rs = executeQuery("SELECT* FROM AccountType");
        while(rs.next()){
            AccountsType accountsType = new AccountsType();
            accountsType.set_id(rs.getInt("id"));
            accountsType.setType_name(rs.getString("type_name"));
            accountsType.setType_path(rs.getString("type_path"));
            data.add(accountsType);
        }
        return data;
    }

    public ObservableList<Currencies> getCurrencies() throws SQLException{
        ObservableList<Currencies> data = FXCollections.observableArrayList();
        ResultSet rs = executeQuery("SELECT* FROM Currencies");
        while(rs.next()){
            Currencies currency = new Currencies();
            currency.set_id(rs.getInt("id"));
            currency.setCountry(rs.getString("country"));
            currency.setCurrency_name (rs.getString("currency_name"));
            currency.setShort_form(rs.getString("short_form"));
            currency.setCurrency_symbol(rs.getString("currency_symbol"));
            data.add(currency);
        }
        return data;
    }

}
