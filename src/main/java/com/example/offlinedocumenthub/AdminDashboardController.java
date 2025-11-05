package com.example.offlinedocumenthub;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private StackPane contentPane;

    @FXML
    private void showUserManagement() {
        loadPane("user-management-view.fxml");
    }

    @FXML
    private void showDocumentManagement() {
        loadPane("document-management-view.fxml");
    }

    @FXML
    private void showActivityLogs() {
        loadPane("activity-log-view.fxml");
    }

    @FXML
    private void showSystemControl() {
        loadPane("system-control-view.fxml");
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Offline Document Hub - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPane(String fxmlFile) {
        try {
            Node pane = FXMLLoader.load(getClass().getResource(fxmlFile));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
