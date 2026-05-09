package com.sis.instructor.service;

import com.sis.instructor.repository.JdbcSyllabusRepository;
import com.sis.instructor.repository.RepositoryChangeType;
import com.sis.instructor.repository.SyllabusRepository;
import com.sis.instructor.validation.FileTypeValidator;
import java.io.File;
import java.nio.file.Files;

public class SyllabusService {
    private final AuthorizationService authorizationService;
    private final FileTypeValidator fileTypeValidator;
    private final SyllabusRepository syllabusRepository;

    public SyllabusService() {
        this(new AuthorizationService(), new FileTypeValidator(), new JdbcSyllabusRepository());
    }

    public SyllabusService(
            AuthorizationService authorizationService,
            FileTypeValidator fileTypeValidator,
            SyllabusRepository syllabusRepository) {
        this.authorizationService = authorizationService;
        this.fileTypeValidator = fileTypeValidator;
        this.syllabusRepository = syllabusRepository;
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
        String fileName = syllabusFile.getName().trim();
        String filePath = "/uploads/syllabuses/" + fileName;
        RepositoryChangeType changeType =
                syllabusRepository.saveOrReplaceSyllabus(
                        courseId,
                        fileName,
                        fileType,
                        mimeType,
                        filePath);

        if (changeType == RepositoryChangeType.CREATED) {
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
