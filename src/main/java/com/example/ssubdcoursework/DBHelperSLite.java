package com.example.ssubdcoursework;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class DBHelperSLite {
    private Connection connection;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:Currencies.db");
    }

    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public ResultSet executeQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public int executeUpdate(String query) throws SQLException {
        System.out.println("query: " + query);
        Statement statement = getConnection().createStatement();
        return statement.executeUpdate(query);
    }

    public ObservableList<Currencies> getCurrencies(String addQuery) throws SQLException{
        ObservableList<Currencies> data = FXCollections.observableArrayList();
        ResultSet rs = executeQuery("SELECT* FROM Currencies"+addQuery);
        while(rs.next()){
            Currencies currency = new Currencies();
            currency.set_id(rs.getInt("_id"));
            currency.setCountry(rs.getString("country"));
            currency.setCurrency_name (rs.getString("currency_name"));
            currency.setShort_form(rs.getString("short_form"));
            currency.setCurrency_symbol(rs.getString("currency_symbol"));
            data.add(currency);
        }
        return data;
    }

    public ObservableList<Accounts> getAccounts(String addQuery) throws SQLException{
        ObservableList<Accounts> data = FXCollections.observableArrayList();
        ResultSet rs = executeQuery("SELECT* FROM Accounts"+addQuery);
        while(rs.next()){
            Accounts account = new Accounts();
            account.set_id(rs.getInt("_id"));
            account.setAccount_name(rs.getString("account_name"));
            account.setAccount_type (  rs.getInt("account_type")  );
            account.setMoney(rs.getDouble("money"));
            account.setCurrency_id(rs.getInt("currency_id"));
            data.add(account);
        }
        return data;
    }

    public Connection getConnection() {
        return connection;
    }

    public ObservableList<Categories> getCategories(String addQuery) throws SQLException {
        ObservableList<Categories> data = FXCollections.observableArrayList();
        ResultSet rs = executeQuery("SELECT* FROM Categories"+addQuery);
        while(rs.next()){
            Categories category = new Categories();
            category.set_id(rs.getInt("_id"));
            category.setCategory_name(rs.getString("category_name"));
            category.setType (rs.getString("type"));
            category.setImg_path(rs.getString("img_path"));
            data.add(category);
        }
        return data;
    }

    public ObservableList<Transactions> getTransactions(String addQuery) throws SQLException {
        ObservableList<Transactions> data = FXCollections.observableArrayList();
        ResultSet rs = executeQuery("SELECT* FROM Transactions"+addQuery);
        while(rs.next()){
            Transactions transaction = new Transactions();
            transaction.set_id(rs.getInt("_id"));
            transaction.setCategory_id(rs.getInt("category_id"));
            transaction.setAmount(rs.getInt("amount"));
            transaction.setDate (rs.getDate("date"));
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
            accountsType.set_id(rs.getInt("_id"));
            accountsType.setType_name(rs.getString("type_name"));
            accountsType.setType_path(rs.getString("type_path"));
            data.add(accountsType);
        }
        return data;
    }
}