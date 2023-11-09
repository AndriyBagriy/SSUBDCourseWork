package com.example.ssubdcoursework;

public class Currencies {
    private int _id;
    String country;
    String currency_name;
    String short_form;
    String currency_symbol;

    public Currencies(int _id, String country, String currency_name, String short_form, String currency_symbol) {
        this._id = _id;
        this.country = country;
        this.currency_name = currency_name;
        this.short_form = short_form;
        this.currency_symbol = currency_symbol;
    }

    public Currencies() {}

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrency_name() {
        return currency_name;
    }

    public void setCurrency_name(String currency_name) {
        this.currency_name = currency_name;
    }

    public String getShort_form() {
        return short_form;
    }

    public void setShort_form(String short_form) {
        this.short_form = short_form;
    }

    public String getCurrency_symbol() {
        return currency_symbol;
    }

    public void setCurrency_symbol(String currency_symbol) {
        this.currency_symbol = currency_symbol;
    }

    public String toString() {
        return country+" - "+currency_name + " (" + short_form + ")";
    }
}
