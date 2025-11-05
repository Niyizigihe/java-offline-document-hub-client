package com.example.offlinedocumenthub;

import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import java.util.prefs.Preferences;

public class ServerConfig {
    private static final Preferences prefs = Preferences.userNodeForPackage(ServerConfig.class);
    private static String serverAddress = "localhost";

    public static String getServerAddress() {
        // For development, always use localhost
        return "localhost";
    }

    // This will be used later when we deploy
    public static String askForServerAddress() {
        TextInputDialog dialog = new TextInputDialog("192.168.1.100");
        dialog.setTitle("Server Connection");
        dialog.setHeaderText("Enter Server IP Address");
        dialog.setContentText("Server IP:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String address = result.get().trim();
            prefs.put("serverAddress", address);
            return address;
        }
        return "localhost";
    }
}