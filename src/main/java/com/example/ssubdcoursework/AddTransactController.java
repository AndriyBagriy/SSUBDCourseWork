package com.example.ssubdcoursework;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javax.script.ScriptException;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class AddTransactController implements Initializable {
    @FXML
    public Label amountLB;
    public ComboBox<Accounts> accountCB;
    public ComboBox<Categories> categoriesCB;
    private Controller controller;
    private final DBHelperSLite dbHelperSLite = new DBHelperSLite();
    public void setMainController(Controller controller){
        this.controller = controller;
    }
    public boolean check;
    public void setSpending(boolean check) throws SQLException {
        this.check = check;
        if(check) categoriesCB.getItems().addAll(dbHelperSLite.getCategories(" WHERE type = 'output'"));
        else categoriesCB.getItems().addAll(dbHelperSLite.getCategories(" WHERE type = 'input'"));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            dbHelperSLite.connect();
            accountCB.getItems().addAll(dbHelperSLite.getAccounts(""));
            accountCB.getSelectionModel().select(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        accountCB.setCellFactory(param -> new AccountListCell());
        categoriesCB.setCellFactory(param -> new CategoriesListCell());
        categoriesCB.getStyleClass().add("time-periodCB");
        accountCB.setButtonCell(new ListCell<Accounts>() {
            @Override
            protected void updateItem(Accounts item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    Circle circle = new Circle(15);
                    circle.setStroke(Paint.valueOf("#01799a"));
                    circle.setStrokeWidth(1);
                    ResultSet shortFormRS, photoPathRS;
                    String short_form = "",photo_path = "";
                    try {
                        shortFormRS = dbHelperSLite.executeQuery("SELECT short_form FROM Currencies WHERE _id = "+ item.getCurrency_id());
                        while (shortFormRS.next()) short_form = shortFormRS.getString("short_form");
                        photoPathRS = dbHelperSLite.executeQuery("SELECT type_path FROM AccountType WHERE _id = "+ item.getAccount_type());
                        while (photoPathRS.next()) photo_path = photoPathRS.getString("type_path");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    Image image = new Image(getClass().getResourceAsStream(photo_path));
                    circle.setFill(new ImagePattern(image));
                    Label lblShortForm = new Label(short_form);
                    VBox vbox = new VBox(circle, lblShortForm);
                    vbox.setStyle("-fx-background-color: transparent");
                    vbox.setSpacing(10);
                    setGraphic(vbox);
                }
            }
        });
    }
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public void applyBtn(ActionEvent actionEvent) throws SQLException, IOException {
        DecimalFormat format = new DecimalFormat("#.##");
        double amount = Double.parseDouble(amountLB.getText());
        long millis=System.currentTimeMillis();
        double newAmount, oldAmount = 0;
        Date date = new Date(millis);
        if(amount != 0 && categoriesCB.getSelectionModel().getSelectedItem() != null) {
            String name = categoriesCB.getSelectionModel().getSelectedItem().getCategory_name();
            String queryTransact = "INSERT INTO Transactions(category_id, amount, date, account_id) VALUES (?, ?, ?, ?)";
            PreparedStatement stmTransact = dbHelperSLite.getConnection().prepareStatement(queryTransact);
            stmTransact.setInt(1, categoriesCB.getSelectionModel().getSelectedItem().get_id());
            stmTransact.setDouble(2, amount);
            stmTransact.setString(3, dateFormat.format(date));
            stmTransact.setInt(4, accountCB.getSelectionModel().getSelectedItem().get_id());
            stmTransact.executeUpdate();
            ResultSet resultSet = dbHelperSLite.executeQuery("SELECT money FROM Accounts WHERE _id = "+ accountCB.getSelectionModel().getSelectedItem().get_id());
            while (resultSet.next()) oldAmount = resultSet.getDouble("money");
            if(check) {controller.setTransactValue(amount, name); newAmount = oldAmount - amount;}
            else newAmount = oldAmount + amount;
            String queryAccounts = "UPDATE Accounts SET money = ? WHERE _id = ?;";
            PreparedStatement stmAccounts = dbHelperSLite.getConnection().prepareStatement(queryAccounts);
            stmAccounts.setDouble(1, Double.parseDouble(format.format(newAmount).replace(",", ".")));
            stmAccounts.setDouble(2, accountCB.getSelectionModel().getSelectedItem().get_id());
            stmAccounts.executeUpdate();
            System.out.println("amount = "+amount);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.close();
        }else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Увага");
            alert.setHeaderText(null);
            alert.setContentText("Не обрані/заповнені необхідні для створення транзакції поля!");
            alert.showAndWait();
        }
    }
    boolean checkPoint = false;
    //boolean zeroCheck = false;
    public void numButtonClick(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        Label lbl = amountLB;
        if(operationCheck) {lbl.setText("0"); operationCheck = false;}
        checkPoint= lbl.getText().contains(".");
        if (!button.getText().equals(",")) {
            if(!checkPoint){
                if (lbl.getText().equals("0")) lbl.setText(lbl.getText().replace("0", button.getText()));
                else lbl.setText(lbl.getText().replace(",", "") + button.getText());
            }
            else {
                String part = lbl.getText().split("\\.")[0];
                char[] part2 = lbl.getText().split("\\.")[1].toCharArray();

                for (int i = 0; i <= part2.length-1; i++){
                    if(part2[i] == '0' /*&& !zeroCheck*/) {
                        part2[i] = button.getText().toCharArray()[0];
                        break;
                    }
                }
                lbl.setText(part + "." + String.valueOf(part2));
            }
        }
        else {
            if(!checkPoint) {
                //System.out.println(lbl.getText().replace(",", "")+".00");
                checkPoint = true;
                lbl.setText(lbl.getText().replace(",", "") + ".00");
            }
        }
    }
    boolean operationCheck = false;
    String temp = "", symbol = "";

    public void calculBtnClick(ActionEvent actionEvent){
        Button button = (Button) actionEvent.getSource();
        Label lbl = amountLB;
        double num = Double.parseDouble(amountLB.getText());
        double exp = num;
        DecimalFormat format = new DecimalFormat("#.##");
        if (temp.equals("")) {
            temp += num;
        }else{
            double tempD = Double.parseDouble(temp);
            switch (symbol) {
                case "+" -> exp = tempD + num;
                case "-" -> exp = tempD - num;
                case "*" -> exp = tempD * num;
                case "/" ->{
                    if(num != 0) exp = tempD / num;
                    else{
                        lbl.setText("Error");
                        operationCheck = true;
                        temp = "";
                        return;
                    }
                }
            }
            temp = String.valueOf(exp);
            lbl.setText(format.format(exp).replace(",", "."));
        }

        switch (button.getText()) {
            case "+" -> {symbol = "+";operationCheck = true;}
            case "-" -> {symbol = "-";operationCheck = true;}
            case "✕" -> {symbol = "*";operationCheck = true;}
            case "÷" -> {symbol = "/";operationCheck = true;}
            case "=" -> {
                if (!temp.equals("")) {
                    temp = "";
                    operationCheck = false;
                }
            }
        }
    }
    public void numButtonClickDel() {
        Label lbl = amountLB;
        checkPoint = lbl.getText().contains(".");
        if(checkPoint){
            String part = lbl.getText().replace(",", "").split("\\.")[0];
            char[] part2 = lbl.getText().replace(",", "").split("\\.")[1].toCharArray();
            for (int i = part2.length-1; i >= 0; i--) {
                if (part2[i] != '0') {
                    part2[i]='0';
                    lbl.setText(part + "." + String.valueOf(part2));
                    break;
                }else if (i == 0 && part2[i] == '0'){
                    checkPoint=false;
                    lbl.setText(part);
                }
            }
        }else {
            if(lbl.getText().length()!=1){
                lbl.setText(lbl.getText().substring(0, lbl.getText().length()-1));
            }else {
                lbl.setText("0");
            }
        }
    }

    public void changeColorEnterL(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-background-color: #008db8;");
    }
    public void changeColourReleasedL(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-background-color: #008db8;");

        if(!button.isHover()){
            button.setStyle("-fx-background-color: #01799a;");
        }
    }
    public void changeColorDark(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-background-color: #01799a;");
    }
    public void changeColourReleasedD(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-background-color: #01799a;");
        if(!button.isHover()){
            button.setStyle("-fx-background-color: #008db8;");
        }
    }

    public void changeColourExtraDark(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-background-color: #006278;");
    }
}
