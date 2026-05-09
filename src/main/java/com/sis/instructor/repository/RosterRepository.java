package com.sis.instructor.repository;

import com.sis.instructor.model.RosterStudent;
import java.util.List;

public interface RosterRepository {
    List<RosterStudent> findEnrolledStudentsBySection(Long sectionId);
}
