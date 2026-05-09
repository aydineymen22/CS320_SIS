package com.sis.instructor.service;

import com.sis.instructor.support.InstructorRepositoryFakes;
import com.sis.instructor.validation.GradeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GradeServiceTest {
    private InstructorRepositoryFakes repositories;
    private GradeService gradeService;

    @BeforeEach
    void setUp() {
        repositories = new InstructorRepositoryFakes();
        AuthorizationService authorizationService = new AuthorizationService(repositories);
        GradeValidator gradeValidator = new GradeValidator(repositories.findValidGradeCodes());
        gradeService = new GradeService(authorizationService, gradeValidator, repositories);
    }

    @Test
    void saveOrUpdateGrade_WhenNewGradeIsValid_ShouldCreateGrade() {
        String result = gradeService.saveOrUpdateGrade(3L, 1L, 1L, "a-");

        assertEquals("Success: Grade recorded for student ID 1.", result);
        assertTrue(repositories.findGradeCode(1L, 1L).isPresent());
        assertEquals("A-", repositories.findGradeCode(1L, 1L).orElseThrow());
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
