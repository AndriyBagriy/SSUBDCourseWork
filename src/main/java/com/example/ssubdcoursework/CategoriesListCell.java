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

public class CategoriesListCell extends ListCell<Categories>{
    private final Label nameLabel = new Label();
    private final DBHelperSLite dbHelperSLite = new DBHelperSLite();

    @Override
    protected void updateItem(Categories item, boolean empty) {
        super.updateItem(item, empty);
        try {
            dbHelperSLite.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (empty || item == null) {
            setGraphic(null);
        } else {
            nameLabel.setText(item.getCategory_name());
            //imageView.setImage(new Image() );
            Circle circle = new Circle(15);
            circle.setStroke(Paint.valueOf("#01799a"));
            circle.setStrokeWidth(1);
            String photo_path = item.getImg_path();
            Image image = new Image(getClass().getResourceAsStream(photo_path));
            circle.setFill(new ImagePattern(image));

            HBox hbox = new HBox(circle, nameLabel);
            hbox.setStyle("-fx-background-color: transparent");
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setSpacing(10);
            setGraphic(hbox);
        }
    }
}
