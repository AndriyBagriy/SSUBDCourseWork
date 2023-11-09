package com.example.ssubdcoursework;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;

public class Transactions {

    private int _id;
    private int category_id;
    private double amount;
    private Date date;
    private int account_id;

    public Transactions() {}

    public Transactions(int _id, int category_id, double amount, Date date, int account_id) {
        this._id = _id;
        this.category_id = category_id;
        this.amount = amount;
        this.date = date;
        this.account_id = account_id;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setAccount_id(int account_id){this.account_id = account_id;}

    public int getAccount_id(){return account_id;}
}
