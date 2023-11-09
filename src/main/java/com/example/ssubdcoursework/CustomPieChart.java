package com.example.ssubdcoursework;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class CustomPieChart extends PieChart {
    public CustomPieChart() {
        super();
    }
    public CustomPieChart(ObservableList<PieChart.Data> data) {
        super(data);
        setLabelLineLength(20);
        createScrollableLegend();
        setLegendSide(Side.RIGHT);
        setClockwise(true);
    }
    private void createScrollableLegend() {
        Node legend = getLegend();
        if (legend != null) {
            legend.prefWidth(100);
            ScrollPane scrollPane = new ScrollPane(legend);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.maxHeightProperty().bind(heightProperty());
            setLegend(scrollPane);
        }
    }
}
