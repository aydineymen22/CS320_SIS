package com.sis.instructor.service;

import com.sis.instructor.model.RosterStudent;
import com.sis.instructor.store.InstructorDataStore;
import java.util.List;

public class RosterService {
    private final AuthorizationService authorizationService;
    private final InstructorDataStore dataStore;

    public RosterService(
            AuthorizationService authorizationService,
            InstructorDataStore dataStore) {
        this.authorizationService = authorizationService;
        this.dataStore = dataStore;
    }

    public List<RosterStudent> getRoster(Long instructorId, Long courseId) {
        if (!authorizationService.isInstructorAssignedToCourse(instructorId, courseId)) {
            return List.of();
        }
        return dataStore.getRosterForSection(courseId);
    }
}
