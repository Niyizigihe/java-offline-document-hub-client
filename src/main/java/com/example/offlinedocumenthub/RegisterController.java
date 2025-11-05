package com.example.offlinedocumenthub;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label msgLabel;

    @FXML
    protected void onRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            msgLabel.setText("All fields are required.");
            return;
        }

        // Use API for registration
        boolean success = ApiClient.register(username, email, password);

        if (success) {
            msgLabel.setText("Registration successful!");
            msgLabel.setStyle("-fx-text-fill: green;");

            // Clear the fields
            usernameField.clear();
            emailField.clear();
            passwordField.clear();

            // Show success message and option to go to login
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText("Account Created");
            alert.setContentText("Your account has been created successfully. You can now login.");
            alert.showAndWait();

        } else {
            msgLabel.setText("Registration failed! Username may already exist.");
            msgLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void onBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 650);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login - Offline Document Hub");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}