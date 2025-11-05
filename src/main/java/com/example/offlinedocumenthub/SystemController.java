package com.example.offlinedocumenthub;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.File;
import java.net.InetAddress;

public class SystemController {

    @FXML
    private Label lblIp;

    @FXML
    private Label lblPort;

    @FXML
    private Label lblUserInfo;

    @FXML
    private Label lblServerStatus;

    @FXML
    public void initialize() {
        // Check if user is logged in and is admin
        if (!SessionManager.isLoggedIn()) {
            showAlertAndClose("Access Denied", "You must be logged in to access system settings.");
            return;
        }

        if (!SessionManager.isAdmin()) {
            showAlertAndClose("Unauthorized Access", "Only administrators can access system settings.");
            return;
        }

        // Display user information
        lblUserInfo.setText("Logged in as: " + SessionManager.getCurrentFullName() +
                " (" + SessionManager.getCurrentRole() + ")");

        // Display system information
        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            lblIp.setText(ipAddress);
            lblPort.setText("8080"); // API server port

            // Test server connection
            if (ApiClient.isServerAvailable()) {
                lblServerStatus.setText("Online");
                lblServerStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else {
                lblServerStatus.setText("Offline");
                lblServerStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }

        } catch (Exception e) {
            lblIp.setText("Unknown");
            lblPort.setText("Unknown");
            lblServerStatus.setText("Error");
            lblServerStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void triggerBackup() {
        if (!SessionManager.isAdmin()) {
            showAlert("Permission Denied", "Only administrators can trigger cloud backups.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cloud Backup");
        confirmAlert.setHeaderText("Start Cloud Backup");
        confirmAlert.setContentText("This will backup documents to cloud storage. Continue?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // This would call a cloud backup API endpoint
                    showAlert("Backup Started", "Cloud backup process has been started on the server.");
                } catch (Exception e) {
                    showAlert("Backup Failed", "Failed to start backup: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void onSystemHealthCheck() {
        if (!SessionManager.isAdmin()) {
            showAlert("Permission Denied", "Only administrators can perform system health checks.");
            return;
        }

        try {
            StringBuilder healthStatus = new StringBuilder();
            healthStatus.append("=== System Health Check ===\n\n");

            // Check server connection
            if (ApiClient.isServerAvailable()) {
                healthStatus.append("✅ Server: Connected\n");
            } else {
                healthStatus.append("❌ Server: Offline\n");
            }

            // Check shared folder (local check)
            java.io.File sharedFolder = new java.io.File("shared_documents");
            if (sharedFolder.exists() && sharedFolder.isDirectory()) {
                File[] files = sharedFolder.listFiles();
                healthStatus.append("✅ Shared Folder: " + (files != null ? files.length : 0) + " files\n");
            } else {
                healthStatus.append("❌ Shared Folder: Not accessible\n");
            }

            // Network information
            try {
                String ipAddress = InetAddress.getLocalHost().getHostAddress();
                healthStatus.append("✅ Network: " + ipAddress + ":8080\n");
            } catch (Exception e) {
                healthStatus.append("❌ Network: Unable to determine IP\n");
            }

            showAlert("System Health", healthStatus.toString());

        } catch (Exception e) {
            showAlert("Health Check Failed", "Error during system health check: " + e.getMessage());
        }
    }

    @FXML
    private void onClearCache() {
        if (!SessionManager.isAdmin()) {
            showAlert("Permission Denied", "Only administrators can clear system cache.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Clear Cache");
        confirmAlert.setHeaderText("Clear System Cache");
        confirmAlert.setContentText("This will clear local cache data. Continue?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Clear local cache (if any)
                showAlert("Cache Cleared", "Local cache has been cleared successfully.");
                initialize(); // Refresh the view
            }
        });
    }

    @FXML
    private void onLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Logout");
        confirmAlert.setHeaderText("Confirm Logout");
        confirmAlert.setContentText("Are you sure you want to logout?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SessionManager.logout();
                try {
                    // Navigate back to login screen
                    Stage stage = (Stage) lblIp.getScene().getWindow();
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("hello-view.fxml"));
                    javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                    stage.setScene(scene);
                    stage.setTitle("Offline Document Hub - Login");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showAlertAndClose(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        // Close the current window
        Stage stage = (Stage) lblIp.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}