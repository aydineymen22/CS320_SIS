package com.sis.student.service;

import com.sis.student.api.StudentServiceAPI;
import com.sis.student.repository.StudentRepository;
import com.sis.common.model.Course;
import com.sis.common.model.Transcript;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class StudentService implements StudentServiceAPI {
    private final StudentRepository repository = new StudentRepository();

    @Override
    public List<Course> viewAvailableCourses() {
        return searchCourses("");
    }

    @Override
    public List<Course> searchCourses(String query) {
        try {
            return repository.findCourses(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public String addCourse(Long studentId, Long sectionId) {
        try {
            List<Course> all = repository.findCourses("");
            Course target = all.stream()
                    .filter(c -> c.getSectionId().equals(sectionId))
                    .findFirst().orElse(null);

            if (target == null) return "Error: Section not found.";
            if (target.getAvailableQuota() <= 0) return "Error: No quota left.";

            String status = repository.getEnrollmentStatus(studentId, sectionId);
            if (status == null) {
                repository.insertEnrollment(studentId, sectionId);
            } else if (status.equals("DROPPED")) {
                repository.updateEnrollmentStatus(studentId, sectionId, "ENROLLED");
            } else {
                return "Error: Already enrolled in this section.";
            }
            return "Successfully enrolled.";

        } catch (SQLException e) {
            return "Error: Database failure.";
        }
    }

    @Override
    public String dropCourse(Long studentId, Long sectionId) {
        try {
            int rows = repository.updateEnrollmentStatus(studentId, sectionId, "DROPPED");
            return rows > 0 ? "Successfully dropped." : "Error: Enrollment not found.";
        } catch (SQLException e) {
            return "Error: Drop failed.";
        }
    }

    @Override
    public Transcript viewTranscript(Long studentId) {
        Transcript transcript = new Transcript(studentId); // Now matches 1-arg constructor
        try (java.sql.ResultSet rs = repository.getTranscriptData(studentId)) {
            while (rs.next()) {
                Course c = new Course();
                c.setCourseCode(rs.getString("course_code"));
                c.setCourseName(rs.getString("course_name"));

                transcript.addCourse(c, rs.getString("grade_code"));
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Transcript Error: " + e.getMessage());
        }
        return transcript;
    }
}