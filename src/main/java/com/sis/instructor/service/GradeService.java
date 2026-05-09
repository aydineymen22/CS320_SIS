package com.sis.instructor.service;

import com.sis.instructor.repository.GradeRepository;
import com.sis.instructor.repository.JdbcGradeRepository;
import com.sis.instructor.repository.RepositoryChangeType;
import com.sis.instructor.validation.GradeValidator;

public class GradeService {
    private final AuthorizationService authorizationService;
    private final GradeValidator gradeValidator;
    private final GradeRepository gradeRepository;

    public GradeService() {
        this(new AuthorizationService(), new GradeValidator(), new JdbcGradeRepository());
    }

    public GradeService(
            AuthorizationService authorizationService,
            GradeValidator gradeValidator,
            GradeRepository gradeRepository) {
        this.authorizationService = authorizationService;
        this.gradeValidator = gradeValidator;
        this.gradeRepository = gradeRepository;
    }

    public String saveOrUpdateGrade(
            Long instructorId,
            Long studentId,
            Long courseId,
            String letterGrade) {
        if (!authorizationService.isInstructorAssignedToCourse(instructorId, courseId)) {
            return "Access denied for selected course.";
        }
        if (!gradeRepository.isStudentRegisteredInSection(studentId, courseId)) {
            return "Student is not enrolled in selected course.";
        }
        if (!gradeValidator.isValidLetterGrade(letterGrade)) {
            return "Invalid letter grade.";
        }

        String normalizedGrade = gradeValidator.normalizeLetterGrade(letterGrade);
        RepositoryChangeType changeType =
                gradeRepository.saveOrUpdateGrade(studentId, courseId, normalizedGrade);

        if (changeType == RepositoryChangeType.CREATED) {
            return "Success: Grade recorded for student ID " + studentId + ".";
        }
        return "Success: Grade updated for student ID " + studentId + ".";
    }
}
