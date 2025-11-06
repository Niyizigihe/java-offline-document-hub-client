package com.example.offlinedocumenthub;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ActivityLogController {

    @FXML
    private TableView<ActivityLog> logTable;

    @FXML
    private TableColumn<ActivityLog, String> colUser;

    @FXML
    private TableColumn<ActivityLog, String> colAction;

    @FXML
    private TableColumn<ActivityLog, String> colDetails;

    @FXML
    private TableColumn<ActivityLog, String> colTimestamp;

    @FXML
    private DatePicker filterDate;

    @FXML
    private ComboBox<String> filterUser;

    @FXML
    private ComboBox<String> filterAction;

    @FXML
    private Label lblTotalLogs;

    private ObservableList<ActivityLog> allLogs = FXCollections.observableArrayList();
    private ObservableList<ActivityLog> filteredLogs = FXCollections.observableArrayList();

    // Action types for filtering
    private final String[] ACTION_TYPES = {
            "ALL", "LOGIN", "LOGOUT", "UPLOAD", "DOWNLOAD",
            "EDIT", "DELETE", "BACKUP", "REGISTER", "SYNC"
    };

    @FXML
    public void initialize() {
        // Check if user is logged in and is admin
        if (!SessionManager.isLoggedIn()) {
            showAlertAndClose("Access Denied", "You must be logged in to access activity logs.");
            return;
        }

        if (!SessionManager.isAdmin()) {
            showAlertAndClose("Unauthorized Access", "Only administrators can access activity logs.");
            return;
        }

        // Initialize table columns
        setupTableColumns();

        // Initialize filter dropdowns
        initializeFilters();

        // Load logs from API
        loadLogsFromAPI();

        // Set up filter listeners for real-time filtering
        setupFilterListeners();
    }

    private void setupTableColumns() {
        colUser.setCellValueFactory(new PropertyValueFactory<>("userDisplayName"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        colDetails.setCellValueFactory(new PropertyValueFactory<>("actionDetails"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("formattedTimestamp"));

        // Add some styling to the table columns
        colUser.setStyle("-fx-alignment: CENTER_LEFT;");
        colAction.setStyle("-fx-alignment: CENTER;");
        colDetails.setStyle("-fx-alignment: CENTER_LEFT;");
        colTimestamp.setStyle("-fx-alignment: CENTER;");
    }

    private void initializeFilters() {
        // Initialize action filter
        filterAction.getItems().addAll(ACTION_TYPES);
        filterAction.setValue("ALL");

        // User filter will be populated from API data
        filterUser.getItems().add("ALL");
        filterUser.setValue("ALL");

        // Set today's date as default filter
        filterDate.setValue(LocalDate.now());
    }

    private void loadLogsFromAPI() {
        allLogs.clear();

        try {
            // Get logs from API
            List<ActivityLog> logs = ApiClient.getActivityLogs();
            if (logs != null && !logs.isEmpty()) {
                allLogs.addAll(logs);

                // Populate user filter
                ObservableList<String> users = FXCollections.observableArrayList();
                users.add("ALL");

                for (ActivityLog log : logs) {
                    // Add to user filter if not already present
                    String displayName = log.getUserDisplayName();
                    if (displayName != null && !users.contains(displayName)) {
                        users.add(displayName);
                    }
                }

                filterUser.setItems(users);
                applyFilters();
                updateLogCount();
            } else {
                showInfo("No Logs", "No activity logs found from the server.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Server Error", "Failed to load activity logs from server: " + e.getMessage());
        }
    }

    private void setupFilterListeners() {
        // Date filter listener - apply filters when date changes
        filterDate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // User filter listener - apply filters when user changes
        filterUser.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Action filter listener - apply filters when action changes
        filterAction.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    @FXML
    private void applyFilters() {
        filteredLogs.clear();

        LocalDate selectedDate = filterDate.getValue();
        String selectedUser = filterUser.getValue();
        String selectedAction = filterAction.getValue();

        for (ActivityLog log : allLogs) {
            boolean dateMatch = selectedDate == null ||
                    log.getTimestamp().toLocalDate().equals(selectedDate);
            boolean userMatch = "ALL".equals(selectedUser) ||
                    log.getUserDisplayName().equals(selectedUser);
            boolean actionMatch = "ALL".equals(selectedAction) ||
                    log.getActionType().equals(selectedAction);

            if (dateMatch && userMatch && actionMatch) {
                filteredLogs.add(log);
            }
        }

        logTable.setItems(filteredLogs);
        updateLogCount();
    }

    @FXML
    private void clearFilters() {
        filterDate.setValue(null);
        filterUser.setValue("ALL");
        filterAction.setValue("ALL");
        applyFilters();
    }

    @FXML
    private void refreshLogs() {
        loadLogsFromAPI();
        showInfo("Refreshed", "Activity logs refreshed successfully.");
    }

    @FXML
    private void exportLogs() {
        if (!SessionManager.isAdmin()) {
            showAlert("Permission Denied", "Only administrators can export activity logs.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Activity Logs");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv")
        );
        fileChooser.setInitialFileName("activity_logs_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        File file = fileChooser.showSaveDialog(logTable.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write CSV header
                writer.write("Timestamp,User,Action,Details\n");

                // Write data
                for (ActivityLog log : filteredLogs) {
                    writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            log.getFormattedTimestamp(),
                            log.getUserDisplayName(),
                            log.getActionType(),
                            log.getActionDetails().replace("\"", "\"\"") // Escape quotes in CSV
                    ));
                }

                writer.flush();
                showInfo("Export Successful",
                        "Activity logs exported successfully to:\n" + file.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Export Failed", "Failed to export logs: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onBackToDashboard() {
        try {
            Stage stage = (Stage) logTable.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("admin-dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Dashboard - Offline Document Hub");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }

    private void updateLogCount() {
        int total = allLogs.size();
        int filtered = filteredLogs.size();

        if (total == filtered) {
            lblTotalLogs.setText("Total Logs: " + total);
            lblTotalLogs.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            lblTotalLogs.setText("Showing: " + filtered + " of " + total + " logs");
            lblTotalLogs.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        }
    }

    private void showAlertAndClose(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        // Close the current window and go back to dashboard
        try {
            Stage stage = (Stage) logTable.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("admin-dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Dashboard - Offline Document Hub");
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) logTable.getScene().getWindow();
            stage.close();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}