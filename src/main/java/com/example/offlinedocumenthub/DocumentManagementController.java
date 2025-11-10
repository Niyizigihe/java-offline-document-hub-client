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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

import static com.example.offlinedocumenthub.ApiClient.*;
//import static javax.xml.catalog.BaseEntry.CatalogEntryType.URI;

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

        if (SessionManager.isAdmin()) {
            NotificationPollingService.getInstance().startPolling();
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

            // Update the button visibility logic
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Document document = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);

                    // Always show download button
                    buttons.getChildren().add(downloadBtn);

                    // Show edit/delete only for admins or document owners
                    boolean canModify = SessionManager.isAdmin() ||
                            (SessionManager.isLoggedIn() && document.getUserId() == SessionManager.getCurrentUserId());

                    if (canModify) {
                        buttons.getChildren().add(editBtn);
                        buttons.getChildren().add(deleteBtn);
                    }

                    setGraphic(buttons);
                }
            }
        });
    }

    @FXML
    private void loadDocuments() {
        documents.clear();
        try {
            System.out.println("=== Loading Documents ===");
            List<Document> docs = ApiClient.getDocuments();
            System.out.println("Received " + docs.size() + " documents from API");

            for (Document doc : docs) {
                System.out.println("Doc: " + doc.getTitle() +
                        " | Size: " + doc.getFileSize() +
                        " | By: " + doc.getUploadedBy());
            }

            documents.addAll(docs);
            documentTable.setItems(documents);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Server Error", "Failed to load documents: " + e.getMessage());
        }
    }

//    private void onDownloadDocument(Document doc) {
//        try {
//            // For now, we'll handle download locally
//            // In a real implementation, you'd call an API endpoint to get the file
//            File sourceFile = new File(doc.getFilePath());
//
//            if (!sourceFile.exists()) {
//                showError("Download Failed", "File not found: " + doc.getFilePath());
//                return;
//            }
//
//            // Let user choose download location
//            DirectoryChooser directoryChooser = new DirectoryChooser();
//            directoryChooser.setTitle("Choose Download Location");
//            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
//
//            File selectedDirectory = directoryChooser.showDialog(documentTable.getScene().getWindow());
//
//            if (selectedDirectory != null) {
//                Path destinationPath = selectedDirectory.toPath().resolve(sourceFile.getName());
//
//                // Copy file to selected location
//                Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
//
//                // Log the download activity
//                logActivity("DOWNLOAD", "Downloaded document: " + doc.getTitle());
//
//                showInfo("Download Successful",
//                        "File downloaded successfully to:\n" + destinationPath.toString());
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            showError("Download Failed", "Failed to download file: " + e.getMessage());
//        } catch (Exception e) {
//            e.printStackTrace();
//            showError("Download Failed", "An error occurred during download: " + e.getMessage());
//        }
//    }
private void onDownloadDocument(Document doc) {
    try {
        System.out.println("Downloading document ID: " + doc.getDocId() + " - " + doc.getTitle());

        // Show progress
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Downloading");
        progressAlert.setHeaderText("Please wait");
        progressAlert.setContentText("Downloading document from server...");
        progressAlert.show();

        // Download file from server using ApiClient
        boolean success = downloadFileFromServer(doc);

        progressAlert.close();

        if (success) {
            logActivity("DOWNLOAD", "Downloaded document: " + doc.getTitle());
            showInfo("Download Successful", "File downloaded successfully!");
        } else {
            showError("Download Failed", "Failed to download file from server.");
        }

    } catch (Exception e) {
        System.err.println("ERROR during download: " + e.getMessage());
        e.printStackTrace();
        showError("Download Failed", "Failed to download file: " + e.getMessage());
    }
}

//    private boolean downloadFileFromServer(Document doc) {
//        try {
//            // Use ApiClient to handle the download
//            String url = ApiClient.getBaseUrl() + "/documents/" + doc.getDocId() + "/download";
//
//            HttpRequest request = ApiClient.addAuthHeader(HttpRequest.newBuilder()
//                            .uri(URI.create(url))
//                            .GET())
//                    .build();
//
//            HttpResponse<byte[]> response = ApiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
//
//            System.out.println("Download response status: " + response.statusCode());
//
//            if (response.statusCode() == 200) {
//                byte[] fileData = response.body();
//
//                // Let user choose save location
//                FileChooser fileChooser = new FileChooser();
//                fileChooser.setTitle("Save Document");
//                fileChooser.setInitialFileName(doc.getTitle() + getFileExtension(doc.getFilePath()));
//                fileChooser.getExtensionFilters().add(
//                        new FileChooser.ExtensionFilter("All Files", "*.*")
//                );
//
//                File saveFile = fileChooser.showSaveDialog(documentTable.getScene().getWindow());
//
//                if (saveFile != null) {
//                    // Save the file
//                    Files.write(saveFile.toPath(), fileData);
//                    System.out.println("File saved to: " + saveFile.getAbsolutePath());
//                    return true;
//                }
//            } else {
//                System.err.println("Download failed with status: " + response.statusCode());
//                String errorBody = new String(response.body());
//                System.err.println("Error response: " + errorBody);
//            }
//
//            return false;
//        } catch (Exception e) {
//            System.err.println("ERROR downloading file from server: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        }
//    }

    private boolean downloadFileFromServer(Document doc) {
        try {
            // Use the dedicated download method from ApiClient
            byte[] fileData = ApiClient.downloadDocument(doc.getDocId());

            if (fileData != null && fileData.length > 0) {
                // Let user choose save location
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Document");

                // Generate safe filename
                String safeFileName = doc.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_");
                String extension = getFileExtension(doc.getFilePath());
                fileChooser.setInitialFileName(safeFileName + extension);

                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("All Files", "*.*"),
                        new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                        new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx"),
                        new FileChooser.ExtensionFilter("Text Files", "*.txt")
                );

                File saveFile = fileChooser.showSaveDialog(documentTable.getScene().getWindow());

                if (saveFile != null) {
                    // Save the file
                    Files.write(saveFile.toPath(), fileData);
                    System.out.println("File downloaded successfully to: " + saveFile.getAbsolutePath());
                    System.out.println("File size: " + fileData.length + " bytes");
                    return true;
                }
            } else {
                System.err.println("No file data received from server");
            }

            return false;
        } catch (Exception e) {
            System.err.println("ERROR downloading file from server: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String getFileExtension(String filePath) {
        if (filePath == null) return "";
        int lastDotIndex = filePath.lastIndexOf(".");
        return (lastDotIndex > 0) ? filePath.substring(lastDotIndex) : ".bin";
    }

    // Helper method to add auth header (add this to your ApiClient if not exists)
    private static HttpRequest.Builder addAuthHeader(HttpRequest.Builder builder) {
        if (ApiClient.isAuthenticated()) {
            // You'll need to make authToken accessible or add a method in ApiClient
            return builder.header("Authorization", "Bearer " + getAuthToken());
        }
        return builder;
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
                    // Show progress
                    Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
                    progressAlert.setTitle("Uploading");
                    progressAlert.setHeaderText("Please wait");
                    progressAlert.setContentText("Uploading document...");
                    progressAlert.show();

                    // Use API to upload document
                    boolean success = ApiClient.createDocument(title.trim(), file.getAbsolutePath(), file);

                    progressAlert.close();

                    if (success) {
                        showInfo("Success", "Document uploaded successfully!");
                        loadDocuments();
                    } else {
                        showError("Upload Failed", "Failed to upload document to server. Check server connection.");
                    }
                }
            });
        }
    }


//    private void onEditDocument(Document doc) {
//        // Check permissions - only admin or document owner can edit
//        if (!SessionManager.isAdmin() &&
//                (SessionManager.isLoggedIn() && doc.getUserId() != SessionManager.getCurrentUserId())) {
//            showError("Permission Denied", "You can only edit your own documents.");
//            return;
//        }
//
//        // For now, show simple edit dialog
//        TextInputDialog dialog = new TextInputDialog(doc.getTitle());
//        dialog.setTitle("Edit Document");
//        dialog.setHeaderText("Edit Document Title");
//        dialog.setContentText("Title:");
//
//        dialog.showAndWait().ifPresent(title -> {
//            if (!title.trim().isEmpty() && !title.equals(doc.getTitle())) {
//                // Update document via API (you'll need to implement updateDocument in ApiClient)
//                showInfo("Info", "Edit functionality will be implemented in API");
//                logActivity("EDIT", "Edited document: " + doc.getTitle());
//            }
//        });
//    }
private void onEditDocument(Document doc) {
    // Check permissions - only admin or document owner can edit
    if (!SessionManager.isAdmin() &&
            (SessionManager.isLoggedIn() && doc.getUserId() != SessionManager.getCurrentUserId())) {
        showError("Permission Denied", "You can only edit your own documents.");
        return;
    }

    // Show edit dialog
    TextInputDialog dialog = new TextInputDialog(doc.getTitle());
    dialog.setTitle("Edit Document");
    dialog.setHeaderText("Edit Document Title");
    dialog.setContentText("New Title:");

    dialog.showAndWait().ifPresent(title -> {
        if (!title.trim().isEmpty() && !title.equals(doc.getTitle())) {
            // Create a copy with updated title
            Document updatedDoc = new Document();
            updatedDoc.setDocId(doc.getDocId());
            updatedDoc.setTitle(title.trim());
            updatedDoc.setFilePath(doc.getFilePath());
            updatedDoc.setUploadedBy(doc.getUploadedBy());
            updatedDoc.setUploadDate(doc.getUploadDate());
            updatedDoc.setUserId(doc.getUserId());
            updatedDoc.setFileSize(doc.getFileSize());

            // Show progress
            Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
            progressAlert.setTitle("Updating Document");
            progressAlert.setHeaderText("Please wait");
            progressAlert.setContentText("Updating document title...");
            progressAlert.show();

            // Call the update API
            boolean success = ApiClient.updateDocument(updatedDoc);

            progressAlert.close();

            if (success) {
                loadDocuments();
                logActivity("EDIT", "Edited document: " + doc.getTitle() + " to " + title);
                showInfo("Success", "Document updated successfully.");
            } else {
                showError("Error", "Failed to update document. Please try again.");
            }
        } else if (title.trim().isEmpty()) {
            showError("Error", "Document title cannot be empty.");
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
    private void debugFilePaths() {
        System.out.println("=== DEBUG FILE PATHS ===");
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        File sharedFolder = new File("shared_documents");
        System.out.println("Shared folder exists: " + sharedFolder.exists());
        System.out.println("Shared folder path: " + sharedFolder.getAbsolutePath());

        if (sharedFolder.exists() && sharedFolder.isDirectory()) {
            File[] files = sharedFolder.listFiles();
            System.out.println("Files in shared folder: " + (files != null ? files.length : 0));
            if (files != null) {
                for (File file : files) {
                    System.out.println("  - " + file.getName() + " (" + file.length() + " bytes)");
                }
            }
        }

        for (Document doc : documents) {
            System.out.println("---");
            System.out.println("Document: " + doc.getTitle());
            System.out.println("  DB Path: " + doc.getFilePath());

            File file = new File(doc.getFilePath());
            System.out.println("  File exists: " + file.exists());
            System.out.println("  Absolute path: " + file.getAbsolutePath());

            // Try to find file by name in shared_documents
            String fileName = new File(doc.getFilePath()).getName();
            File sharedFile = new File("shared_documents", fileName);
            System.out.println("  Shared folder file exists: " + sharedFile.exists());
            System.out.println("  Shared file path: " + sharedFile.getAbsolutePath());
        }
        System.out.println("=== END DEBUG ===");

        showInfo("Debug Info", "Check console for file path debugging information.");
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