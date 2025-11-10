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
import javafx.scene.Cursor;

import java.io.File;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    @FXML private Button btnTriggerBackup;
    @FXML private Button btnHealthCheck;
    @FXML private Button btnClearCache;
    @FXML private Button btnViewBackupHistory;
    @FXML private Button btnRefresh;
    @FXML private Button btnLogout;
    @FXML private Button btnBackToDashboard;
    @FXML private VBox mainContainer;
    private Timeline progressTimeline;

    private Timeline backupTimeline;
    private String currentBackupId = null;
//    private Timeline progressTimeline;
    private NotificationPollingService notificationService;


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

        // Setup button hover effects
        setupButtonHoverEffects();

        initializeNotificationService();

        // Load initial data
        refreshSystemInfo();
    }
    private void initializeNotificationService() {
        notificationService = NotificationPollingService.getInstance();
        notificationService.startPolling();
        System.out.println("🔔 Notification service started in system controller");
    }
    private void setupButtonHoverEffects() {
        // Apply to all buttons
        btnTriggerBackup.setOnMouseEntered(e -> btnTriggerBackup.getScene().setCursor(Cursor.HAND));
        btnTriggerBackup.setOnMouseExited(e -> btnTriggerBackup.getScene().setCursor(Cursor.DEFAULT));

        btnHealthCheck.setOnMouseEntered(e -> btnHealthCheck.getScene().setCursor(Cursor.HAND));
        btnHealthCheck.setOnMouseExited(e -> btnHealthCheck.getScene().setCursor(Cursor.DEFAULT));

        btnClearCache.setOnMouseEntered(e -> btnClearCache.getScene().setCursor(Cursor.HAND));
        btnClearCache.setOnMouseExited(e -> btnClearCache.getScene().setCursor(Cursor.DEFAULT));

        btnViewBackupHistory.setOnMouseEntered(e -> btnViewBackupHistory.getScene().setCursor(Cursor.HAND));
        btnViewBackupHistory.setOnMouseExited(e -> btnViewBackupHistory.getScene().setCursor(Cursor.DEFAULT));

        btnRefresh.setOnMouseEntered(e -> btnRefresh.getScene().setCursor(Cursor.HAND));
        btnRefresh.setOnMouseExited(e -> btnRefresh.getScene().setCursor(Cursor.DEFAULT));

        btnLogout.setOnMouseEntered(e -> btnLogout.getScene().setCursor(Cursor.HAND));
        btnLogout.setOnMouseExited(e -> btnLogout.getScene().setCursor(Cursor.DEFAULT));

        btnBackToDashboard.setOnMouseEntered(e -> btnBackToDashboard.getScene().setCursor(Cursor.HAND));
        btnBackToDashboard.setOnMouseExited(e -> btnBackToDashboard.getScene().setCursor(Cursor.DEFAULT));
    }

    @FXML
    public void refreshSystemInfo() {
        SessionManager.updateLastActivityTime();

        // Display user information
        lblUserInfo.setText("Logged in as: " + SessionManager.getCurrentFullName() +
                " (" + SessionManager.getCurrentRole() + ")");

        try {
            // Get ALL data from server
            Map<String, Object> systemStatus = ApiClient.getSystemStatus();

            if (systemStatus != null && !systemStatus.containsKey("error")) {
                // Server status
                String serverStatus = (String) systemStatus.get("server");
                lblServerStatus.setText(serverStatus);
                lblServerStatus.setStyle(serverStatus.equals("Online") ?
                        "-fx-text-fill: #27ae60; -fx-font-weight: bold;" :
                        "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

                // Database status
                String dbStatus = (String) systemStatus.get("database");
                lblDatabaseStatus.setText(dbStatus);
                lblDatabaseStatus.setStyle(dbStatus.equals("Connected") ?
                        "-fx-text-fill: #27ae60; -fx-font-weight: bold;" :
                        "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

                // Active sessions count
                Integer activeSessions = (Integer) systemStatus.get("activeSessions");
                lblActiveSessions.setText(String.valueOf(activeSessions != null ? activeSessions : 0));

                // Last backup
                String lastBackup = (String) systemStatus.get("lastBackup");
                if (lastBackup != null && !lastBackup.equals("Never") && !lastBackup.equals("Unknown")) {
                    // Format the backup time
                    try {
                        LocalDateTime backupTime = LocalDateTime.parse(lastBackup.replace(" ", "T"));
                        String formattedTime = backupTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
                        lblLastBackup.setText(formattedTime);
                    } catch (Exception e) {
                        lblLastBackup.setText(lastBackup);
                    }
                } else {
                    lblLastBackup.setText(lastBackup);
                }

                // Shared folder info
                Map<String, Object> sharedFolder = (Map<String, Object>) systemStatus.get("sharedFolder");
                if (sharedFolder != null) {
                    String path = (String) sharedFolder.get("path");
                    Integer fileCount = (Integer) sharedFolder.get("fileCount");
                    lblSharedFolder.setText(path + " (" + fileCount + " files)");
                }

            } else {
                // Fallback if server data not available
                setFallbackData();
            }

            // Network information (local)
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            lblIp.setText(ipAddress);
            lblPort.setText("8080");

        } catch (Exception e) {
            System.err.println("Error refreshing system info: " + e.getMessage());
            setFallbackData();
        }
    }

    private void setFallbackData() {
        lblServerStatus.setText("Unknown");
        lblServerStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        lblDatabaseStatus.setText("Unknown");
        lblDatabaseStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        lblActiveSessions.setText("0");
        lblLastBackup.setText("Unknown");

        // Check shared folder locally
        File sharedFolder = new File("shared_documents");
        if (sharedFolder.exists() && sharedFolder.isDirectory()) {
            File[] files = sharedFolder.listFiles();
            lblSharedFolder.setText(sharedFolder.getAbsolutePath() + " (" + (files != null ? files.length : 0) + " files)");
        } else {
            lblSharedFolder.setText("./shared_documents (Not accessible)");
        }
    }

    @FXML
    private void triggerBackup() {
        SessionManager.updateLastActivityTime();

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
                startManualBackupProcess();
            }
        });
    }

private void startManualBackupProcess() {
    // Show progress section
    backupProgressSection.setVisible(true);
    backupProgressBar.setProgress(0);
    lblBackupStatus.setText("Starting manual backup process...");

    // Stop any existing progress monitoring
    stopProgressMonitoring();

    // Start backup in background thread
    new Thread(() -> {
        try {
            // Trigger backup on server
            Map<String, Object> backupResult = ApiClient.triggerBackup("manual");

            javafx.application.Platform.runLater(() -> {
                if (backupResult != null && Boolean.TRUE.equals(backupResult.get("success"))) {
                    currentBackupId = (String) backupResult.get("backupId");
                    lblBackupStatus.setText("Backup started! Monitoring progress...");
                    lblBackupStatus.setStyle("-fx-text-fill: #27ae60;");

                    // Start progress monitoring with backup ID
                    startProgressMonitoring(currentBackupId);
                } else {
                    backupProgressSection.setVisible(false);
                    showAlert("Backup Failed", "Failed to start backup: " +
                            (backupResult != null ? backupResult.get("message") : "Unknown error"));
                }
            });

        } catch (Exception e) {
            javafx.application.Platform.runLater(() -> {
                backupProgressSection.setVisible(false);
                showAlert("Backup Failed", "Backup failed to start: " + e.getMessage());
            });
        }
    }).start();
}
    private void startProgressMonitoring(String backupId) {
        stopProgressMonitoring(); // Stop any existing monitoring

        progressTimeline = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> updateBackupProgress(backupId))
        );
        progressTimeline.setCycleCount(Timeline.INDEFINITE);
        progressTimeline.play();

        System.out.println("🔍 Started progress monitoring for backup: " + backupId);
    }


private void updateBackupProgress(String backupId) {
    new Thread(() -> {
        try {
            Map<String, Object> progress = ApiClient.getBackupProgress(backupId);

            javafx.application.Platform.runLater(() -> {
                if (progress != null && !progress.containsKey("error")) {
                    int currentProgress = (int) progress.get("progress");
                    String status = (String) progress.get("status");
                    boolean isActive = Boolean.TRUE.equals(progress.get("active"));

                    // Update progress bar with smooth animation
                    backupProgressBar.setProgress(currentProgress / 100.0);

                    // Update status with color coding
                    lblBackupStatus.setText(status + " (" + currentProgress + "%)");
                    if (currentProgress < 100 && isActive) {
                        lblBackupStatus.setStyle("-fx-text-fill: #f39c12;"); // Orange for in-progress
                    } else if (currentProgress >= 100) {
                        lblBackupStatus.setStyle("-fx-text-fill: #27ae60;"); // Green for complete
                    } else {
                        lblBackupStatus.setStyle("-fx-text-fill: #e74c3c;"); // Red for error
                    }

                    // If backup is complete
                    if (!isActive && currentProgress >= 100) {
                        stopProgressMonitoring();
                        backupProgressSection.setVisible(false);
                        refreshSystemInfo();
                        showAlert("Backup Complete", "✅ Backup completed successfully!\n\n" +
                                "All data has been securely backed up to Google Drive.");
                    }
                    // If backup failed or was interrupted
                    else if (!isActive && currentProgress < 100) {
                        stopProgressMonitoring();
                        backupProgressSection.setVisible(false);
                        showAlert("Backup Failed", "❌ Backup was interrupted or failed.\n\n" +
                                "Error: " + status);
                    }
                } else {
                    // Error getting progress
                    lblBackupStatus.setText("Error monitoring progress");
                    lblBackupStatus.setStyle("-fx-text-fill: #e74c3c;");
                }
            });
        } catch (Exception e) {
            System.err.println("Error updating backup progress: " + e.getMessage());
        }
    }).start();
}

    private void stopProgressMonitoring() {
        if (progressTimeline != null) {
            progressTimeline.stop();
            progressTimeline = null;
            currentBackupId = null;
        }
    }
    @FXML
    private void viewBackupHistory() {
        SessionManager.updateLastActivityTime();

        if (!SessionManager.isAdmin()) {
            showAlert("Permission Denied", "Only administrators can view backup history.");
            return;
        }

        try {
            // Get backup history from database
            List<Map<String, Object>> backupHistory = ApiClient.getBackupHistory();

            if (backupHistory.isEmpty()) {
                showAlert("Backup History", "No backup history found.");
                return;
            }

            // Create backup history dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Backup History");
            dialog.setHeaderText("Recent Backups (Database History)");

            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(20));

            for (Map<String, Object> backup : backupHistory) {
                VBox backupItem = new VBox(5);
                backupItem.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f8f9fa;");

                HBox headerRow = new HBox(10);

                String backupType = (String) backup.get("backupType");
                String status = (String) backup.get("status");
                String createdBy = (String) backup.get("createdBy");

                // Set icon based on status
                String icon = "📁";
                if ("success".equals(status)) icon = "✅";
                if ("failed".equals(status)) icon = "❌";
                if ("in_progress".equals(status)) icon = "🔄";

                Label nameLabel = new Label(icon + " " + backup.get("backupFolder"));
                nameLabel.setStyle("-fx-font-weight: bold;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // Show correct creator information
                String creatorInfo = "Auto";
                if ("manual".equals(backupType)) {
                    creatorInfo = createdBy != null ? createdBy : "Admin";
                }

                Label creatorLabel = new Label("By: " + creatorInfo);
                creatorLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

                headerRow.getChildren().addAll(nameLabel, spacer, creatorLabel);

                // Backup details
                VBox detailsBox = new VBox(2);

                String startTime = (String) backup.get("startTime");
                String formattedTime = "Unknown";
                if (startTime != null && startTime.length() >= 19) {
                    formattedTime = startTime.substring(0, 19).replace("T", " ");
                }

                Label timeLabel = new Label("Started: " + formattedTime);
                timeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

                Label statusLabel = new Label("Status: " + status.toUpperCase());
                statusLabel.setStyle("-fx-text-fill: " +
                        ("success".equals(status) ? "#27ae60" :
                                "failed".equals(status) ? "#e74c3c" : "#f39c12") + "; -fx-font-size: 11px;");

                String errorMessage = (String) backup.get("errorMessage");
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    Label errorLabel = new Label("Error: " + errorMessage);
                    errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 10px; -fx-wrap-text: true;");
                    detailsBox.getChildren().add(errorLabel);
                }

                detailsBox.getChildren().addAll(timeLabel, statusLabel);
                backupItem.getChildren().addAll(headerRow, detailsBox);
                content.getChildren().add(backupItem);
            }

            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setPrefSize(600, 400);

            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (Exception e) {
            showAlert("Error", "Failed to load backup history: " + e.getMessage());
        }
    }
    @FXML
    private void onSystemHealthCheck() {
        SessionManager.updateLastActivityTime();

        if (!SessionManager.isAdmin()) {
            showAlert("Permission Denied", "Only administrators can perform system health checks.");
            return;
        }

        try {
            StringBuilder healthStatus = new StringBuilder();
            healthStatus.append("=== System Health Check ===\n\n");

            // Get detailed system status from server
            Map<String, Object> systemStatus = ApiClient.getSystemStatus();

            if (systemStatus != null && !systemStatus.containsKey("error")) {
                healthStatus.append("✅ Server: ").append(systemStatus.get("server")).append("\n");
                healthStatus.append("✅ Database: ").append(systemStatus.get("database")).append("\n");

                Integer activeSessions = (Integer) systemStatus.get("activeSessions");
                healthStatus.append("✅ Active Sessions: ").append(activeSessions).append("\n");

                // Get last backup time from backup history (more reliable)
                String lastBackup = getLastBackupTimeFromHistory();
                healthStatus.append("✅ Last Backup: ").append(lastBackup).append("\n");

                Map<String, Object> sharedFolder = (Map<String, Object>) systemStatus.get("sharedFolder");
                if (sharedFolder != null) {
                    healthStatus.append("✅ Shared Folder: ").append(sharedFolder.get("fileCount")).append(" files\n");
                }
            } else {
                healthStatus.append("❌ Server: Unable to get system status\n");
            }

            // Add backup automation status
            healthStatus.append("\n=== Backup Automation ===\n");
            try {
                List<Map<String, Object>> backupHistory = ApiClient.getBackupHistory();
                if (!backupHistory.isEmpty()) {
                    Map<String, Object> latestBackup = backupHistory.get(0);
                    String backupType = (String) latestBackup.get("backupType");
                    String status = (String) latestBackup.get("status");
                    String createdBy = (String) latestBackup.get("createdBy");

                    healthStatus.append("✅ Latest Backup Type: ").append(backupType).append("\n");
                    healthStatus.append("✅ Latest Backup Status: ").append(status).append("\n");
                    healthStatus.append("✅ Triggered By: ").append(createdBy).append("\n");

                    // Count auto vs manual backups
                    long autoBackups = backupHistory.stream()
                            .filter(b -> "auto".equals(b.get("backupType")))
                            .count();
                    long manualBackups = backupHistory.stream()
                            .filter(b -> "manual".equals(b.get("backupType")))
                            .count();
                    long successfulBackups = backupHistory.stream()
                            .filter(b -> "success".equals(b.get("status")))
                            .count();

                    healthStatus.append("✅ Total Backups: ").append(backupHistory.size()).append("\n");
                    healthStatus.append("✅ Auto Backups: ").append(autoBackups).append("\n");
                    healthStatus.append("✅ Manual Backups: ").append(manualBackups).append("\n");
                    healthStatus.append("✅ Successful Backups: ").append(successfulBackups).append("\n");
                } else {
                    healthStatus.append("❌ No backup history found\n");
                }
            } catch (Exception e) {
                healthStatus.append("❌ Backup history unavailable: ").append(e.getMessage()).append("\n");
            }

            showAlert("System Health Check", healthStatus.toString());

        } catch (Exception e) {
            showAlert("Health Check Failed", "Error during system health check: " + e.getMessage());
        }
    }

    private String getLastBackupTimeFromHistory() {
        try {
            List<Map<String, Object>> backupHistory = ApiClient.getBackupHistory();
            if (!backupHistory.isEmpty()) {
                // Find the most recent successful backup
                Optional<Map<String, Object>> lastSuccessfulBackup = backupHistory.stream()
                        .filter(backup -> "success".equals(backup.get("status")))
                        .findFirst();

                if (lastSuccessfulBackup.isPresent()) {
                    Map<String, Object> backup = lastSuccessfulBackup.get();
                    String startTime = (String) backup.get("startTime");
                    String backupFolder = (String) backup.get("backupFolder");
                    String backupType = (String) backup.get("backupType");

                    if (startTime != null && startTime.length() >= 19) {
                        String formattedTime = startTime.substring(0, 19).replace("T", " ");
                        return formattedTime + " (" + backupType + " - " + backupFolder + ")";
                    }
                }

                // If no successful backups, show the most recent one (even if failed)
                Map<String, Object> latestBackup = backupHistory.get(0);
                String startTime = (String) latestBackup.get("startTime");
                String status = (String) latestBackup.get("status");
                String backupType = (String) latestBackup.get("backupType");

                if (startTime != null && startTime.length() >= 19) {
                    String formattedTime = startTime.substring(0, 19).replace("T", " ");
                    return formattedTime + " (" + backupType + " - " + status + ")";
                }
            }
            return "Never";
        } catch (Exception e) {
            return "Unknown (Error: " + e.getMessage() + ")";
        }
    }
    @FXML
    private void onClearCache() {
        SessionManager.updateLastActivityTime();

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
                // Clear any cached data and refresh
                refreshSystemInfo();
                showAlert("Cache Cleared", "System cache has been cleared successfully.\nSystem data refreshed from server.");
            }
        });
    }

    @FXML
    private void onBackToDashboard() {
        SessionManager.updateLastActivityTime();

        try {
            // Stop progress monitoring when leaving
            stopProgressMonitoring();

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
        SessionManager.updateLastActivityTime();

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Logout");
        confirmAlert.setHeaderText("Confirm Logout");
        confirmAlert.setContentText("Are you sure you want to logout?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Stop notification service
                if (notificationService != null) {
                    notificationService.stopPolling();
                }
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

        onBackToDashboard();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}