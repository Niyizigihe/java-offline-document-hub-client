package com.example.offlinedocumenthub;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

public class DocumentManagementController {

    @FXML private TableView<Document> documentTable;
    @FXML private TableColumn<Document, Integer> colId;
    @FXML private TableColumn<Document, String> colTitle;
    @FXML private TableColumn<Document, String> colFilePath;
    @FXML private TableColumn<Document, String> colUploadedBy;
    @FXML private TableColumn<Document, LocalDate> colDate;
    @FXML private TableColumn<Document, String> colFileSize;
    @FXML private TableColumn<Document, Void> colActions;
    @FXML private TextField searchField;
    @FXML private Label lblUserInfo;

    private final ObservableList<Document> documents = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Display current user info
        if (SessionManager.isLoggedIn()) {
            lblUserInfo.setText("Welcome, " + SessionManager.getCurrentFullName() +
                    " (" + SessionManager.getCurrentRole() + ")");
        }

        setupColumns();
        loadDocuments();

        // Search filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterDocuments(newVal));
    }

    private void setupColumns() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getDocId()).asObject());
        colTitle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        colFilePath.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFilePath()));
        colUploadedBy.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUploadedBy()));
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getUploadDate()));
        colFileSize.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFileSize()));

        // Actions column (Download/Edit/Delete buttons)
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button downloadBtn = new Button("Download");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                // Style buttons
                downloadBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                editBtn.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                // Set button actions
                downloadBtn.setOnAction(e -> onDownloadDocument(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> onEditDocument(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> onDeleteDocument(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Document document = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5, downloadBtn, editBtn);

                    // Only show delete button for admins or document owners
                    if (SessionManager.isAdmin() ||
                            (SessionManager.isLoggedIn() && document.getUserId() == SessionManager.getCurrentUserId())) {
                        buttons.getChildren().add(deleteBtn);
                    }

                    setGraphic(buttons);
                }
            }
        });
    }

    @FXML
    public void loadDocuments() {
        documents.clear();

        try {
            // Get documents from API
            List<Document> docs = ApiClient.getDocuments();
            documents.addAll(docs);
            documentTable.setItems(documents);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Server Error", "Failed to load documents from server: " + e.getMessage());
        }
    }

    private void onDownloadDocument(Document doc) {
        try {
            // For now, we'll handle download locally
            // In a real implementation, you'd call an API endpoint to get the file
            File sourceFile = new File(doc.getFilePath());

            if (!sourceFile.exists()) {
                showError("Download Failed", "File not found: " + doc.getFilePath());
                return;
            }

            // Let user choose download location
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose Download Location");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            File selectedDirectory = directoryChooser.showDialog(documentTable.getScene().getWindow());

            if (selectedDirectory != null) {
                Path destinationPath = selectedDirectory.toPath().resolve(sourceFile.getName());

                // Copy file to selected location
                Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                // Log the download activity
                logActivity("DOWNLOAD", "Downloaded document: " + doc.getTitle());

                showInfo("Download Successful",
                        "File downloaded successfully to:\n" + destinationPath.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Download Failed", "Failed to download file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Download Failed", "An error occurred during download: " + e.getMessage());
        }
    }

    @FXML
    private void onAddDocument() {
        if (!SessionManager.isLoggedIn()) {
            showError("Access Denied", "You must be logged in to upload documents.");
            return;
        }

        // Use API-based dialog
        showAddDocumentDialog();
    }

    private void showAddDocumentDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Document File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documents (*.pdf, *.doc, *.docx, *.txt)", "*.pdf", "*.doc", "*.docx", "*.txt")
        );

        File file = fileChooser.showOpenDialog(documentTable.getScene().getWindow());
        if (file != null) {
            TextInputDialog dialog = new TextInputDialog(file.getName().replaceFirst("[.][^.]+$", ""));
            dialog.setTitle("Add Document");
            dialog.setHeaderText("Enter Document Title");
            dialog.setContentText("Title:");

            dialog.showAndWait().ifPresent(title -> {
                if (!title.trim().isEmpty()) {
                    // Use API to upload document
                    boolean success = ApiClient.createDocument(title.trim(), file.getName(), file);
                    if (success) {
                        showInfo("Success", "Document uploaded successfully!");
                        loadDocuments();
                        logActivity("UPLOAD", "Uploaded document: " + title);
                    } else {
                        showError("Upload Failed", "Failed to upload document to server");
                    }
                }
            });
        }
    }

    private void onEditDocument(Document doc) {
        // Check permissions - only admin or document owner can edit
        if (!SessionManager.isAdmin() &&
                (SessionManager.isLoggedIn() && doc.getUserId() != SessionManager.getCurrentUserId())) {
            showError("Permission Denied", "You can only edit your own documents.");
            return;
        }

        // For now, show simple edit dialog
        TextInputDialog dialog = new TextInputDialog(doc.getTitle());
        dialog.setTitle("Edit Document");
        dialog.setHeaderText("Edit Document Title");
        dialog.setContentText("Title:");

        dialog.showAndWait().ifPresent(title -> {
            if (!title.trim().isEmpty() && !title.equals(doc.getTitle())) {
                // Update document via API (you'll need to implement updateDocument in ApiClient)
                showInfo("Info", "Edit functionality will be implemented in API");
                logActivity("EDIT", "Edited document: " + doc.getTitle());
            }
        });
    }

    private void onDeleteDocument(Document doc) {
        // Check permissions - only admin or document owner can delete
        if (!SessionManager.isAdmin() &&
                (SessionManager.isLoggedIn() && doc.getUserId() != SessionManager.getCurrentUserId())) {
            showError("Permission Denied", "You can only delete your own documents.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Document");
        alert.setHeaderText("Confirm Deletion");
        alert.setContentText("Are you sure you want to delete '" + doc.getTitle() + "'?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = ApiClient.deleteDocument(doc.getDocId());
                if (success) {
                    loadDocuments();
                    logActivity("DELETE", "Deleted document: " + doc.getTitle());
                    showInfo("Success", "Document deleted successfully.");
                } else {
                    showError("Error", "Failed to delete document from server");
                }
            }
        });
    }

    private void filterDocuments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            documentTable.setItems(documents);
            return;
        }

        String lower = keyword.toLowerCase();
        ObservableList<Document> filtered = FXCollections.observableArrayList();

        for (Document doc : documents) {
            if (doc.getTitle().toLowerCase().contains(lower) ||
                    doc.getUploadedBy().toLowerCase().contains(lower) ||
                    doc.getFilePath().toLowerCase().contains(lower)) {
                filtered.add(doc);
            }
        }
        documentTable.setItems(filtered);
    }

    @FXML
    private void onSyncCloud() {
        if (!SessionManager.isAdmin()) {
            showError("Permission Denied", "Only administrators can sync to cloud.");
            return;
        }

        try {
            // This would call a cloud backup API endpoint
            showInfo("Info", "Cloud backup functionality will be implemented via API");
            logActivity("BACKUP", "Performed cloud backup");
        } catch (Exception e) {
            showError("Sync Failed", e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        loadDocuments();
        showInfo("Refreshed", "Document list refreshed successfully.");
    }

    private void logActivity(String actionType, String details) {
        if (!SessionManager.isLoggedIn()) return;

        // This would call an API endpoint to log activity
        // For now, we'll just print to console
        System.out.println("Activity: " + actionType + " - " + details);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setHeaderText(title);
        alert.showAndWait();
    }
}