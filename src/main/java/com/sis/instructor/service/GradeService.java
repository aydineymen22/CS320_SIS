package com.sis.instructor.service;

import com.sis.instructor.validation.GradeValidator;

public class GradeService {
    private final AuthorizationService authorizationService;
    private final GradeValidator gradeValidator;

    public GradeService(
            AuthorizationService authorizationService,
            GradeValidator gradeValidator) {
        this.authorizationService = authorizationService;
        this.gradeValidator = gradeValidator;
    }

    public String saveOrUpdateGrade(
            Long instructorId,
            Long studentId,
            Long courseId,
            String letterGrade) {
        if (!authorizationService.isInstructorAssignedToCourse(instructorId, courseId)) {
            return "Access denied for selected course.";
        }
        if (!gradeValidator.isValidLetterGrade(letterGrade)) {
            return "Invalid letter grade.";
        }
        return "TODO: implement grade save/update.";
    }
}
