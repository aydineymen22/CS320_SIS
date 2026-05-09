package com.sis.instructor.repository;

public interface InstructorAssignmentRepository {
    boolean isInstructorAssignedToSection(Long instructorId, Long sectionId);
}
