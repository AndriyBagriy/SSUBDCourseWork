package com.example.ssubdcoursework;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class AddAccountController implements Initializable {
    @FXML
    public Label amountLB;
    public ComboBox<Currencies> currenciesCB;
    public ComboBox<AccountsType> accountTypeCB;
    public TextField accountNameTF;
    private Controller controller;
    private final DBHelperSLite dbHelperSLite = new DBHelperSLite();
    public void setMainController(Controller controller){
        this.controller = controller;
    }

    public boolean check;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            dbHelperSLite.connect();
            currenciesCB.getItems().addAll(dbHelperSLite.getCurrencies(""));
            currenciesCB.getSelectionModel().select(0);
            accountTypeCB.getItems().addAll(dbHelperSLite.getAccountType());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        accountTypeCB.getStyleClass().add("time-periodCB");
        currenciesCB.setButtonCell(new ListCell<Currencies>() {
            @Override
            protected void updateItem(Currencies item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText("");
                } else {
                    ResultSet rsName = null;
                    ResultSet rsSymbol = null;
                    String short_form = "", symbol = "";
                    try {
                        rsName = dbHelperSLite.executeQuery("SELECT short_form FROM Currencies WHERE _id = "+ item.get_id());
                        while (rsName.next()) short_form = rsName.getString("short_form");

                        rsSymbol = dbHelperSLite.executeQuery("SELECT currency_symbol FROM Currencies WHERE _id = "+ item.get_id());
                        while (rsSymbol.next()) symbol = rsSymbol.getString("currency_symbol");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    Label lbl = new Label(short_form+" ("+symbol+")");
                    Font font = Font.font("SansSerif", 23);
                    lbl.setFont(font);

                    StackPane stackPane = new StackPane(lbl);
                    stackPane.setStyle("-fx-background-color: transparent");
                    setGraphic(stackPane);
                }
            }
        });
    }

    public void applyBtn(ActionEvent actionEvent) throws SQLException {
        double amount = Double.parseDouble(amountLB.getText());
        String accountName = accountNameTF.getText();
        if(!accountName.equals("") && currenciesCB.getSelectionModel().getSelectedItem() != null &&
                accountTypeCB.getSelectionModel().getSelectedItem() != null) {
            String query = "INSERT INTO Accounts(account_name, account_type, money, currency_id) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = dbHelperSLite.getConnection().prepareStatement(query);
            statement.setString(1, accountName);
            statement.setInt(2, accountTypeCB.getSelectionModel().getSelectedItem().get_id());
            statement.setDouble(3, amount);
            statement.setInt(4, currenciesCB.getSelectionModel().getSelectedItem().get_id());
            statement.executeUpdate();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    boolean checkPoint = false;
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
                    if(part2[i] == '0') {
                        part2[i] = button.getText().toCharArray()[0];
                        break;
                    }
                }
                lbl.setText(part + "." + String.valueOf(part2));
            }
        }
        else {
            if(!checkPoint) {
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