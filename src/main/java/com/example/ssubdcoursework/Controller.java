package com.example.ssubdcoursework;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.text.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static java.lang.Long.MAX_VALUE;


public class Controller implements Initializable {

    public Button nextBtn;
    private final DBHelperSLite dbHelperSLite = new DBHelperSLite();
    private final DatabaseHelperPG dbHelperPG = new DatabaseHelperPG();

    public VBox vBoxTransactions, vBoxAccounts, chartVB, currenciesPBs, accountsPBs;
    public Tab tab1, tab2, tab3;
    public TabPane tabPane1;
    private static final String API_KEY = "3df31ec23c48d6d1b9f3c3d2";
    //private static final String API_KEY = "2ff05a3b9d7beeb56a0770c2";
    public Button addExpenses, addProfit;
    public PieChart pieChart;
    public Circle circleChart;
    public ListView chartLegList;
    public FlowPane legendsFlowP;
    public SplitPane splitConverter;
    public Label incomeLbl, amountFirst, currencyFirst, amountSecond, currencySecond, minusLbl, plusLbl, expensesLbl;;
    public ComboBox<Currencies> currenciesListFirst, currenciesListSecond;
    public ComboBox<String> timePeriodsCB;
    public ProgressBar incomePB, expensesPB;
    private Text centerText;
    public BarChart<String, Number> barChart;
    public XYChart.Series<String, Number> barChartSeriesInput = new XYChart.Series<>();
    public XYChart.Series<String, Number> barChartSeriesOutput = new XYChart.Series<>();
    boolean checkPoint = false;

    private static final String[] colors = {
            "lightgray", "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF", "#000000",
            "#FFFFFF", "#800000", "#008000", "#000080", "#808000", "#800080", "#008080", "#C00000",
            "#00C000", "#0000C0", "#C0C000", "#C000C0", "#00C0C0", "#C0C0C0", "#400000", "#004000",
            "#000040","#404000", "#400040", "#004040", "#404040", };
    private static  final String[] timePeriodArray = {"Поточний рік", "Поточний місяць", "Поточний тиждень"};
    Locale ukLocale = new Locale("uk");
    String currentPeriod = "";
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            dbHelperSLite.connect();
            dbHelperPG.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        tabPane1.getStyleClass().add("tab-Pane1");
        pieChart.getStyleClass().add("pie-chart");
        circleChart.getStyleClass().add("circle-chart");
        splitConverter.getStyleClass().add("split-converter");
        currenciesListFirst.getStyleClass().add("time-periodCB");
        currenciesListSecond.getStyleClass().add("time-periodCB");
        addExpenses.setShape(new Circle(150));
        addProfit.setShape(new Circle(150));

        ArrayList<Tab> tabs = new ArrayList<>();
        tabs.add(tab1);
        tabs.add(tab2);
        tabs.add(tab3);
        String[] tabNames = {"Головна", "Статистика", "Конвертер"};
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            Label l = new Label(tabNames[i]);
            l.getStyleClass().add("tabLabel");
            StackPane stp = new StackPane(new Group(l));
            tab.setGraphic(stp);
        }
        tabPane1.setTabMinHeight(227);
        tabPane1.setTabMinWidth(30);

        pieChart.setClockwise(false);
        pieChart.setStartAngle(90);
        try {
            addNewData(100, "Транзакції відсутні", colors[0]);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
        timePeriodsCB.getItems().addAll(timePeriodArray);
        timePeriodsCB.getStyleClass().add("time-periodCB");
        timePeriodsCB.valueProperty().addListener((observableValue, item, t1) -> {
           if(timePeriodsCB.getSelectionModel().getSelectedIndex()>=0) {
                if(!observableValue.getValue().contains(".")) {
                    if(timePeriodsCB.getItems().size()!=3){
                        timePeriodsCB.getItems().remove(0);
                    }
                    currentPeriod = observableValue.getValue();
                    dateTemp = null;
                    try {

                        createProgressBars("");
                        periodInit();
                        initBarChart();
                        initChart();
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("temp: " + observableValue.getValue());
                }
            }
        });
        timePeriodsCB.getSelectionModel().select(0);
        currentPeriod = timePeriodsCB.getSelectionModel().getSelectedItem();

        try {
            postgreSync();
            fillAccountsList();
            fillTransactionsList();
            fillProgressBars("");
            initBarChart();
            initChart();
            updateComboboxes();
        } catch (SQLException | ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        currencyFirst.setText(currenciesListFirst.getSelectionModel().getSelectedItem().getCurrency_symbol());
        currencySecond.setText(currenciesListSecond.getSelectionModel().getSelectedItem().getCurrency_symbol());
        currenciesListFirst.valueProperty().addListener((observableValue, currencies, t1) -> {
            currencyFirst.setText(t1.getCurrency_symbol());
            try {
                updateExchangeRate();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });

        currenciesListSecond.valueProperty().addListener((observableValue, currencies, t1) -> {
            currencySecond.setText(t1.getCurrency_symbol());
            try {
                updateExchangeRate();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        StringProperty textPropertyFirst = amountFirst.textProperty();
        StringProperty textPropertySecond = amountSecond.textProperty();

        textPropertyFirst.addListener((observableValue, s, t1) -> {
            try {
                if(currencyLblFirstCheck)
                    updateExchangeRate();
                toFormatStringFirst();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        textPropertySecond.addListener((observableValue, s, t1) -> {
            try {
                if(!currencyLblFirstCheck)
                    updateExchangeRate();
                toFormatStringSecond();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void postgreSync() throws SQLException, ParseException {
        Date dataPG = null, dataSQLite = null;
        ResultSet dataPGRS = dbHelperPG.executeQuery("SELECT* FROM lastupdate");
        ResultSet dataSqLiteRS = dbHelperSLite.executeQuery("SELECT* FROM LastUpdate");
        while (dataPGRS.next()) dataPG = format.parse(dataPGRS.getString ("updatedate"));
        while (dataSqLiteRS.next()) dataSQLite = format.parse(dataSqLiteRS.getString ("updateDate"));
        if(dataPG == null || dataSQLite ==null) {
            if (dataPG == null && dataSQLite == null) {
                replaceSQLitetoPG();
            } else if (dataPG == null && dataSQLite != null) {
                replacePGtoSQLite();
            } else {
                replaceSQLitetoPG();
            }
        }else{
            int temp = dataSQLite.compareTo(dataPG);
            switch (temp){
                case 1 -> replacePGtoSQLite();
                case -1 -> replaceSQLitetoPG();
            }
        }
    }
    DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public void replacePGtoSQLite() throws SQLException, ParseException {
        String[] tables = {"accounts", "transactions"};
        for (String table : tables){dbHelperPG.executeUpdate("DELETE FROM "+table);}
        ObservableList<Accounts> accounts = dbHelperSLite.getAccounts("");
        ObservableList<Transactions> transactions = dbHelperSLite.getTransactions("");
        for(Accounts account : accounts){
            String query = "INSERT INTO Accounts(id, account_name, account_type, account_money, currency_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = dbHelperPG.getConnection().prepareStatement(query);
            statement.setInt(1, account.get_id());
            statement.setString(2, account.getAccount_name());
            statement.setInt(3, account.getAccount_type());
            statement.setDouble(4, account.getMoney());
            statement.setInt(5, account.getCurrency_id());
            statement.executeUpdate();
        }
        for(Transactions transact : transactions){
            Date date = sqlDateFormat.parse(sqlDateFormat.format(transact.getDate()));
            String dateString = sqlDateFormat.format(date);
            java.sql.Date sqlDate = java.sql.Date.valueOf(dateString);
            String query = "INSERT INTO Transactions(id, category_id, amount, transact_date, account_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = dbHelperPG.getConnection().prepareStatement(query);
            statement.setInt(1, transact.get_id());
            statement.setInt(2, transact.getCategory_id());
            statement.setDouble(3, transact.getAmount());
            statement.setDate(4, sqlDate);
            statement.setInt(5, transact.getAccount_id());
            statement.executeUpdate();
        }
        updateDatePGLite();
    }

    public void replaceSQLitetoPG() throws SQLException, ParseException {
        ResultSet dataCatRs = dbHelperSLite.executeQuery("SELECT COUNT(*) AS count FROM Categories");
        ResultSet dataTypeRs = dbHelperSLite.executeQuery("SELECT COUNT(*) AS count FROM AccountType");
        ResultSet dataCurRs = dbHelperSLite.executeQuery("SELECT COUNT(*) AS count FROM Currencies");
        int countCat = 0, countType = 0, countCur = 0;
        while (dataCatRs.next()) countCat = dataCatRs.getInt("count");
        while (dataTypeRs.next()) countType = dataTypeRs.getInt("count");
        while (dataCurRs.next()) countCur = dataCurRs.getInt("count");

        if(countCat == 0 || countType == 0 || countCur == 0){
            fillTables();
        }


        String[] tables = {"accounts", "transactions"};
        for (String table : tables){
            dbHelperSLite.executeUpdate("DELETE FROM "+table);
        }

        ObservableList<Accounts> accounts = dbHelperPG.getAccounts();
        ObservableList<Transactions> transactions = dbHelperPG.getTransactions();

        for(Accounts account : accounts){
            String query = "INSERT INTO Accounts(_id, account_name, account_type, money, currency_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = dbHelperSLite.getConnection().prepareStatement(query);
            statement.setInt(1, account.get_id());
            statement.setString(2, account.getAccount_name());
            statement.setInt(3, account.getAccount_type());
            statement.setDouble(4, account.getMoney());
            statement.setInt(5, account.getCurrency_id());
            statement.executeUpdate();
        }

        for(Transactions transact : transactions){
            String query = "INSERT INTO Transactions(_id, category_id, amount, date, account_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = dbHelperSLite.getConnection().prepareStatement(query);
            statement.setInt(1, transact.get_id());
            statement.setInt(2, transact.getCategory_id());
            statement.setDouble(3, transact.getAmount());
            statement.setString(4, sqlDateFormat.format(transact.getDate()));
            statement.setInt(5, transact.getAccount_id());
            statement.executeUpdate();
        }
        updateDateSQLite();
    }

    public void fillTables() throws SQLException {
        String[] tables = {"categories", "account_type", "currencies"};
        for (String table : tables){
            dbHelperSLite.executeUpdate("DELETE FROM "+table);
        }

        ObservableList<Categories> categories = dbHelperPG.getCategories();
        ObservableList<AccountsType> accountsTypes = dbHelperPG.getAccountType();
        ObservableList<Currencies> currencies = dbHelperPG.getCurrencies();

        for(Categories category : categories){
            String query = "INSERT INTO Categories(_id, category_name, type, img_path) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = dbHelperSLite.getConnection().prepareStatement(query);
            statement.setInt(1, category.get_id());
            statement.setString(2, category.getCategory_name());
            statement.setString(3, category.getType());
            statement.setString(4, category.getImg_path());
            statement.executeUpdate();
        }

        for(AccountsType accountsType : accountsTypes){
            String query = "INSERT INTO AccountsType(_id, type_name, type_path) VALUES (?, ?, ?)";
            PreparedStatement statement = dbHelperSLite.getConnection().prepareStatement(query);
            statement.setInt(1, accountsType.get_id());
            statement.setString(2, accountsType.getType_name());
            statement.setString(3, accountsType.getType_path());
            statement.executeUpdate();
        }
        for(Currencies currency : currencies){
            String query = "INSERT INTO Currencies(_id, country, currency_name, short_form, currency_symbol) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = dbHelperSLite.getConnection().prepareStatement(query);
            statement.setInt(1, currency.get_id());
            statement.setString(2, currency.getCountry());
            statement.setString(3, currency.getCurrency_name());
            statement.setString(4, currency.getShort_form());
            statement.setString(5, currency.getCurrency_symbol());
            statement.executeUpdate();
        }
    }

    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public void updateDateSQLite() throws SQLException{
        ResultSet dataPGRS = dbHelperSLite.executeQuery("SELECT COUNT(*) AS count FROM LastUpdate");
        int count = 0;
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        while (dataPGRS.next()) count = dataPGRS.getInt("count");
        if(count == 0){
            String query = "INSERT INTO LastUpdate(_id, updateDate) VALUES (?, ?)";
            PreparedStatement statement = dbHelperSLite.getConnection().prepareStatement(query);
            statement.setInt(1, 1);
            statement.setString(2, format.format(date));
            statement.executeUpdate();
        }else{
            String queryAccounts = "UPDATE LastUpdate SET updateDate = ? WHERE _id = 1;";
            PreparedStatement statement = dbHelperSLite.getConnection().prepareStatement(queryAccounts);
            statement.setString(1, format.format(date));
            statement.executeUpdate();
        }
    }

    public void updateDatePGLite() throws SQLException, ParseException {
        ResultSet dataPGRS = dbHelperPG.executeQuery("SELECT COUNT(*) AS count FROM lastupdate");
        int count = 0;
        Date date = format.parse(format.format(new Date(System.currentTimeMillis())));
        String dateString = format.format(date);
        java.sql.Date sqlDate = java.sql.Date.valueOf(dateString);

        while (dataPGRS.next()) count = dataPGRS.getInt("count");
        if(count == 0){
            String query = "INSERT INTO lastupdate(id, updateDate) VALUES (?, ?)";
            PreparedStatement statement = dbHelperPG.getConnection().prepareStatement(query);
            statement.setInt(1, 1);
            statement.setDate(2, sqlDate);
            statement.executeUpdate();
        }else{
            String queryAccounts = "UPDATE lastupdate SET updateDate = ? WHERE id = 1;";
            PreparedStatement statement = dbHelperPG.getConnection().prepareStatement(queryAccounts);
            statement.setDate(1, sqlDate);
            statement.executeUpdate();
        }
    }

    public void initBarChart() throws SQLException, IOException {
        barChartSeriesInput.getData().clear();
        barChartSeriesOutput.getData().clear();
        barChart.getData().clear();
        Calendar calendar = Calendar.getInstance();

        LocalDate startOfWeek, endOfWeek;
        startOfWeek = dateTemp;
        while (startOfWeek.getDayOfWeek() != DayOfWeek.MONDAY) {
            startOfWeek = startOfWeek.minusDays(1);
        }
        endOfWeek = startOfWeek.plusDays(6);
        String query = "";
        switch (currentPeriod) {
            case "Поточний рік" -> {
                query = " WHERE strftime('%Y', date) == '"+dateTemp.getYear()+"'";
                ObservableList<Transactions> transactions = dbHelperSLite.getTransactions(query);
                for(int i = 1; i<=12; i++){
                    String monthName = Month.of(i).getDisplayName(TextStyle.FULL_STANDALONE, ukLocale);
                    monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

                    double sumInput = 0.0, sumOutput = 0.0;
                    for(Transactions transaction : transactions){
                        String categoryTypeStr = "";
                        ResultSet categoryTypeRS = dbHelperSLite.executeQuery("SELECT type FROM Categories WHERE _id = " + transaction.getCategory_id());
                        while (categoryTypeRS.next()) categoryTypeStr = categoryTypeRS.getString("type");
                        Date date = transaction.getDate();
                        calendar.setTime(date);
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH)+1;
                        double amount = getAmountCurrencies(transaction);
                        if(year == dateTemp.getYear() && month == i){
                            if(categoryTypeStr.equals("input"))sumInput += amount;
                            else sumOutput += amount;
                        }
                    }
                    barChartSeriesInput.getData().add(new XYChart.Data<>(monthName, sumInput));
                    barChartSeriesOutput.getData().add(new XYChart.Data<>(monthName, -sumOutput));
                }
            }
            case "Поточний місяць" -> {
                query = " WHERE strftime('%Y', date) == '"+dateTemp.getYear()+"' AND strftime('%m', date) == '"+dateTemp.getMonthValue()+"'";
                ObservableList<Transactions> transactions = dbHelperSLite.getTransactions(query);
                for(int i = 1; i<=dateTemp.lengthOfMonth(); i++){
                    String monthBar = String.format("%02d", dateTemp.getMonthValue());
                    String dayBar = String.format("%02d", i);
                    String dayName = monthBar + "." + dayBar;
                    double sumInput = 0.0, sumOutput = 0.0;
                    for(Transactions transaction : transactions){
                        String categoryTypeStr = "";
                        ResultSet categoryTypeRS = dbHelperSLite.executeQuery("SELECT type FROM Categories WHERE _id = " + transaction.getCategory_id());
                        while (categoryTypeRS.next()) categoryTypeStr = categoryTypeRS.getString("type");

                        Date date = transaction.getDate();
                        calendar.setTime(date);
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH)+1;
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        double amount = getAmountCurrencies(transaction);
                        if(year == dateTemp.getYear() && month == dateTemp.getMonthValue() && day == i){
                            if(categoryTypeStr.equals("input"))sumInput += amount;
                            else sumOutput += amount;
                        }
                    }
                    barChartSeriesInput.getData().add(new XYChart.Data<>(dayName, sumInput));
                    barChartSeriesOutput.getData().add(new XYChart.Data<>(dayName, -sumOutput));
                }
            }
            case "Поточний тиждень" -> {
                query =" WHERE date BETWEEN '"+dateFormat.format(startOfWeek)+"' AND '"+dateFormat.format(endOfWeek)+"'";
                ObservableList<Transactions> transactions = dbHelperSLite.getTransactions(query);
                LocalDate tempLD = startOfWeek;
                for(int i = 1; i<=7; i++){
                    String dayOfWeek = tempLD.getDayOfWeek().getDisplayName(TextStyle.FULL, ukLocale);
                    dayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1);
                    double sumInput = 0.0, sumOutput = 0.0;
                    for(Transactions transaction : transactions){
                        String categoryTypeStr = "";
                        ResultSet categoryTypeRS = dbHelperSLite.executeQuery("SELECT type FROM Categories WHERE _id = " + transaction.getCategory_id());
                        while (categoryTypeRS.next()) categoryTypeStr = categoryTypeRS.getString("type");
                        Date date = transaction.getDate();
                        calendar.setTime(date);
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH)+1;
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        double amount = getAmountCurrencies(transaction);
                        if(year == tempLD.getYear() && month == tempLD.getMonthValue() && day == tempLD.getDayOfMonth()){
                            if(categoryTypeStr.equals("input"))sumInput += amount;
                            else sumOutput += amount;
                        }
                    }
                    barChartSeriesInput.getData().add(new XYChart.Data<>(dayOfWeek, sumInput));
                    barChartSeriesOutput.getData().add(new XYChart.Data<>(dayOfWeek, -sumOutput));
                    tempLD = tempLD.plusDays(1);
                }

            }
        }
        barChart.getData().addAll(barChartSeriesInput, barChartSeriesOutput);
    }


    public double getAmountCurrencies(Transactions transaction) throws SQLException, IOException {
        int currencyId = 0;
        double amount = transaction.getAmount();
        ResultSet currenciesRS = dbHelperSLite.executeQuery("SELECT currency_id FROM Accounts WHERE _id = " + transaction.getAccount_id());
        while (currenciesRS.next()) currencyId = currenciesRS.getInt(1);
        if(currencyId != 146){
            String shortFormSTR = "";
            String shortFormQuery = "SELECT short_form FROM Currencies WHERE _id = " +currencyId;
            ResultSet shortFormRS = dbHelperSLite.executeQuery(shortFormQuery);
            while (shortFormRS.next()) shortFormSTR = shortFormRS.getString(1);
            amount = getExchangeRate(shortFormSTR, "UAH", amount);
        }
        return amount;
    }

    private ArrayList<Double> getSumOfMoney(ObservableList<Accounts> accounts, String query) throws SQLException, IOException {
        ArrayList<Double> sumOfMoneyList = new ArrayList<>();
        for(Accounts account : accounts){
            String balanceQuery = "SELECT SUM(CASE WHEN C.type = 'input' THEN T.amount WHEN C.type = 'output' " +
                    "THEN -T.amount ELSE 0 END) AS balance FROM Transactions T JOIN Categories C on C._id = T.category_id " +
                    "WHERE T.account_id = "+account.get_id()+query;
            double balance = 0.0;

            ResultSet balances = dbHelperSLite.executeQuery(balanceQuery);
            while (balances.next()) balance = balances.getDouble(1);
            if(account.getCurrency_id() != 146){
                String shortFormSTR = "";
                String shortFormQuery = "SELECT short_form FROM Currencies WHERE _id = " +account.getCurrency_id();
                ResultSet shortFormRS = dbHelperSLite.executeQuery(shortFormQuery);
                while (shortFormRS.next()) shortFormSTR = shortFormRS.getString(1);
                balance = getExchangeRate(shortFormSTR, "UAH", balance);
            }
            sumOfMoneyList.add(balance);
        }
        return sumOfMoneyList;
    }

    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate dateTemp;
    public void changePeriod(ActionEvent actionEvent) throws SQLException, IOException {
        if(timePeriodsCB.getItems().size()!=3){
            String temp = String.valueOf(dateTemp);
            timePeriodsCB.getItems().remove(0);
            timePeriodsCB.getSelectionModel().select(temp);
        }
        LocalDate localDate = LocalDate.now();
        if(dateTemp == null) dateTemp = localDate;
        Button button = (Button) actionEvent.getSource();
        LocalDate startOfWeek, endOfWeek;
        startOfWeek = dateTemp;
        while (startOfWeek.getDayOfWeek() != DayOfWeek.MONDAY) {
            startOfWeek = startOfWeek.minusDays(1);
        }
        endOfWeek = startOfWeek.plusDays(6);
        switch (currentPeriod) {
            case "Поточний рік" -> dateTemp = button.getId().equals("nextBtn") ? dateTemp.plusYears(1) : dateTemp.minusYears(1);
            case "Поточний місяць" -> dateTemp = button.getId().equals("nextBtn") ? dateTemp.plusMonths(1) : dateTemp.minusMonths(1);
            case "Поточний тиждень" -> {
                if(button.getId().equals("nextBtn")){
                    startOfWeek = endOfWeek.plusDays(1);
                    endOfWeek = startOfWeek.plusDays(6);
                    dateTemp = dateTemp.plusWeeks(1);
                } else {
                    endOfWeek = startOfWeek.minusDays(1);
                    startOfWeek = endOfWeek.minusDays(6);
                    dateTemp = dateTemp.minusWeeks(1);
                }
            }           
        }
        if(currentPeriod.equals("Поточний тиждень")){
            String elemFirst = dateFormat.format(startOfWeek).replace("-", ".");
            String elemSecond = dateFormat.format(endOfWeek).replace("-", ".");
            timePeriodsCB.getItems().add(0, elemFirst+" - "+elemSecond);
            timePeriodsCB.getSelectionModel().select(0);
        }else {
            String elem = dateFormat.format(dateTemp).replace("-", ".");
            timePeriodsCB.getItems().add(0, elem);
            timePeriodsCB.setValue(elem);
        }
        periodInit();
        initBarChart();
    }
    public void periodInit() throws SQLException, IOException {
        LocalDate startOfWeek, endOfWeek;
        startOfWeek = dateTemp;
        while (startOfWeek.getDayOfWeek() != DayOfWeek.MONDAY) {
            startOfWeek = startOfWeek.minusDays(1);
        }
        endOfWeek = startOfWeek.plusDays(6);

        String[] periodArr = dateFormat.format(dateTemp).split("-");
        int month = Integer.parseInt(periodArr[1]);
        int day = Integer.parseInt(periodArr[2]);

        switch (currentPeriod) {
            case "Поточний рік" -> {
                createProgressBars(" AND strftime('%Y', T.date) >= '"+periodArr[0]+"'");
                fillProgressBars(" AND strftime('%Y', T.date) = '"+periodArr[0]+"'");
            }
            case "Поточний місяць" -> {
                createProgressBars(" AND strftime('%Y', T.date) >= '"+periodArr[0]+"' AND strftime('%m', T.date) >= '"+month+"'");
                fillProgressBars(" AND strftime('%Y', T.date) = '"+periodArr[0]+"' AND strftime('%m', T.date) = '"+periodArr[1]+"'");
            }
            case "Поточний тиждень" -> {
                createProgressBars(" AND T.date >= '"+dateFormat.format(startOfWeek)+"'");
                fillProgressBars(" AND T.date BETWEEN '"+dateFormat.format(startOfWeek)+"' AND '"+dateFormat.format(endOfWeek)+"'");
            }
        }
    }



    public void fillProgressBars(String addQuery) throws SQLException, IOException {
        String mainQueryExpenses = "SELECT T.amount, C2.short_form FROM Transactions T JOIN Categories C ON T.category_id = C._id INNER JOIN Accounts A on T.account_id = A._id INNER JOIN Currencies C2 on A.currency_id = C2._id WHERE C.type = 'output'";
        String mainQueryIncome = "SELECT T.amount, C2.short_form FROM Transactions T JOIN Categories C ON T.category_id = C._id INNER JOIN Accounts A on T.account_id = A._id INNER JOIN Currencies C2 on A.currency_id = C2._id WHERE C.type = 'input'";
        double sumExpenses = getSumOfTransaction(mainQueryExpenses + addQuery);
        double sumIncome = getSumOfTransaction(mainQueryIncome + addQuery);

        double incomeProgress = (sumIncome == 0.0) ? 0.0F : (sumIncome / Math.max(sumExpenses, sumIncome));
        double expensesProgress = (sumExpenses == 0.0) ? 0.0F : (sumExpenses / Math.max(sumIncome, sumExpenses));
        incomePB.setProgress(incomeProgress);
        expensesPB.setProgress(expensesProgress);
    }

    public void createProgressBars(String query) throws SQLException, IOException {
        LocalDate localDate = LocalDate.now();
        if(dateTemp == null) dateTemp = localDate;
        accountsPBs.getChildren().clear();
        currenciesPBs.getChildren().clear();
        ObservableList<Accounts> accounts = dbHelperSLite.getAccounts("");
        Font font = Font.font("SansSerif Bold", FontWeight.BOLD,17);
        Font font1 = Font.font("SansSerif Bold", FontWeight.MEDIUM,15);
        int i = 3;
        String queryNOTcur = "";
        String year = String.valueOf(dateTemp.getYear());
        String month = String.valueOf(dateTemp.getMonth().getValue());
        String day = String.valueOf(dateTemp.getDayOfMonth());

        LocalDate startOfWeek = dateTemp, endOfWeek;
        while (startOfWeek.getDayOfWeek() != DayOfWeek.MONDAY) {
            startOfWeek = startOfWeek.minusDays(1);
        }
        endOfWeek = startOfWeek.plusDays(6);

        switch (currentPeriod) {
            case "Поточний рік" -> {
                queryNOTcur =" AND NOT (strftime('%Y', T.date) == '"+year+"')";
            }
            case "Поточний місяць" -> {
                queryNOTcur = " AND NOT (strftime('%Y', T.date) == '"+year+"' AND strftime('%m', T.date) == '"+month+"')";
            }
            case "Поточний тиждень" -> {
                queryNOTcur = " AND NOT (T.date BETWEEN '"+dateFormat.format(startOfWeek)+"' AND '"+dateFormat.format(endOfWeek)+"')";
            }
        }

        double money = 0.0;
        List<Double> transactSum = getSumOfMoney(accounts, query+queryNOTcur);
        List<double[]> dataCurrencies = new ArrayList<>();
        for (int y = 0; y < transactSum.size(); y++) {
            double accMoney = accounts.get(y).getMoney();
            if(accounts.get(y).getCurrency_id() != 146){
                String shortFormSTR = "";
                String shortFormQuery = "SELECT short_form FROM Currencies WHERE _id = " +accounts.get(y).getCurrency_id();
                ResultSet shortFormRS = dbHelperSLite.executeQuery(shortFormQuery);
                while (shortFormRS.next()) shortFormSTR = shortFormRS.getString(1);
                accMoney = getExchangeRate(shortFormSTR, "UAH", accMoney);
            }

            double temp = accMoney + transactSum.get(y);
            dataCurrencies.add(new double[]{temp, accounts.get(y).getCurrency_id()});
            money += temp;
        }

        Map<Integer, Double> result = new HashMap<>();

        for (double[] entry : dataCurrencies) {
            double a = entry[0];
            int b = (int) entry[1];
            result.put(b, result.getOrDefault(b, 0.0) + a);
        }
        double sumMoneyCurrency = 0.0;
        for (Map.Entry<Integer, Double> entry : result.entrySet()) {
            sumMoneyCurrency+=entry.getValue();
        }

        for (Map.Entry<Integer, Double> entry : result.entrySet()) {
            if(i == colors.length-1) i = 3;
            VBox vbox = new VBox();
            GridPane grid = new GridPane();
            ProgressBar pb = new ProgressBar();
            pb.setPrefHeight(20);
            pb.setMaxWidth(MAX_VALUE);
            String color = colors[i];
            String shortForm = "";
            String tempStr = "";
            ResultSet short_formRS = dbHelperSLite.executeQuery("SELECT short_form FROM Currencies WHERE _id = "+entry.getKey());
            while (short_formRS.next()) shortForm = short_formRS.getString(1);
            if(entry.getKey() < 0) {tempStr = " (від'ємний)"; color = colors[1];}

            pb.setStyle("-fx-accent: "+color);
            Label nameLbl = new Label(shortForm+tempStr);
            Label moneyLbl = new Label(String.format("%.2f", entry.getValue()));
            nameLbl.setFont(font); nameLbl.setTextFill(Color.WHITE);
            moneyLbl.setFont(font1); moneyLbl.setTextFill(Color.WHITE);
            grid.add(nameLbl, 0, 0);
            grid.add(moneyLbl, 1, 0);
            pb.setProgress(((entry.getValue()*100)/sumMoneyCurrency)/100);
            //System.out.println("sumCur = " + sumMoneyCurrency);

            vbox.getChildren().addAll(grid,pb);
            VBox.setMargin(vbox, new Insets(0, 0, 10, 0));
            GridPane.setHgrow(moneyLbl, Priority.ALWAYS);
            GridPane.setHalignment(moneyLbl, HPos.RIGHT);
            currenciesPBs.getChildren().add(vbox);
            i++;
        }

        for(int y = 0; y < accounts.size(); y++){
            if(i == colors.length-1) i = 3;
            VBox vbox = new VBox();
            ProgressBar pb = new ProgressBar();
            GridPane grid = new GridPane();
            pb.setPrefHeight(20);
            pb.setMaxWidth(MAX_VALUE);
            String color = colors[i];
            pb.setStyle("-fx-accent: "+color);
            double balance;
            String tempStr = "";
            Label nameLbl, moneyLbl;
            double temp  = accounts.get(y).getMoney();
            if(accounts.get(y).getCurrency_id() != 146){
                String shortFormSTR = "";
                String shortFormQuery = "SELECT short_form FROM Currencies WHERE _id = " +accounts.get(y).getCurrency_id();
                ResultSet shortFormRS = dbHelperSLite.executeQuery(shortFormQuery);
                while (shortFormRS.next()) shortFormSTR = shortFormRS.getString(1);
                temp = getExchangeRate(shortFormSTR, "UAH", temp);
            }
            balance = temp + transactSum.get(y);
            if (balance < 0) {
                tempStr = " (від'ємний)";
                color = colors[1];
            }
            pb.setStyle("-fx-accent: " + color);
            nameLbl = new Label(accounts.get(y).getAccount_name() + tempStr);
            moneyLbl = new Label(String.format("%.2f", balance));

            nameLbl.setFont(font); nameLbl.setTextFill(Color.WHITE);
            moneyLbl.setFont(font1); moneyLbl.setTextFill(Color.WHITE);
            grid.add(nameLbl, 0, 0);
            grid.add(moneyLbl, 1, 0);
            pb.setProgress(((balance*100)/money)/100);
            vbox.getChildren().addAll(grid,pb);
            VBox.setMargin(vbox, new Insets(0, 0, 10, 0));
            GridPane.setHgrow(moneyLbl, Priority.ALWAYS);
            GridPane.setHalignment(moneyLbl, HPos.RIGHT);
            accountsPBs.getChildren().add(vbox);
            i++;
        }
    }
    public double getSumOfTransaction(String query) throws SQLException, IOException {
        double sum = 0.0;
        ResultSet sumRS = dbHelperSLite.executeQuery(query);

        while (sumRS.next()) {
            String shortFrom = sumRS.getString(2);
            double amountTmp = sumRS.getDouble(1);
            if(!shortFrom.equals("UAH")){
                amountTmp = getExchangeRate(shortFrom, "UAH", amountTmp);
            }
            sum += amountTmp;
        }
        return sum;
    }
    private void initChart() throws SQLException, IOException {
        String query = " JOIN Categories ON Transactions.category_id = Categories._id WHERE Categories.type = 'output'";
        ObservableList<Transactions> transactions = dbHelperSLite.getTransactions(query);
        String categoryName = "";
        for(Transactions transaction : transactions){
            ResultSet resultSet = dbHelperSLite.executeQuery("SELECT category_name FROM Categories WHERE _id = "+ transaction.getCategory_id());
            while (resultSet.next()) categoryName = resultSet.getString("category_name");
            setTransactValue(transaction.getAmount(), categoryName);
        }
        updateChartLbl();
    }

    public void updateChartLbl() throws SQLException, IOException {
        String expensesQuery = "SELECT T.amount, C2.short_form FROM Transactions T JOIN Categories C ON T.category_id = C._id INNER JOIN Accounts A on T.account_id = A._id INNER JOIN Currencies C2 on A.currency_id = C2._id WHERE C.type = 'output'";
        String incomeQuery = "SELECT T.amount, C2.short_form FROM Transactions T JOIN Categories C ON T.category_id = C._id INNER JOIN Accounts A on T.account_id = A._id INNER JOIN Currencies C2 on A.currency_id = C2._id WHERE C.type = 'input'";
        double sumExpenses = getSumOfTransaction(expensesQuery);
        double sumIncome =getSumOfTransaction(incomeQuery);
        incomeLbl.setText("+"+sumIncome+" грн.");
        expensesLbl.setText("-"+sumExpenses+" грн.");
    }

    public void addTransaction(ActionEvent actionEvent) throws IOException, SQLException {
        dbHelperSLite.disconnect();
        Button button = (Button) actionEvent.getSource();
        String butId = button.getId(), title = "";
        FXMLLoader loader = new FXMLLoader(getClass().getResource("additionTransact.fxml"));
        Parent root = loader.load();
        AddTransactController popupController = loader.getController();
        popupController.setMainController(this);
        popupController.setSpending(butId.equals("addExpenses"));
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        if(butId.equals("addExpenses")) title = "Додати нову витрату";
        else title = "Додати новий дохід";
        popupStage.setTitle(title);
        popupStage.setOnHidden(windowEvent -> {
            try {
                dbHelperSLite.disconnect();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        popupStage.setScene(new Scene(root));
        popupStage.showAndWait();
        dbHelperSLite.connect();
        fillAccountsList();
        fillProgressBars("");
        createProgressBars("");
        fillTransactionsList();
        updateChartLbl();
    }
    public void fillTransactionsList() throws SQLException {
        vBoxTransactions.getChildren().clear();
        ObservableList<Transactions> transactions = dbHelperSLite.getTransactions("");
        for(Transactions transact : transactions){
            HBox hBox = new HBox();
            GridPane grid = new GridPane();
            Circle circlePhoto = new Circle(20);
            circlePhoto.setStroke(Paint.valueOf("#01799a"));
            circlePhoto.setStrokeWidth(1);

            String categoryNameStr="", accountNameStr = "", categoryTypeStr = "", imgPathStr="", symbol="";
            String query = "SELECT c.category_name, c.type, c.img_path, a.account_name, C2.currency_symbol " +
                    "FROM Transactions t " +
                    "INNER JOIN Categories c ON t.category_id = c._id " +
                    "INNER JOIN Accounts a ON t.account_id = a._id " +
                    "INNER JOIN Currencies C2 on C2._id = a.currency_id " +
                    "WHERE t._id = " + transact.get_id();
            ResultSet resultSet = dbHelperSLite.executeQuery(query);

            while (resultSet.next()) {
                categoryNameStr = resultSet.getString("category_name");
                accountNameStr = resultSet.getString("account_name");
                categoryTypeStr = resultSet.getString("type");
                imgPathStr = resultSet.getString("img_path");
                symbol = resultSet.getString("currency_symbol");
            }
            Image image = new Image(getClass().getResourceAsStream(imgPathStr));
            circlePhoto.setFill(new ImagePattern(image));

            Font font = Font.font("SansSerif Bold", FontWeight.BOLD,14);
            Label accountNameLb = new Label(accountNameStr);
            accountNameLb.setFont(font); accountNameLb.setTextFill(Color.WHITE);
            Label amountLb;
            if(categoryTypeStr.equals("output")) {
                amountLb = new Label("-" + transact.getAmount()+symbol);
                amountLb.setTextFill(Paint.valueOf("#da0000"));
            }else{
                amountLb = new Label("+" + transact.getAmount()+symbol);
                amountLb.setTextFill(Paint.valueOf("#5aff59"));
            }
            amountLb.setFont(font);
            Label dateLb = new Label(transact.getDate().toString().split(" ")[0]);
            dateLb.setFont(font); dateLb.setTextFill(Color.WHITE);
            Label categoryNameLb = new Label(categoryNameStr);
            categoryNameLb.setFont(font); categoryNameLb.setTextFill(Color.WHITE);

            grid.add(categoryNameLb, 0, 0);
            grid.add(amountLb, 1, 0);
            grid.add(accountNameLb, 0, 1);
            grid.add(dateLb, 1, 1);

            hBox.getChildren().addAll(circlePhoto, grid);
            hBox.setAlignment(Pos.CENTER);
            HBox.setHgrow(grid, Priority.ALWAYS);
            GridPane.setHgrow(amountLb, Priority.ALWAYS);
            GridPane.setHalignment(amountLb, HPos.RIGHT);
            GridPane.setHalignment(dateLb, HPos.RIGHT);

            vBoxTransactions.getChildren().add(hBox);
            VBox.setMargin(hBox, new Insets(0, 0, 10 , 0));
            HBox.setMargin(grid, new Insets(0, 0, 0 , 5));
        }
    }
    public void fillAccountsList() throws SQLException {
        vBoxAccounts.getChildren().clear();
        ObservableList<Accounts> accounts = dbHelperSLite.getAccounts("");
        Font font = Font.font("SansSerif Bold", FontWeight.BOLD,14);
        for(Accounts account : accounts){
            Button btn = new Button();
            Controller tempController = this;
            EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        dbHelperSLite.disconnect();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("changeAccount.fxml"));
                        Parent root = loader.load();
                        ChangeAccountController popupController = loader.getController();
                        popupController.setMainController(tempController);
                        popupController.setIdAccounts(account.get_id());
                        Stage popupStage = new Stage();
                        popupStage.initModality(Modality.APPLICATION_MODAL);
                        popupStage.setTitle("Змінити рахунок");
                        popupStage.setOnHidden(windowEvent -> {
                            try {
                                dbHelperSLite.connect();
                                fillAccountsList();
                                initBarChart();
                                fillProgressBars("");
                                createProgressBars("");
                                dbHelperSLite.disconnect();
                            } catch (SQLException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        popupStage.setScene(new Scene(root));
                        popupStage.showAndWait();
                        dbHelperSLite.connect();
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            btn.setOnAction(eventHandler);
            VBox vBox = new VBox();
            HBox hBox = new HBox();
            String currency_symbol = "", photo_path = "";
            ResultSet symbolRS = dbHelperSLite.executeQuery("SELECT currency_symbol FROM Currencies WHERE _id = "+ account.getCurrency_id());
            while (symbolRS.next()) currency_symbol = symbolRS.getString("currency_symbol");
            ResultSet pathRS = dbHelperSLite.executeQuery("SELECT type_path FROM AccountType WHERE _id = "+ account.getAccount_type());
            while (pathRS.next()) photo_path = pathRS.getString("type_path");
            Rectangle rectPhoto = new Rectangle(50, 50);
            rectPhoto.setArcWidth(20);
            rectPhoto.setArcHeight(20);
            rectPhoto.setStroke(Paint.valueOf("#01799a"));
            rectPhoto.setStrokeWidth(1);
            Label name = new Label(), money = new Label();
            name.setFont(font); name.setTextFill(Color.WHITE);
            money.setFont(font); money.setTextFill(Color.WHITE);
            name.setText(account.getAccount_name());
            money.setText(account.getMoney()+currency_symbol);
            Image image = new Image(getClass().getResourceAsStream(photo_path));
            rectPhoto.setFill(new ImagePattern(image));
            vBox.setAlignment(Pos.CENTER_RIGHT);
            vBox.getChildren().addAll(name, money);
            hBox.getChildren().addAll(rectPhoto, vBox);
            HBox.setMargin(vBox, new Insets(0,0,0,0));
            btn.setGraphic(hBox);
            VBox.setMargin(btn, new Insets(0, 0, 10, 0));
            HBox.setHgrow(vBox, Priority.ALWAYS);
            btn.getStyleClass().add("account-btn");
            btn.setPrefHeight(50);
            btn.setMaxWidth(Double.MAX_VALUE);
            hBox.setPadding(new Insets(0,10,0,10));
            vBoxAccounts.getChildren().add(btn);
        }

    }
    public void addAccountBtn() throws SQLException, IOException {
        dbHelperSLite.disconnect();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("additionAccount.fxml"));
        Parent root = loader.load();
        AddAccountController popupController = loader.getController();
        popupController.setMainController(this);
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Додати новий рахунок");
        popupStage.setOnHidden(windowEvent -> {
            try {
                dbHelperSLite.disconnect();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        popupStage.setScene(new Scene(root));
        popupStage.showAndWait();
        dbHelperSLite.connect();
        fillAccountsList();
        initBarChart();
        fillProgressBars("");
        createProgressBars("");
    }
    private void toFormatStringFirst() {
        boolean checkPointF=amountFirst.getText().contains(".");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("#,##0.###", symbols);
        DecimalFormat formatPoint = new DecimalFormat("#,##0.000", symbols);
        double amount = Double.parseDouble(amountFirst.getText().replace(",", ""));
        if(checkPointF) amountFirst.setText(formatPoint.format(amount));
        else amountFirst.setText(format.format(amount));
    }
    private void toFormatStringSecond() {
        boolean checkPointS=amountSecond.getText().contains(".");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("#,##0.###", symbols);
        DecimalFormat formatPoint = new DecimalFormat("#,##0.000", symbols);
        double amount = Double.parseDouble(amountSecond.getText().replace(",", ""));
        if(checkPointS) amountSecond.setText(formatPoint.format(amount));
        else amountSecond.setText(format.format(amount));
    }
    private void updateExchangeRate() throws IOException {
        Label lblFrom, lblTo;
        String fromCur;
        String toCur;
        if (currencyLblFirstCheck) {
            lblFrom = amountFirst;
            lblTo = amountSecond;
            fromCur = currenciesListFirst.getSelectionModel().getSelectedItem().short_form;
            toCur = currenciesListSecond.getSelectionModel().getSelectedItem().short_form;
        }else {
            lblFrom = amountSecond;
            lblTo = amountFirst;
            toCur = currenciesListFirst.getSelectionModel().getSelectedItem().short_form;
            fromCur = currenciesListSecond.getSelectionModel().getSelectedItem().short_form;
        }
        URL url = new URL("https://v6.exchangerate-api.com/v6/"+API_KEY+"/pair/"+fromCur+"/"+toCur);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(reader);
        JsonObject jsonObject = root.getAsJsonObject();
        JsonElement conversionRate = jsonObject.get("conversion_rate");
        float rate = conversionRate.getAsFloat();
        double amountFrom = Double.parseDouble(lblFrom.getText().replace(",", ""));
        lblTo.setText(String.valueOf(amountFrom*rate));
    }
    private void toFormatString() {
        Label lbl;
        if (currencyLblFirstCheck) lbl = amountFirst;
        else lbl = amountSecond;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("#,##0.###", symbols);
        DecimalFormat formatPoint = new DecimalFormat("#,##0.000", symbols);
        double amount = Double.parseDouble(lbl.getText().replace(",", ""));

        if(checkPoint) lbl.setText(formatPoint.format(amount));
        else lbl.setText(format.format(amount));
    }
    private void updateComboboxes() throws SQLException {

        currenciesListFirst.getItems().addAll(dbHelperSLite.getCurrencies(""));
        currenciesListFirst.getSelectionModel().select(0);
        currenciesListSecond.getItems().addAll(dbHelperSLite.getCurrencies(""));
        currenciesListSecond.getSelectionModel().select(0);

    }
    public double getExchangeRate(String fromCur, String toCur, double amount) throws IOException {
        URL url = new URL("https://v6.exchangerate-api.com/v6/"+API_KEY+"/pair/"+fromCur+"/"+toCur);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(reader);
        JsonObject jsonObject = root.getAsJsonObject();
        JsonElement conversionRate = jsonObject.get("conversion_rate");
        float rate = conversionRate.getAsFloat();
        return amount*rate;
    }
    public void updateLegends(){
        chartLegList.getItems().clear();
        pieChart.getData().forEach(data -> {
            chartLegList.getItems().add(data.getName() + ": " + (int) data.getPieValue() + "%");
        });
    }
    int colorCounter = 1;
    public void setTransactValue(double amountFun, String transactName) throws SQLException, IOException {
        if(pieChart.getData().get(0).getName().equals("Транзакції відсутні")){
            pieChart.getData().clear();
        }
        int id = 0;
        boolean check = false;
        if (!pieChart.getData().isEmpty()){
            do{
                if(pieChart.getData().get(id).getName().equals(transactName)){
                    check = true;
                    break;
                }
                id++;
            }while (id <= pieChart.getData().size()-1);
            if(check) {
                pieChart.getData().get(id).setPieValue(pieChart.getData().get(id).getPieValue() + amountFun);
            }else{
                addNewData(amountFun, transactName, colors[colorCounter]);
                HBox legendHBox = new HBox(10);
                legendHBox.setAlignment(Pos.CENTER);
                Circle circle = new Circle(5, Color.web(colors[colorCounter]));
                colorCounter++;
                Text label = new Text(transactName+"\t");
                legendHBox.getChildren().addAll(circle, label);
                legendsFlowP.getChildren().add(legendHBox);
            }
        }else {
            addNewData(amountFun, transactName, colors[colorCounter]);
            HBox legendHBox = new HBox(10);
            legendHBox.setAlignment(Pos.CENTER);
            Circle circle = new Circle(5, Color.web(colors[colorCounter]));
            colorCounter++;
            Text label = new Text(transactName+"\t");
            legendHBox.getChildren().addAll(circle, label);
            legendsFlowP.getChildren().add(legendHBox);
        }
    }
    private void addNewData(double amount, String transactName, String color) throws SQLException, IOException {
        if(colorCounter == 28) colorCounter = 1;
        PieChart.Data transaction = new PieChart.Data(transactName, amount);
        pieChart.getData().add(transaction);
        pieChart.getData().get(pieChart.getData().size()-1).getNode().setStyle("-fx-pie-color: "+ color + ";");
        DecimalFormat expensesFormat = new DecimalFormat("#,##0.00");
    }
    public void converterBtnClick(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        Label lbl;
        if (currencyLblFirstCheck) lbl = amountFirst;
        else lbl = amountSecond;
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
                    if(part2[i] == '0'){
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
                lbl.setText(lbl.getText().replace(",", "") + ".000");
            }
        }
    }
    public void converterBtnClickDel() {
        Label lbl;
        if (currencyLblFirstCheck) lbl = amountFirst;
        else lbl = amountSecond;
        checkPoint= lbl.getText().contains(".");
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
    public void converterBtnClickClear() {
        checkPoint=false;
        Label lbl;
        if (currencyLblFirstCheck) lbl = amountFirst;
        else lbl = amountSecond;
        lbl.setText("0");
    }
    public void changeColorEnterL(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-background-color: #008db8;");
    }
    public void changeColorReleasedL(MouseEvent mouseEvent) {
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
    public void changeColorReleasedD(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-background-color: #01799a;");
        if(!button.isHover()){
            button.setStyle("-fx-background-color: #008db8;");
        }
    }
    public void changeColorExtraDark(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-background-color: #006278;");
    }
    boolean currencyLblFirstCheck = true;
    public void currencyLblFirst() {
        if(!currencyLblFirstCheck) {
            amountFirst.setStyle("-fx-font-weight: bold;");
            amountSecond.setStyle("-fx-font-weight: normal;");
            currencyLblFirstCheck = true;
        }
    }
    public void currencyLblSecond() {
        if(currencyLblFirstCheck) {
            amountFirst.setStyle("-fx-font-weight: normal;");
            amountSecond.setStyle("-fx-font-weight: bold;");
            currencyLblFirstCheck = false;
        }

    }
    public void clearData(ActionEvent actionEvent) throws SQLException {

        String query = "DELETE FROM Transactions";
        PreparedStatement statement = dbHelperSLite.getConnection().prepareStatement(query);
        statement.executeUpdate();

    }
    public void minusColorLight(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-border-color: #ff7777; -fx-background-color: transparent; -fx-border-width: 10px;");
        minusLbl.setTextFill(Paint.valueOf("#ff7777"));
    }
    public void minusColorStd(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-border-color: #ff3e3e; -fx-background-color: transparent; -fx-border-width: 10px; ");
        minusLbl.setTextFill(Paint.valueOf("#ff3e3e"));
    }
    public void minusColorDark(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-border-color: #cc1c1c; -fx-background-color: transparent; -fx-border-width: 10px;");
        minusLbl.setTextFill(Paint.valueOf("#cc1c1c"));
    }
    public void minusColorReleased(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-border-color: #ff7777; -fx-background-color: transparent; -fx-border-width: 10px;");
        minusLbl.setTextFill(Paint.valueOf("#ff7777"));
        if(!button.isHover()){
            button.setStyle("-fx-border-color: #ff3e3e; -fx-background-color: transparent; -fx-border-width: 10px;");
            minusLbl.setTextFill(Paint.valueOf("#ff3e3e"));
        }
    }
    public void plusColorLight(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-border-color: #bcffbc; -fx-background-color: transparent; -fx-border-width: 10px;");
        plusLbl.setTextFill(Paint.valueOf("#bcffbc"));
    }
    public void plusColorStd(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-border-color: #83ee83; -fx-background-color: transparent; -fx-border-width: 10px; ");
        plusLbl.setTextFill(Paint.valueOf("#83ee83"));
    }
    public void plusColorDark(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-border-color: #45e745; -fx-background-color: transparent; -fx-border-width: 10px;");
        plusLbl.setTextFill(Paint.valueOf("#45e745"));
    }
    public void plusColorReleased(MouseEvent mouseEvent) {
        Button button = (Button) mouseEvent.getSource();
        button.setStyle("-fx-border-color: #bcffbc; -fx-background-color: transparent; -fx-border-width: 10px;");
        plusLbl.setTextFill(Paint.valueOf("#bcffbc"));
        if(!button.isHover()){
            button.setStyle("-fx-border-color: #83ee83; -fx-background-color: transparent; -fx-border-width: 10px;");
            plusLbl.setTextFill(Paint.valueOf("#83ee83"));
        }
    }

    public void closeApp(ActionEvent actionEvent) throws SQLException, ParseException {
        updateDateSQLite();
        postgreSync();
        dbHelperPG.disconnect();
        dbHelperSLite.disconnect();
        Platform.exit();
    }
    public void saveChange(ActionEvent actionEvent) throws SQLException, ParseException {
        updateDateSQLite();
        postgreSync();
    }
    public void syncAction(ActionEvent actionEvent) throws SQLException, ParseException {
        MenuItem menuItem = (MenuItem) actionEvent.getSource();
        String text = "Ви впевнені що бажаете провести синхронізацію?";
        if(showAlert(text))
            switch (menuItem.getId()){
                case "syncPgLiteBtn" -> replacePGtoSQLite();
                case "syncLitePgBtn" -> replaceSQLitetoPG();
                case "autoSyncBtn" -> postgreSync();
            }
    }
    public void clearTransactions() throws SQLException {
        String text = "Ви впевнені що бажаете очитити транзакції?";
        if(showAlert(text)) dbHelperSLite.executeUpdate("DELETE FROM Transaction");
    }

    public boolean showAlert(String text){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Увага");
        alert.setHeaderText("Підтвердження операції");
        alert.setContentText(text);
        ButtonType yesBtn = new ButtonType("Так");
        ButtonType noBtn = new ButtonType("Ні");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getButtonTypes().setAll(yesBtn, noBtn);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == yesBtn;
    }
}
