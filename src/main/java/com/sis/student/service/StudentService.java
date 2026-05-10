package com.sis.student.service;

import com.sis.common.model.Course;
import com.sis.common.model.Transcript;
import com.sis.student.repository.StudentRepository;
import java.sql.SQLException;
import java.util.List;

public class StudentService {
    private final StudentRepository repository = new StudentRepository();

    public String addCourse(Long studentId, Long sectionId) {
        try {
            List<Course> all = repository.findCourses("");
            Course target = all.stream()
                    .filter(c -> c.getSectionId().equals(sectionId))
                    .findFirst().orElse(null);

            if (target == null) return "Error: Course not found.";
            if (target.getAvailableQuota() <= 0) return "Error: No quota left.";

            String currentStatus = repository.getEnrollmentStatus(studentId, sectionId);

            if ("ENROLLED".equals(currentStatus)) {
                return "Error: Already in your schedule.";
            } else if ("DROPPED".equals(currentStatus)) {
                repository.updateEnrollmentStatus(studentId, sectionId, "ENROLLED");
                return "Successfully enrolled in " + target.getCourseName();
            } else {
                repository.insertEnrollment(studentId, sectionId);
                return "Successfully enrolled in " + target.getCourseName();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Database failure.";
        }
    }

    public String dropCourse(Long studentId, Long sectionId) {
        try {
            String status = repository.getEnrollmentStatus(studentId, sectionId);
            if (status == null || "DROPPED".equals(status)) return "Error: Enrollment not found.";

            repository.updateEnrollmentStatus(studentId, sectionId, "DROPPED");
            return "Successfully dropped.";
        } catch (SQLException e) {
            return "Error: Database failure.";
        }
    }

    public List<Course> viewAvailableCourses() {
        try {
            return repository.findCourses("");
        } catch (SQLException e) {
            return List.of();
        }
    }

    public List<Course> searchCourses(String query) {
        try {
            return repository.findCourses(query);
        } catch (SQLException e) {
            return List.of();
        }
    }

    public Transcript viewTranscript(Long studentId) {
        Transcript transcript = new Transcript(studentId);
        try {
            repository.fillTranscript(transcript, studentId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transcript;
    }

    public List<Course> getMySchedule(Long studentId) {
        try {
            return repository.findEnrolledCourses(studentId);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public Course viewCourseDetails(Long sectionId) {
        try {
            return repository.findCourseBySectionId(sectionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
