module com.example.ssubdcoursework {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.gson;
    requires java.scripting;


    opens com.example.ssubdcoursework to javafx.fxml;
    exports com.example.ssubdcoursework;
}