package com.sis.instructor.service;

import com.sis.instructor.model.SyllabusMetadata;
import com.sis.instructor.support.InstructorRepositoryFakes;
import com.sis.instructor.validation.FileTypeValidator;
import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SyllabusServiceTest {
    private InstructorRepositoryFakes repositories;
    private SyllabusService syllabusService;

    @BeforeEach
    void setUp() {
        repositories = new InstructorRepositoryFakes();
        AuthorizationService authorizationService = new AuthorizationService(repositories);
        syllabusService = new SyllabusService(
                authorizationService,
                new FileTypeValidator(),
                repositories);
    }

    @Test
    void uploadOrReplaceSyllabus_WhenTypeIsSupported_ShouldReplaceExistingRecord() {
        String result = syllabusService.uploadOrReplaceSyllabus(
                3L,
                1L,
                new File("cs320_revised_syllabus.pdf"));

        assertEquals("Success: Syllabus replaced for course ID 1.", result);

        SyllabusMetadata metadata = repositories.findBySectionId(1L).orElseThrow();
        assertEquals("cs320_revised_syllabus.pdf", metadata.getFileName());
        assertEquals("PDF", metadata.getFileType());
    }

    @Test
    void uploadOrReplaceSyllabus_WhenTypeIsUnsupported_ShouldReturnValidationError() {
        String result = syllabusService.uploadOrReplaceSyllabus(
                3L,
                1L,
                new File("notes.txt"));

        assertEquals("Unsupported syllabus type.", result);
        assertTrue(repositories.findBySectionId(1L).isPresent());
    }
}
