package com.example.ssubdcoursework;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class Application extends javafx.application.Application {
    private final DBHelperSLite dbHelperSLite = new DBHelperSLite();
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("view4.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add("style.css");
        stage.setTitle("Курсова робота");
        stage.setOnHidden(windowEvent -> {
            try {
                dbHelperSLite.disconnect();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        stage.setScene(scene);
        stage.setMinWidth(1300);
        stage.setMinHeight(770);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}