module com.example.javaproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires itextpdf;
    requires java.desktop;
    requires langchain4j.open.ai;
    requires org.apache.poi.ooxml;
    requires webcam.capture;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires javafx.swing;
    requires javafx.base;
    requires opencv;



    opens com.example.javaproject to javafx.fxml;
    exports com.example.javaproject;
    exports com.example.javaproject.controllers;
    opens com.example.javaproject.controllers to javafx.fxml;
    exports com.example.javaproject.models;
    opens com.example.javaproject.models to javafx.fxml;
    exports com.example.javaproject.dao;
    opens com.example.javaproject.dao to javafx.fxml;
}