package com.sis.instructor.service;

import com.sis.instructor.store.InstructorDataStore;

public class AuthorizationService {
    private final InstructorDataStore dataStore;

    public AuthorizationService(InstructorDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public boolean isInstructorAssignedToCourse(Long instructorId, Long courseId) {
        if (instructorId == null || courseId == null) {
            return false;
        }
        return dataStore.isInstructorAssignedToSection(instructorId, courseId);
    }
}
