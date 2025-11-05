package com.example.offlinedocumenthub;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label msgLabel;
    @FXML private Label serverStatusLabel;

    private static String currentServerAddress = "localhost";

    @FXML
    public void initialize() {
        // Ask for server address when the login screen loads
        askForServerAddress();
    }

    private void askForServerAddress() {
        TextInputDialog dialog = new TextInputDialog("localhost");
        dialog.setTitle("Server Connection");
        dialog.setHeaderText("Connect to Document Hub Server");
        dialog.setContentText("Enter server IP address:");

        // Set a larger dialog
        dialog.getEditor().setPrefWidth(300);

        dialog.showAndWait().ifPresent(result -> {
            if (!result.trim().isEmpty()) {
                currentServerAddress = result.trim();
                ApiClient.setServerAddress(currentServerAddress);
                testServerConnection();
            }
        });
    }

    private void testServerConnection() {
        msgLabel.setText("Testing connection to server...");

        if (ApiClient.testConnection()) {
            serverStatusLabel.setText("✅ Connected to: " + currentServerAddress);
            serverStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            msgLabel.setText("Server connected successfully! Please login.");
            msgLabel.setStyle("-fx-text-fill: #27ae60;");
        } else {
            serverStatusLabel.setText("❌ Cannot connect to: " + currentServerAddress);
            serverStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            msgLabel.setText("Server connection failed! Please check the IP address.");
            msgLabel.setStyle("-fx-text-fill: #e74c3c;");

            // Ask to try again
            askToRetryConnection();
        }
    }

    private void askToRetryConnection() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Connection Failed");
        alert.setHeaderText("Cannot connect to server");
        alert.setContentText("Would you like to enter a different server address?");

        ButtonType retryButton = new ButtonType("Change Server");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(retryButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == retryButton) {
                askForServerAddress();
            }
        });
    }

    @FXML
    void onLogin(ActionEvent event) {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        // Validate empty fields
        if (user.isEmpty() || pass.isEmpty()) {
            msgLabel.setText("Please fill in all fields!");
            msgLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // Test server connection first
        if (!ApiClient.testConnection()) {
            msgLabel.setText("Server connection lost! Please check connection.");
            msgLabel.setStyle("-fx-text-fill: #e74c3c;");
            askToRetryConnection();
            return;
        }

        // Show loading state
        msgLabel.setText("Logging in...");
        msgLabel.setStyle("-fx-text-fill: #3498db;");

        // Use API login
        boolean apiSuccess = ApiClient.login(user, pass);
        if (apiSuccess) {
            msgLabel.setText("Login successful! Redirecting...");
            msgLabel.setStyle("-fx-text-fill: #27ae60;");
            proceedToDashboard(event);
        } else {
            msgLabel.setText("Login failed! Check your username and password.");
            msgLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    void onChangeServer(ActionEvent event) {
        askForServerAddress();
    }

    @FXML
    void onTestConnection(ActionEvent event) {
        testServerConnection();
    }

    private void proceedToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-dashboard-view.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 650);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Dashboard - Offline Document Hub - Connected to: " + currentServerAddress);
            stage.show();
        } catch (IOException e) {
            msgLabel.setText("Failed to load dashboard!");
            msgLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }

    @FXML
    protected void onExit(ActionEvent event) {
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void onRegisterLinkClick(ActionEvent event) {
        // Test connection before allowing registration
        if (!ApiClient.testConnection()) {
            msgLabel.setText("Cannot register - server connection required!");
            msgLabel.setStyle("-fx-text-fill: #e74c3c;");
            askToRetryConnection();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register-view.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 650);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Register - Offline Document Hub");
        } catch (IOException e) {
            e.printStackTrace();
            msgLabel.setText("Failed to open registration page.");
            msgLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }
}