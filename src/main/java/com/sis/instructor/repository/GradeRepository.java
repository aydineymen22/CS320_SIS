package com.sis.instructor.repository;

import java.util.Set;

public interface GradeRepository {
    boolean isStudentRegisteredInSection(Long studentId, Long sectionId);

    RepositoryChangeType saveOrUpdateGrade(Long studentId, Long sectionId, String gradeCode);

    Set<String> findValidGradeCodes();
}
