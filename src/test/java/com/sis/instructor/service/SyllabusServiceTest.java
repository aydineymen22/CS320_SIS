package com.sis.instructor.service;

import com.sis.instructor.model.SyllabusMetadata;
import com.sis.instructor.store.InstructorDataStore;
import com.sis.instructor.validation.FileTypeValidator;
import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SyllabusServiceTest {
    private InstructorDataStore dataStore;
    private SyllabusService syllabusService;

    @BeforeEach
    void setUp() {
        dataStore = new InstructorDataStore();
        AuthorizationService authorizationService = new AuthorizationService(dataStore);
        syllabusService = new SyllabusService(
                authorizationService,
                new FileTypeValidator(),
                dataStore);
    }

    @Test
    void uploadOrReplaceSyllabus_WhenTypeIsSupported_ShouldReplaceExistingRecord() {
        String result = syllabusService.uploadOrReplaceSyllabus(
                3L,
                1L,
                new File("cs320_revised_syllabus.pdf"));

        assertEquals("Success: Syllabus replaced for course ID 1.", result);

        SyllabusMetadata metadata = dataStore.findSyllabus(1L).orElseThrow();
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
        assertTrue(dataStore.findSyllabus(1L).isPresent());
    }
}
