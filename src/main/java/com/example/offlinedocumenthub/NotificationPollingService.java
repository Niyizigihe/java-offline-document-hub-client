package com.example.offlinedocumenthub;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class NotificationPollingService {
    private static NotificationPollingService instance;
    private Timeline notificationTimeline;
    private Set<Integer> shownNotificationIds = new HashSet<>();
    private boolean isRunning = false;

    private NotificationPollingService() {
        // Private constructor for singleton
    }

    public static NotificationPollingService getInstance() {
        if (instance == null) {
            instance = new NotificationPollingService();
        }
        return instance;
    }

    public void startPolling() {
        if (isRunning) {
            return;
        }

        System.out.println("🔔 Starting notification polling service...");
        isRunning = true;

        notificationTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> checkForNewNotifications())
        );
        notificationTimeline.setCycleCount(Timeline.INDEFINITE);
        notificationTimeline.play();
    }

    public void stopPolling() {
        if (notificationTimeline != null) {
            notificationTimeline.stop();
            notificationTimeline = null;
        }
        isRunning = false;
        shownNotificationIds.clear();
        System.out.println("🔔 Stopped notification polling service");
    }

    private void checkForNewNotifications() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isAdmin()) {
            return;
        }

        new Thread(() -> {
            try {
                List<Map<String, Object>> notifications = ApiClient.getNotifications();

                Platform.runLater(() -> {
                    if (notifications != null && !notifications.isEmpty()) {
                        processNotifications(notifications);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error checking notifications: " + e.getMessage());
            }
        }).start();
    }

    private void processNotifications(List<Map<String, Object>> notifications) {
        for (Map<String, Object> notification : notifications) {
            Integer notificationId = (Integer) notification.get("id");
            String title = (String) notification.get("title");
            String message = (String) notification.get("message");
            Boolean isRead = (Boolean) notification.get("isRead");

            // Only show unread notifications we haven't shown before
            if (notificationId != null && !shownNotificationIds.contains(notificationId) &&
                    (isRead == null || !isRead)) {

                showNotificationPopup(title, message, notificationId);
                shownNotificationIds.add(notificationId);

                // Mark as read on server
                markNotificationAsRead(notificationId);
            }
        }
    }

private void showNotificationPopup(String title, String message, Integer notificationId) {
    Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("System Notification");
        alert.setHeaderText(title);
        alert.setContentText(message);

        // Style the alert
        alert.getDialogPane().setStyle("-fx-border-color: #3498db; -fx-border-width: 2;");

        // Position in top-right corner
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMaxX() - 450);
        stage.setY(50);

        // Add auto-close timer
        Timeline autoCloseTimeline = new Timeline(
                new KeyFrame(Duration.seconds(8), e -> {
                    if (alert.isShowing()) {
                        alert.close();
                    }
                })
        );
        autoCloseTimeline.play();

        // Show the alert
        alert.show();

        System.out.println("🔔 Showing notification: " + title);

        // Bring to front
        stage.toFront();
    });
}
    private void markNotificationAsRead(int notificationId) {
        new Thread(() -> {
            try {
                // This method needs to be added to ApiClient
                ApiClient.markNotificationAsRead(notificationId);
            } catch (Exception e) {
                System.err.println("Error marking notification as read: " + e.getMessage());
            }
        }).start();
    }

    public boolean isRunning() {
        return isRunning;
    }
}