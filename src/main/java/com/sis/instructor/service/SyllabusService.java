package com.sis.instructor.service;

import com.sis.instructor.validation.FileTypeValidator;
import java.io.File;

public class SyllabusService {
    private final AuthorizationService authorizationService;
    private final FileTypeValidator fileTypeValidator;

    public SyllabusService(
            AuthorizationService authorizationService,
            FileTypeValidator fileTypeValidator) {
        this.authorizationService = authorizationService;
        this.fileTypeValidator = fileTypeValidator;
    }

    public String uploadOrReplaceSyllabus(Long instructorId, Long courseId, File syllabusFile) {
        if (!authorizationService.isInstructorAssignedToCourse(instructorId, courseId)) {
            return "Access denied for selected course.";
        }
        if (!fileTypeValidator.isSupportedSyllabusType(syllabusFile.getName(), "")) {
            return "Unsupported syllabus type.";
        }
        return "TODO: implement syllabus upload.";
    }
}
