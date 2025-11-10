package com.example.offlinedocumenthub;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   💻 OFFLINE DOCUMENT HUB - CLIENT MODE");
        System.out.println("   Starting JavaFX Application...");
        System.out.println("=========================================");

        // Launch JavaFX application
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load and show the login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Offline Document Hub - Client");
        primaryStage.setScene(new Scene(root, 1000, 650));
        primaryStage.show();
//        BackupAutomationService.startBackupMonitoring();
    }
}