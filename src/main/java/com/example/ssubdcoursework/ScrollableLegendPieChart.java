package com.example.ssubdcoursework;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class ScrollableLegendPieChart extends VBox {

    private PieChart pieChart;
    private ListView<String> legendList;
    private ScrollPane scrollPane;

    public ScrollableLegendPieChart() {
        pieChart = new PieChart();
        legendList = new ListView<>();
        scrollPane = new ScrollPane(legendList);

        pieChart.dataProperty().addListener((observable, oldValue, newValue) -> updateLegendList());
        this.getChildren().addAll(pieChart, scrollPane);
    }

    private void updateLegendList() {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (PieChart.Data data : pieChart.getData()) {
            items.add(data.getName() + ": " + data.getPieValue());
        }
        legendList.setItems(items);
    }

    public PieChart getPieChart() {
        return pieChart;
    }
}