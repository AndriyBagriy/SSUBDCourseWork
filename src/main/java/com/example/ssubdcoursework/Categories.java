package com.example.ssubdcoursework;

public class Categories {
    private int _id;
    String category_name;
    String type;
    String img_path;

    public Categories() {
    }

    public Categories(int _id, String category_name, String type, String img_path) {
        this._id = _id;
        this.category_name = category_name;
        this.type = type;
        this.img_path = img_path;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    @Override
    public String toString() {
        return category_name;
    }
}