package com.sis.instructor.service;

import com.sis.instructor.validation.GradeValidator;
import com.sis.instructor.store.InstructorDataStore;

public class GradeService {
    private final AuthorizationService authorizationService;
    private final GradeValidator gradeValidator;
    private final InstructorDataStore dataStore;

    public GradeService(
            AuthorizationService authorizationService,
            GradeValidator gradeValidator,
            InstructorDataStore dataStore) {
        this.authorizationService = authorizationService;
        this.gradeValidator = gradeValidator;
        this.dataStore = dataStore;
    }

    public String saveOrUpdateGrade(
            Long instructorId,
            Long studentId,
            Long courseId,
            String letterGrade) {
        if (!authorizationService.isInstructorAssignedToCourse(instructorId, courseId)) {
            return "Access denied for selected course.";
        }
        if (!dataStore.isStudentRegisteredInSection(studentId, courseId)) {
            return "Student is not enrolled in selected course.";
        }
        if (!gradeValidator.isValidLetterGrade(letterGrade)) {
            return "Invalid letter grade.";
        }

        String normalizedGrade = gradeValidator.normalizeLetterGrade(letterGrade);
        InstructorDataStore.ChangeType changeType =
                dataStore.saveOrUpdateGrade(studentId, courseId, normalizedGrade);

        if (changeType == InstructorDataStore.ChangeType.CREATED) {
            return "Success: Grade recorded for student ID " + studentId + ".";
        }
        return "Success: Grade updated for student ID " + studentId + ".";
    }
}
