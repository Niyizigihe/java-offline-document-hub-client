package com.example.offlinedocumenthub;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityLog {
    private int logId;
    private int userId;
    private String userDisplayName;
    private String actionType;
    private String actionDetails;
    private LocalDateTime timestamp;
    private String formattedTimestamp;

    // Default constructor for JSON deserialization
    public ActivityLog() {}

    public ActivityLog(int logId, int userId, String userDisplayName, String actionType,
                       String actionDetails, LocalDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.userDisplayName = userDisplayName;
        this.actionType = actionType;
        this.actionDetails = actionDetails;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserDisplayName() { return userDisplayName; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getActionDetails() { return actionDetails; }
    public void setActionDetails(String actionDetails) { this.actionDetails = actionDetails; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // Formatted timestamp for display
//    public String getFormattedTimestamp() {
//        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//    }
    public String getFormattedTimestamp() {
        if (formattedTimestamp != null) {
            return formattedTimestamp;
        }
        return timestamp != null ?
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
    }

    public void setFormattedTimestamp(String formattedTimestamp) {
        this.formattedTimestamp = formattedTimestamp;
    }
}