package com.example.ssubdcoursework;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountListCell extends ListCell<Accounts> {
    private final Label nameLabel = new Label();
    private final Label currencyLabel = new Label();
    private final DBHelperSLite dbHelperSLite = new DBHelperSLite();

    @Override
    protected void updateItem(Accounts item, boolean empty) {
        super.updateItem(item, empty);
        try {
            dbHelperSLite.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (empty || item == null) {
            setGraphic(null);
        } else {
            ResultSet shortFormRS, photoPathRS;
            String short_form = "", photo_path = "";

            try {
                shortFormRS = dbHelperSLite.executeQuery("SELECT short_form FROM Currencies WHERE _id = "+ item.getCurrency_id());
                while (shortFormRS.next()) short_form = shortFormRS.getString("short_form");
                photoPathRS = dbHelperSLite.executeQuery("SELECT type_path FROM AccountType WHERE _id = "+ item.getAccount_type());
                while (photoPathRS.next()) photo_path = photoPathRS.getString("type_path");

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            nameLabel.setText(item.getAccount_name());
            currencyLabel.setText(short_form);

            //photo_path = "/img/test.png";
            Circle circle = new Circle(15);
            circle.setStroke(Paint.valueOf("#01799a"));
            circle.setStrokeWidth(1);
            Image image = new Image(getClass().getResourceAsStream(photo_path));
            circle.setFill(new ImagePattern(image));
            //circle.setStyle("-fx-background-color: yellow");
            HBox hbox = new HBox(circle, new VBox(nameLabel, currencyLabel));
            hbox.setStyle("-fx-background-color: transparent");
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setSpacing(10);
            setGraphic(hbox);
        }
    }
}
