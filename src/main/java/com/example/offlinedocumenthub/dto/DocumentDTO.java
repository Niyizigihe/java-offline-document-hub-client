package com.example.offlinedocumenthub.dto;

import java.time.LocalDate;

public class DocumentDTO {
    private int docId;
    private String title;
    private String filePath;
    private String uploadedBy;
    private LocalDate uploadDate;
    private String fileSize;

    public DocumentDTO() {}

    public DocumentDTO(int docId, String title, String filePath, String uploadedBy, LocalDate uploadDate) {
        this.docId = docId;
        this.title = title;
        this.filePath = filePath;
        this.uploadedBy = uploadedBy;
        this.uploadDate = uploadDate;
    }

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

    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }
}