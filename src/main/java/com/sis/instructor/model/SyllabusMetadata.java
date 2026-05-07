package com.sis.instructor.model;

import java.time.LocalDateTime;

public class SyllabusMetadata {
    private final Long sectionId;
    private final String fileName;
    private final String fileType;
    private final String mimeType;
    private final String filePath;
    private final LocalDateTime uploadedAt;

    public SyllabusMetadata(
            Long sectionId,
            String fileName,
            String fileType,
            String mimeType,
            String filePath,
            LocalDateTime uploadedAt) {
        this.sectionId = sectionId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFilePath() {
        return filePath;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
