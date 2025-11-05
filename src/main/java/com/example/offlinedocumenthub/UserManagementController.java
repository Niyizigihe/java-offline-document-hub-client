package com.example.offlinedocumenthub;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;

public class UserManagementController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Void> colActions;

    @FXML
    public void initialize() {
        // Check if user is admin
        if (!SessionManager.isAdmin()) {
            showAlertAndClose("Access Denied", "Only administrators can access user management.");
            return;
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        addActionsToTable();
        loadUsersFromAPI();
    }

    private void addActionsToTable() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                editBtn.setOnAction(e -> editUser(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteUser(getTableView().getItems().get(getIndex())));
            }

            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    @FXML
    private void addUser() {
        showUserForm(null); // null means new user
    }

    private void editUser(User user) {
        showUserForm(user); // user != null means edit
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete user " + user.getUsername() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(button -> {
            if (button == ButtonType.YES) {
                boolean success = ApiClient.deleteUser(user.getId());
                if (success) {
                    loadUsersFromAPI();
                    showAlert("Success", "User deleted successfully.");
                } else {
                    showAlert("Error", "Failed to delete user from server.");
                }
            }
        });
    }

    private void loadUsersFromAPI() {
        userTable.getItems().clear();
        try {
            List<User> users = ApiClient.getUsers();
            userTable.getItems().addAll(users);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Server Error", "Failed to load users from server: " + e.getMessage());
        }
    }

    private void showUserForm(User userToEdit) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(userToEdit == null ? "Add User" : "Edit User");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20,150,10,10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField fullnameField = new TextField();
        fullnameField.setPromptText("Full Name");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(userToEdit == null ? "Password" : "(Leave blank to keep)");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("admin", "student");
        roleBox.setValue(userToEdit == null ? "student" : userToEdit.getRole());

        if (userToEdit != null) {
            usernameField.setText(userToEdit.getUsername());
            fullnameField.setText(userToEdit.getFullName());
        }

        grid.add(new Label("Username:"),0,0); grid.add(usernameField,1,0);
        grid.add(new Label("Full Name:"),0,1); grid.add(fullnameField,1,1);
        grid.add(new Label("Password:"),0,2); grid.add(passwordField,1,2);
        grid.add(new Label("Role:"),0,3); grid.add(roleBox,1,3);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(userToEdit == null);

        // Enable/disable save button based on input
        usernameField.textProperty().addListener((obs, oldVal, newVal) ->
                updateSaveButton(saveButton, userToEdit, usernameField, fullnameField, passwordField));
        fullnameField.textProperty().addListener((obs, oldVal, newVal) ->
                updateSaveButton(saveButton, userToEdit, usernameField, fullnameField, passwordField));
        passwordField.textProperty().addListener((obs, oldVal, newVal) ->
                updateSaveButton(saveButton, userToEdit, usernameField, fullnameField, passwordField));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String username = usernameField.getText().trim();
                String fullname = fullnameField.getText().trim();
                String password = passwordField.getText().trim();
                String role = roleBox.getValue();

                User user = new User();
                user.setId(userToEdit != null ? userToEdit.getId() : 0);
                user.setUsername(username);
                user.setFullName(fullname);
                user.setRole(role);

                // Only set password if provided (for new users or when changing password)
                if (!password.isEmpty() || userToEdit == null) {
                    user.setPassword(hashPassword(password));
                } else if (userToEdit != null) {
                    // Keep existing password if not changed
                    user.setPassword(userToEdit.getPassword());
                }

                return user;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            boolean success;
            if (userToEdit == null) {
                success = ApiClient.createUser(user);
            } else {
                success = ApiClient.updateUser(user);
            }

            if (success) {
                loadUsersFromAPI();
                showAlert("Success",
                        userToEdit == null ? "User created successfully!" : "User updated successfully!");
            } else {
                showAlert("Error",
                        userToEdit == null ? "Failed to create user!" : "Failed to update user!");
            }
        });
    }

    private void updateSaveButton(Node saveButton, User userToEdit,
                                  TextField usernameField, TextField fullnameField, PasswordField passwordField) {
        boolean usernameValid = !usernameField.getText().trim().isEmpty();
        boolean fullnameValid = !fullnameField.getText().trim().isEmpty();
        boolean passwordValid = userToEdit != null || !passwordField.getText().trim().isEmpty();

        saveButton.setDisable(!(usernameValid && fullnameValid && passwordValid));
    }

    private String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }

    private void showAlertAndClose(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        // Close and go back to dashboard
        try {
            Stage stage = (Stage) userTable.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("admin-dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Dashboard - Offline Document Hub");
        } catch (Exception e) {
            e.printStackTrace();
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