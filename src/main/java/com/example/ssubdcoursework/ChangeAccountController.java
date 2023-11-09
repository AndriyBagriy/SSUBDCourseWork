package com.example.ssubdcoursework;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChangeAccountController implements Initializable {
    public Button changeBtn, delBtn;
    public TextField nameTF;
    private Controller controller;
    private int id;
    private final DBHelperSLite dbHelperSLite = new DBHelperSLite();
    public void setMainController(Controller controller){
        this.controller = controller;
    }

    public void setIdAccounts(int id) {
        this.id = id;
        System.out.println("id = " + this.id);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        changeBtn.getStyleClass().add("account-btn");
        delBtn.getStyleClass().add("time-account-btn");
    }

    public void update(ActionEvent actionEvent) throws SQLException {
        dbHelperSLite.connect();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText("Невалідне введення. Введіть від 4 до 16 стандартних символів.");
        String regex = "^[a-zA-Zа-яА-Я0-9 ]{4,21}$";;
        String temp = nameTF.getText();
        if(temp.matches(regex)){
            dbHelperSLite.executeUpdate("UPDATE Accounts SET account_name = '"+temp+"' WHERE _id = "+ id);
            dbHelperSLite.disconnect();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.close();
        }else{
            alert.showAndWait();
        }
    }
    public void delete(ActionEvent actionEvent) throws SQLException {
        dbHelperSLite.connect();
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Помилка");
        alert.setHeaderText("Ви впевнені що бажаете видалити рахунок?");
        alert.setContentText("Разом за рахунком будуть видалені всі транзакції, які були проведені з цього рахунку");
        ButtonType yesBtn = new ButtonType("Так");
        ButtonType noBtn = new ButtonType("Ні");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getButtonTypes().setAll(yesBtn, noBtn);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == yesBtn){
            dbHelperSLite.executeUpdate("DELETE FROM Transaction WHERE account_id = "+ id);
            dbHelperSLite.executeUpdate("DELETE FROM Accounts WHERE _id = "+ id);
            dbHelperSLite.disconnect();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.close();
        }
    }
}
