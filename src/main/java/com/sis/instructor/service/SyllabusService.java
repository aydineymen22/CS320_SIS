package com.sis.instructor.service;

import com.sis.instructor.store.InstructorDataStore;
import com.sis.instructor.validation.FileTypeValidator;
import java.io.File;
import java.nio.file.Files;

public class SyllabusService {
    private final AuthorizationService authorizationService;
    private final FileTypeValidator fileTypeValidator;
    private final InstructorDataStore dataStore;

    public SyllabusService(
            AuthorizationService authorizationService,
            FileTypeValidator fileTypeValidator,
            InstructorDataStore dataStore) {
        this.authorizationService = authorizationService;
        this.fileTypeValidator = fileTypeValidator;
        this.dataStore = dataStore;
    }

    public String uploadOrReplaceSyllabus(Long instructorId, Long courseId, File syllabusFile) {
        if (!authorizationService.isInstructorAssignedToCourse(instructorId, courseId)) {
            return "Access denied for selected course.";
        }

        String contentType = resolveContentType(syllabusFile);
        if (!fileTypeValidator.isSupportedSyllabusType(syllabusFile.getName(), contentType)) {
            return "Unsupported syllabus type.";
        }

        String fileType = fileTypeValidator.detectFileType(syllabusFile.getName(), contentType);
        String mimeType = fileTypeValidator.resolveMimeType(syllabusFile.getName(), contentType);
        InstructorDataStore.ChangeType changeType =
                dataStore.saveOrReplaceSyllabus(courseId, syllabusFile, fileType, mimeType);

        if (changeType == InstructorDataStore.ChangeType.CREATED) {
            return "Success: Syllabus uploaded for course ID " + courseId + ".";
        }
        return "Success: Syllabus replaced for course ID " + courseId + ".";
    }

    private String resolveContentType(File syllabusFile) {
        try {
            if (syllabusFile.exists()) {
                return Files.probeContentType(syllabusFile.toPath());
            }
        } catch (Exception ignored) {
            // Fallback to extension-based validation below.
        }
        return "";
    }
}
