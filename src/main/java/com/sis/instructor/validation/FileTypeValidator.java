package com.sis.instructor.validation;

import java.util.Locale;
import java.util.Set;

public class FileTypeValidator {
    private static final String PDF = "PDF";
    private static final String DOCX = "DOCX";
    private static final String PDF_MIME = "application/pdf";
    private static final String DOCX_MIME =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("pdf", "docx");

    public boolean isSupportedSyllabusType(String fileName, String contentType) {
        return !detectFileType(fileName, contentType).isBlank();
    }

    public String detectFileType(String fileName, String contentType) {
        String normalizedContentType = normalize(contentType);
        if (PDF_MIME.equals(normalizedContentType)) {
            return PDF;
        }
        if (DOCX_MIME.equals(normalizedContentType)) {
            return DOCX;
        }

        String extension = extractExtension(fileName);
        if ("pdf".equals(extension)) {
            return PDF;
        }
        if ("docx".equals(extension)) {
            return DOCX;
        }
        return "";
    }

    public String resolveMimeType(String fileName, String contentType) {
        String normalizedContentType = normalize(contentType);
        if (!normalizedContentType.isBlank()) {
            return normalizedContentType;
        }

        String extension = extractExtension(fileName);
        if ("pdf".equals(extension)) {
            return PDF_MIME;
        }
        if ("docx".equals(extension)) {
            return DOCX_MIME;
        }
        return "";
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank() || !fileName.contains(".")) {
            return "";
        }

        int extensionSeparator = fileName.lastIndexOf('.') + 1;
        String extension = fileName.substring(extensionSeparator).trim().toLowerCase(Locale.ROOT);
        if (SUPPORTED_EXTENSIONS.contains(extension)) {
            return extension;
        }
        return "";
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
