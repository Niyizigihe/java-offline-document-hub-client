package com.example.offlinedocumenthub;

import com.fasterxml.jackson.annotation.JsonSetter;

import java.time.LocalDate;

public class Document {
    private int docId;
    private String title;
    private String filePath;
    private String uploadedBy;
    private LocalDate uploadDate;
    private int userId;
    private String fileSize;

    // Default constructor for JSON deserialization
    public Document() {}

    // Getters and setters
    public int getDocId() { return docId; }
    public void setDocId(int docId) { this.docId = docId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDate getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDate uploadDate) { this.uploadDate = uploadDate; }

    // Handle String dates during JSON deserialization
    @JsonSetter("uploadDate")
    public void setUploadDate(String dateString) {
        if (dateString != null && !dateString.isEmpty()) {
            this.uploadDate = LocalDate.parse(dateString);
        }
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }

    @JsonSetter("fileSize")
    public void setFileSize(Object fileSizeObj) {
        if (fileSizeObj instanceof String) {
            this.fileSize = (String) fileSizeObj;
        } else if (fileSizeObj instanceof Number) {
            long bytes = ((Number) fileSizeObj).longValue();
            // Format the file size for display
            if (bytes == 0) {
                this.fileSize = "0 B";
            } else if (bytes < 1024) {
                this.fileSize = bytes + " B";
            } else if (bytes < 1024 * 1024) {
                this.fileSize = String.format("%.1f KB", bytes / 1024.0);
            } else {
                this.fileSize = String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            }
        } else {
            this.fileSize = "0 B";
        }
    }
}