package com.sis.instructor.service;

import com.sis.instructor.model.RosterStudent;
import com.sis.instructor.repository.JdbcRosterRepository;
import com.sis.instructor.repository.RosterRepository;
import java.util.List;

public class RosterService {
    private final AuthorizationService authorizationService;
    private final RosterRepository rosterRepository;

    public RosterService() {
        this(new AuthorizationService(), new JdbcRosterRepository());
    }

    public RosterService(
            AuthorizationService authorizationService,
            RosterRepository rosterRepository) {
        this.authorizationService = authorizationService;
        this.rosterRepository = rosterRepository;
    }

    public List<RosterStudent> getRoster(Long instructorId, Long courseId) {
        if (!authorizationService.isInstructorAssignedToCourse(instructorId, courseId)) {
            return List.of();
        }
        return rosterRepository.findEnrolledStudentsBySection(courseId);
    }
}
