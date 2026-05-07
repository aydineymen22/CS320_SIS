package com.sis.instructor.service;

import com.sis.instructor.store.InstructorDataStore;
import com.sis.instructor.validation.GradeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GradeServiceTest {
    private InstructorDataStore dataStore;
    private GradeService gradeService;

    @BeforeEach
    void setUp() {
        dataStore = new InstructorDataStore();
        AuthorizationService authorizationService = new AuthorizationService(dataStore);
        GradeValidator gradeValidator = new GradeValidator(dataStore.getValidGradeCodes());
        gradeService = new GradeService(authorizationService, gradeValidator, dataStore);
    }

    @Test
    void saveOrUpdateGrade_WhenNewGradeIsValid_ShouldCreateGrade() {
        String result = gradeService.saveOrUpdateGrade(3L, 1L, 1L, "a-");

        assertEquals("Success: Grade recorded for student ID 1.", result);
        assertTrue(dataStore.findGradeCode(1L, 1L).isPresent());
        assertEquals("A-", dataStore.findGradeCode(1L, 1L).orElseThrow());
    }

    @Test
    void saveOrUpdateGrade_WhenStudentIsNotInCourse_ShouldReturnEnrollmentError() {
        String result = gradeService.saveOrUpdateGrade(3L, 2L, 2L, "A");

        assertEquals("Student is not enrolled in selected course.", result);
    }

    @Test
    void saveOrUpdateGrade_WhenGradeIsInvalid_ShouldReturnValidationError() {
        String result = gradeService.saveOrUpdateGrade(3L, 1L, 1L, "F");

        assertEquals("Invalid letter grade.", result);
    }
}
