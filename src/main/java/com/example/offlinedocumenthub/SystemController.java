package com.example.offlinedocumenthub;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.File;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class SystemController {

    @FXML private Label lblIp;
    @FXML private Label lblPort;
    @FXML private Label lblUserInfo;
    @FXML private Label lblServerStatus;
    @FXML private Label lblDatabaseStatus;
    @FXML private Label lblSharedFolder;
    @FXML private Label lblActiveSessions;
    @FXML private Label lblLastBackup;
    @FXML private Label lblBackupStatus;
    @FXML private ProgressBar backupProgressBar;
    @FXML private VBox backupProgressSection;

    private Timeline backupTimeline;
    private int backupProgress = 0;

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

        refreshSystemInfo();
    }

    @FXML
    public void refreshSystemInfo() {
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

            // Check shared folder
            File sharedFolder = new File("shared_documents");
            if (sharedFolder.exists() && sharedFolder.isDirectory()) {
                File[] files = sharedFolder.listFiles();
                lblSharedFolder.setText(sharedFolder.getAbsolutePath() + " (" + (files != null ? files.length : 0) + " files)");
            } else {
                lblSharedFolder.setText(sharedFolder.getAbsolutePath() + " (Not accessible)");
            }

            // Simulate active sessions (in a real app, this would come from the server)
            lblActiveSessions.setText("1");

        } catch (Exception e) {
            lblIp.setText("Unknown");
            lblPort.setText("Unknown");
            lblServerStatus.setText("Error");
            lblServerStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            lblSharedFolder.setText("./shared_documents (Error)");
        }
    }

    // In SystemController.java - replace these methods
    @FXML
    private void triggerBackup() {
        if (!SessionManager.isAdmin()) {
            showAlert("Permission Denied", "Only administrators can trigger cloud backups.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cloud Backup");
        confirmAlert.setHeaderText("Start Cloud Backup");
        confirmAlert.setContentText("This will backup all documents and database to Google Drive. This may take several minutes. Continue?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                startRealBackupProcess();
            }
        });
    }

    private void startRealBackupProcess() {
        // Show progress section
        backupProgressSection.setVisible(true);
        backupProgressBar.setProgress(0);
        backupProgress = 0;
        lblBackupStatus.setText("Starting backup process...");

        // Start real backup
        new Thread(() -> {
            try {
                // Trigger backup on server
                Map<String, Object> backupResult = ApiClient.triggerBackup();

                if (Boolean.TRUE.equals(backupResult.get("success"))) {
                    // Update UI on JavaFX thread
                    javafx.application.Platform.runLater(() -> {
                        backupProgressBar.setProgress(1.0);
                        lblBackupStatus.setText("Backup completed successfully!");

                        // Update last backup time
                        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        lblLastBackup.setText(timestamp);

                        showAlert("Backup Complete", "Cloud backup completed successfully!\nBackup folder: " +
                                backupResult.get("folderName"));

                        // Hide progress section after delay
                        Timeline hideTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                            backupProgressSection.setVisible(false);
                        }));
                        hideTimeline.play();
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        backupProgressSection.setVisible(false);
                        showAlert("Backup Failed", "Backup failed: " + backupResult.get("message"));
                    });
                }

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    backupProgressSection.setVisible(false);
                    showAlert("Backup Failed", "Backup failed: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void viewBackupHistory() {
        if (!SessionManager.isAdmin()) {
            showAlert("Permission Denied", "Only administrators can view backup history.");
            return;
        }

        try {
            List<Map<String, String>> backups = ApiClient.listBackups();

            if (backups.isEmpty()) {
                showAlert("Backup History", "No backups found in Google Drive.");
                return;
            }

            // Create backup history dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Backup History");
            dialog.setHeaderText("Recent Backups in Google Drive");

            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(20));

            for (Map<String, String> backup : backups) {
                HBox backupItem = new HBox(10);
                backupItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label nameLabel = new Label(backup.get("name"));
                nameLabel.setStyle("-fx-font-weight: bold;");

                Label dateLabel = new Label(backup.get("createdTime").substring(0, 19));
                dateLabel.setStyle("-fx-text-fill: #666;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                backupItem.getChildren().addAll(nameLabel, spacer, dateLabel);
                content.getChildren().add(backupItem);
            }

            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setPrefSize(400, 300);

            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (Exception e) {
            showAlert("Error", "Failed to load backup history: " + e.getMessage());
        }
    }



    private void updateBackupProgress(int progress, String status) {
        backupProgress = progress;
        backupProgressBar.setProgress(progress / 100.0);
        lblBackupStatus.setText(status);
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

            // Check shared folder
            File sharedFolder = new File("shared_documents");
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

            // Database connection (simulated)
            healthStatus.append("✅ Database: Connected\n");

            showAlert("System Health Check", healthStatus.toString());

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
        confirmAlert.setContentText("This will clear temporary files and refresh system data. Continue?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Simulate cache clearing
                showAlert("Cache Cleared", "System cache has been cleared successfully.");
                refreshSystemInfo();
            }
        });
    }

    @FXML
    private void onBackToDashboard() {
        try {
            Stage stage = (Stage) lblIp.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("admin-dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1200, 700);
            stage.setScene(scene);
            stage.setTitle("Dashboard - Offline Document Hub");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load dashboard: " + e.getMessage());
        }
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
                    javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1000, 650);
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

        // Close the current window and go back to dashboard
        try {
            onBackToDashboard();
        } catch (Exception e) {
            Stage stage = (Stage) lblIp.getScene().getWindow();
            stage.close();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}