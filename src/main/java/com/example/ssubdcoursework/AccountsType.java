package com.example.ssubdcoursework;

public class AccountsType {
    private int _id;
    String type_name;
    String type_path;

    public AccountsType() {
    }

    public AccountsType(int _id, String type_name, String type_path) {
        this._id = _id;
        this.type_name = type_name;
        this.type_path = type_path;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getType_name() {
        return type_name;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public String getType_path() {
        return type_path;
    }

    public void setType_path(String type_path) {
        this.type_path = type_path;
    }

    @Override
    public String toString() {
        return type_name;
    }
}
