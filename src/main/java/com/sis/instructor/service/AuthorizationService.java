package com.sis.instructor.service;

import com.sis.instructor.repository.InstructorAssignmentRepository;
import com.sis.instructor.repository.JdbcInstructorAssignmentRepository;

public class AuthorizationService {
    private final InstructorAssignmentRepository instructorAssignmentRepository;

    public AuthorizationService() {
        this(new JdbcInstructorAssignmentRepository());
    }

    public AuthorizationService(InstructorAssignmentRepository instructorAssignmentRepository) {
        this.instructorAssignmentRepository = instructorAssignmentRepository;
    }

    public boolean isInstructorAssignedToCourse(Long instructorId, Long courseId) {
        if (instructorId == null || courseId == null) {
            return false;
        }
        return instructorAssignmentRepository.isInstructorAssignedToSection(instructorId, courseId);
    }
}
