package com.example.ssubdcoursework;

public class Accounts {
    private int _id;
    String account_name;
    int account_type;
    double money;
    int currency_id;

    public Accounts() {
    }

    public Accounts(int _id, String account_name, int account_type, int money, int currency_id) {
        this._id = _id;
        this.account_name = account_name;
        this.account_type = account_type;
        this.money = money;
        this.currency_id = currency_id;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getAccount_name() {
        return account_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public int getAccount_type() {
        return account_type;
    }

    public void setAccount_type(int account_type) {
        this.account_type = account_type;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public int getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(int currency_id) {
        this.currency_id = currency_id;
    }
}
