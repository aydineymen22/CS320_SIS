package com.sis.instructor.service;

import java.util.List;

public class RosterService {
    private final AuthorizationService authorizationService;

    public RosterService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public List<String> getRoster(Long instructorId, Long courseId) {
        if (!authorizationService.isInstructorAssignedToCourse(instructorId, courseId)) {
            return List.of();
        }
        return List.of();
    }
}
