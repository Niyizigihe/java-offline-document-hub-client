package com.example.offlinedocumenthub;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DashboardController {

    @FXML private StackPane contentPane;
    @FXML private Label statusLabel;

    @FXML
    private void showDocuments() {
        statusLabel.setText("Documents module is selected.");
        contentPane.getChildren().clear();
        contentPane.getChildren().add(new Label("Documents view (TODO)"));
    }

    @FXML
    private void showChat() {
        statusLabel.setText("Chat module is selected.");
        contentPane.getChildren().clear();
        contentPane.getChildren().add(new Label("Chat view (TODO)"));
    }

    @FXML
    private void showUsers() {
        statusLabel.setText("Users module is selected.");
        contentPane.getChildren().clear();
        contentPane.getChildren().add(new Label("Users management (admin only)"));
    }

    @FXML
    private void showBackup() {
        statusLabel.setText("Backup module is selected.");
        contentPane.getChildren().clear();
        contentPane.getChildren().add(new Label("Backup status (TODO)"));
    }

    @FXML
    private void onSync() {
        Alert a = new Alert(AlertType.INFORMATION, "Sync triggered (admin only) - not implemented yet.");
        a.showAndWait();
    }

    @FXML
    private void onLogout() {
        // return to login screen
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("hello-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 900, 600);
            javafx.stage.Stage stage = (javafx.stage.Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Offline Document Hub - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
