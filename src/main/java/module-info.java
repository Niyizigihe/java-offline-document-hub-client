module com.example.offlinedocumenthub {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires io.javalin;
    requires com.fasterxml.jackson.databind;
    requires java.prefs;
    requires java.net.http;
    requires com.fasterxml.jackson.datatype.jsr310;


    opens com.example.offlinedocumenthub to javafx.fxml;
    exports com.example.offlinedocumenthub;
}