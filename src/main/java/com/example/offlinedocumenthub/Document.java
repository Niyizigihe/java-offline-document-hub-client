package com.example.offlinedocumenthub;

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

    public Document(int docId, String title, String filePath, String uploadedBy, LocalDate uploadDate) {
        this(docId, title, filePath, uploadedBy, uploadDate, -1);
    }

    public Document(int docId, String title, String filePath, String uploadedBy, LocalDate uploadDate, int userId) {
        this.docId = docId;
        this.title = title;
        this.filePath = filePath;
        this.uploadedBy = uploadedBy;
        this.uploadDate = uploadDate;
        this.userId = userId;
    }

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

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }
    public void setFileSize(long fileSizeBytes) {
        // Convert bytes to human readable format
        if (fileSizeBytes < 1024) {
            this.fileSize = fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            this.fileSize = String.format("%.1f KB", fileSizeBytes / 1024.0);
        } else {
            this.fileSize = String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
        }
    }
}