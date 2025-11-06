//package com.example.offlinedocumenthub;
//
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.control.Button;
//import javafx.scene.layout.StackPane;
//import javafx.scene.layout.VBox;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//
//public class AdminDashboardController {
//
//    @FXML private StackPane contentPane;
//    @FXML private VBox leftMenu;
//    @FXML private Button btnUsers;
//    @FXML private Button btnDocuments;
//    @FXML private Button btnActivityLogs;
//    @FXML private Button btnSystemControl;
//
//    @FXML
//    public void initialize() {
//        setupRoleBasedAccess();
//    }
//
//    private void setupRoleBasedAccess() {
//        // Check user role and show/hide buttons accordingly
//        if (SessionManager.isLoggedIn()) {
//            String role = SessionManager.getCurrentRole();
//            String username = SessionManager.getCurrentUsername();
//
//            System.out.println("User logged in: " + username + " with role: " + role);
//
//            if ("admin".equals(role)) {
//                // Admin can see all buttons
//                btnUsers.setVisible(true);
//                btnUsers.setManaged(true);
//                btnActivityLogs.setVisible(true);
//                btnActivityLogs.setManaged(true);
//                btnSystemControl.setVisible(true);
//                btnSystemControl.setManaged(true);
//                btnDocuments.setVisible(true);
//                btnDocuments.setManaged(true);
//            } else {
//                // Student can only see Documents button
//                btnUsers.setVisible(false);
//                btnUsers.setManaged(false);
//                btnActivityLogs.setVisible(false);
//                btnActivityLogs.setManaged(false);
//                btnSystemControl.setVisible(false);
//                btnSystemControl.setManaged(false);
//                btnDocuments.setVisible(true);
//                btnDocuments.setManaged(true);
//
//                // Also update the button text for students
//                btnDocuments.setText("Shared Documents");
//            }
//        } else {
//            // If not logged in, hide all buttons (shouldn't happen, but safety check)
//            btnUsers.setVisible(false);
//            btnUsers.setManaged(false);
//            btnActivityLogs.setVisible(false);
//            btnActivityLogs.setManaged(false);
//            btnSystemControl.setVisible(false);
//            btnSystemControl.setManaged(false);
//            btnDocuments.setVisible(false);
//            btnDocuments.setManaged(false);
//        }
//    }
//
//    @FXML
//    private void showUserManagement() {
//        if (!SessionManager.isAdmin()) {
//            showAccessDeniedAlert();
//            return;
//        }
//        loadPane("user-management-view.fxml");
//    }
//
//    @FXML
//    private void showDocumentManagement() {
//        loadPane("document-management-view.fxml");
//    }
//
//    @FXML
//    private void showActivityLogs() {
//        if (!SessionManager.isAdmin()) {
//            showAccessDeniedAlert();
//            return;
//        }
//        loadPane("activity-log-view.fxml");
//    }
//
//    @FXML
//    private void showSystemControl() {
//        if (!SessionManager.isAdmin()) {
//            showAccessDeniedAlert();
//            return;
//        }
//        loadPane("system-control-view.fxml");
//    }
//
//    @FXML
//    private void logout() {
//        try {
//            // Clear session
//            SessionManager.logout();
//            ApiClient.logout();
//
//            // Navigate back to login
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
//            Scene scene = new Scene(loader.load(), 800, 600);
//            Stage stage = (Stage) contentPane.getScene().getWindow();
//            stage.setScene(scene);
//            stage.setTitle("Offline Document Hub - Login");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void loadPane(String fxmlFile) {
//        try {
//            Node pane = FXMLLoader.load(getClass().getResource(fxmlFile));
//            contentPane.getChildren().clear();
//            contentPane.getChildren().add(pane);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("Failed to load: " + fxmlFile);
//        }
//    }
//
//    private void showAccessDeniedAlert() {
//        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
//        alert.setTitle("Access Denied");
//        alert.setHeaderText("Insufficient Permissions");
//        alert.setContentText("You do not have permission to access this feature. Please contact an administrator.");
//        alert.showAndWait();
//    }
//}

package com.example.offlinedocumenthub;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
//import javafx.scene.control.Text;
import javafx.scene.text.Text;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {

    @FXML private StackPane contentPane;
    @FXML private VBox leftMenu;
    @FXML private VBox welcomePane;
    @FXML private Button btnUsers;
    @FXML private Button btnDocuments;
    @FXML private Button btnActivityLogs;
    @FXML private Button btnSystemControl;

    // Welcome pane components
    @FXML private Label lblWelcome;
    @FXML private Label lblUserWelcome;
    @FXML private Label lblDocumentCount;
    @FXML private Label lblUserCount;
    @FXML private Label lblActivityCount;
    @FXML private VBox documentsCard;
    @FXML private VBox usersCard;
    @FXML private VBox activityCard;
    @FXML private Button btnQuickUpload;
    @FXML private Button btnQuickViewDocs;
    @FXML private Button btnQuickUsers;
    @FXML private Text txtProjectInfo;
    @FXML private Label lblServerStatus;
    @FXML private Label lblDatabaseStatus;
    @FXML private Label lblLastLogin;

    @FXML
    public void initialize() {
        setupRoleBasedAccess();
        loadDashboardData();
        showWelcomePane();
    }

    private void setupRoleBasedAccess() {
        if (SessionManager.isLoggedIn()) {
            String role = SessionManager.getCurrentRole();
            String username = SessionManager.getCurrentUsername();
            String fullName = SessionManager.getCurrentFullName();

            // Update welcome labels
            lblWelcome.setText("Welcome, " + fullName + " (" + role + ")");
            lblUserWelcome.setText("Hello " + fullName + "! Welcome back to Offline Document Hub");

            if ("admin".equals(role)) {
                // Admin can see all buttons
                setButtonVisibility(true, true, true, true);
                setQuickActionVisibility(true, true, true);
                setCardVisibility(true, true, true);
            } else {
                // Student can only see Documents button
                setButtonVisibility(false, true, false, false);
                setQuickActionVisibility(true, true, false);
                setCardVisibility(true, false, false);

                // Update text for students
                btnDocuments.setText("My Documents");
                btnQuickViewDocs.setText("📋 View My Documents");
            }
        }
    }

    private void setButtonVisibility(boolean users, boolean documents, boolean activityLogs, boolean system) {
        btnUsers.setVisible(users);
        btnUsers.setManaged(users);
        btnActivityLogs.setVisible(activityLogs);
        btnActivityLogs.setManaged(activityLogs);
        btnSystemControl.setVisible(system);
        btnSystemControl.setManaged(system);
        btnDocuments.setVisible(documents);
        btnDocuments.setManaged(documents);
    }

    private void setQuickActionVisibility(boolean upload, boolean viewDocs, boolean users) {
        btnQuickUpload.setVisible(upload);
        btnQuickUpload.setManaged(upload);
        btnQuickViewDocs.setVisible(viewDocs);
        btnQuickViewDocs.setManaged(viewDocs);
        btnQuickUsers.setVisible(users);
        btnQuickUsers.setManaged(users);
    }

    private void setCardVisibility(boolean documents, boolean users, boolean activity) {
        documentsCard.setVisible(documents);
        documentsCard.setManaged(documents);
        usersCard.setVisible(users);
        usersCard.setManaged(users);
        activityCard.setVisible(activity);
        activityCard.setManaged(activity);
    }

    private void loadDashboardData() {
        try {
            // Load document count
            List<Document> documents = ApiClient.getDocuments();
            lblDocumentCount.setText(String.valueOf(documents != null ? documents.size() : 0));

            // Load user count (admin only)
            if (SessionManager.isAdmin()) {
                List<User> users = ApiClient.getUsers();
                lblUserCount.setText(String.valueOf(users != null ? users.size() : 0));
            }

            // Load activity count (admin only)
            if (SessionManager.isAdmin()) {
                List<ActivityLog> activities = ApiClient.getActivityLogs();
                lblActivityCount.setText(String.valueOf(activities != null ? activities.size() : 0));
            }

            // Check system status
            boolean serverOnline = ApiClient.testConnection();
            lblServerStatus.setText("● Server: " + (serverOnline ? "Online" : "Offline"));
            lblServerStatus.setStyle(serverOnline ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;" : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            // Database status (simplified - we assume if server is online, DB is connected)
            lblDatabaseStatus.setText("● Database: " + (serverOnline ? "Connected" : "Disconnected"));
            lblDatabaseStatus.setStyle(serverOnline ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;" : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            // Last login
            lblLastLogin.setText("Last login: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));

        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            lblServerStatus.setText("● Server: Error");
            lblServerStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }

    private void showWelcomePane() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(welcomePane);
    }

    @FXML
    private void showUserManagement() {
        if (!SessionManager.isAdmin()) {
            showAccessDeniedAlert();
            return;
        }
        loadPane("user-management-view.fxml");
    }

    @FXML
    private void showDocumentManagement() {
        loadPane("document-management-view.fxml");
    }

    @FXML
    private void showActivityLogs() {
        if (!SessionManager.isAdmin()) {
            showAccessDeniedAlert();
            return;
        }
        loadPane("activity-log-view.fxml");
    }

    @FXML
    private void showSystemControl() {
        if (!SessionManager.isAdmin()) {
            showAccessDeniedAlert();
            return;
        }
        loadPane("system-control-view.fxml");
    }

    @FXML
    private void onBackToDashboard() {
        try {
            Stage stage = (Stage) contentPane.getScene().getWindow(); // Get current stage
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("admin-dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1200, 700); // Set dimensions
            stage.setScene(scene);
            stage.setTitle("Dashboard - Offline Document Hub");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // Show error message to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to load dashboard");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void quickUpload() {
        // Simulate quick upload - you can enhance this with actual file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Document to Upload");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showOpenDialog(contentPane.getScene().getWindow());
        if (file != null) {
            // Show quick upload dialog
            showInfo("Quick Upload", "Selected file: " + file.getName() + "\n\nPlease use the Documents section to complete the upload with title and description.");
            showDocumentManagement(); // Navigate to documents for completion
        }
    }

    @FXML
    private void refreshDashboard() {
        loadDashboardData();
        showInfo("Dashboard Refreshed", "Dashboard statistics updated successfully.");
    }

    private void loadPane(String fxmlFile) {
        try {
            Node pane = FXMLLoader.load(getClass().getResource(fxmlFile));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load: " + fxmlFile);
        }
    }

    @FXML
    private void logout() {
        try {
            SessionManager.logout();
            ApiClient.logout();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Offline Document Hub - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAccessDeniedAlert() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Insufficient Permissions");
        alert.setContentText("You do not have permission to access this feature. Please contact an administrator.");
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}