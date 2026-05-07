package com.sis.instructor.controller;

import com.sis.instructor.model.RosterStudent;
import com.sis.instructor.service.AuthorizationService;
import com.sis.instructor.service.GradeService;
import com.sis.instructor.service.RosterService;
import com.sis.instructor.service.SyllabusService;
import com.sis.instructor.store.InstructorDataStore;
import com.sis.instructor.validation.FileTypeValidator;
import com.sis.instructor.validation.GradeValidator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstructorControllerTest {
    private InstructorController controller;

    @BeforeEach
    void setUp() {
        InstructorDataStore dataStore = new InstructorDataStore();
        AuthorizationService authorizationService = new AuthorizationService(dataStore);
        GradeValidator gradeValidator = new GradeValidator(dataStore.getValidGradeCodes());
        FileTypeValidator fileTypeValidator = new FileTypeValidator();

        controller = new InstructorController(
                new SyllabusService(authorizationService, fileTypeValidator, dataStore),
                new RosterService(authorizationService, dataStore),
                new GradeService(authorizationService, gradeValidator, dataStore));
    }

    @Test
    void viewRoster_WithNullIds_ShouldReturnEmptyList() {
        assertTrue(controller.viewRoster(null, 1L).isEmpty());
    }

    @Test
    void viewRoster_ForAssignedCourse_ShouldReturnStructuredStudents() {
        List<RosterStudent> roster = controller.viewRoster(3L, 1L);

        assertEquals(2, roster.size());
        assertEquals("Zeynep", roster.get(0).getFirstName());
        assertEquals("20230001", roster.get(1).getStudentNumber());
    }
}
