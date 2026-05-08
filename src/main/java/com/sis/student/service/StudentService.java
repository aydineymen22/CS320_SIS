package com.sis.student.service;

import com.sis.student.api.StudentServiceAPI;
import com.sis.student.model.Course;
import com.sis.student.model.Transcript;
import com.sis.student.util.DatabaseManager;

import java.sql.*;
import java.util.*;

public class StudentService implements StudentServiceAPI {

    @Override
    public List<Course> viewAvailableCourses() {
        return searchCourses("");
    }

    @Override
    public List<Course> searchCourses(String query) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course_listing_view WHERE course_name LIKE ? OR course_code LIKE ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + query + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(new Course(
                            rs.getLong("section_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("section_no"),
                            rs.getString("term_name"),
                            rs.getInt("available_quota"),
                            rs.getString("instructor_name")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    @Override
    public String addCourse(Long studentId, Long sectionId) {
        String checkLogicSql = "SELECT cs.is_open, v.available_quota FROM course_sections cs " +
                "JOIN course_listing_view v ON cs.section_id = v.section_id WHERE cs.section_id = ?";

        String checkExistingSql = "SELECT status FROM enrollments WHERE student_id = ? AND section_id = ?";

        String insertSql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'ENROLLED')";
        String updateSql = "UPDATE enrollments SET status = 'ENROLLED' WHERE student_id = ? AND section_id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkLogicSql)) {
                checkStmt.setLong(1, sectionId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) return "Error: Section not found.";
                if (!rs.getBoolean("is_open")) return "Error: Section is closed.";
                if (rs.getInt("available_quota") <= 0) return "Error: No quota left.";
            }

            try (PreparedStatement checkExistStmt = conn.prepareStatement(checkExistingSql)) {
                checkExistStmt.setLong(1, studentId);
                checkExistStmt.setLong(2, sectionId);
                ResultSet rs = checkExistStmt.executeQuery();

                if (rs.next()) {
                    String status = rs.getString("status");
                    if (status.equals("ENROLLED") || status.equals("COMPLETED")) {
                        return "Error: Already enrolled in this section.";
                    }
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setLong(1, studentId);
                        updateStmt.setLong(2, sectionId);
                        updateStmt.executeUpdate();
                        return "Successfully enrolled.";
                    }
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setLong(1, studentId);
                insertStmt.setLong(2, sectionId);
                insertStmt.executeUpdate();
                return "Successfully enrolled.";
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1452) return "Error: Student or Section ID does not exist.";
            e.printStackTrace();
            return "Error: Database failure (" + e.getMessage() + ")";
        }
    }

    @Override
    public String dropCourse(Long studentId, Long sectionId) {
        String sql = "UPDATE enrollments SET status = 'DROPPED' WHERE student_id = ? AND section_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, studentId);
            pstmt.setLong(2, sectionId);
            int rowsUpdated = pstmt.executeUpdate();

            return rowsUpdated > 0 ? "Successfully dropped." : "Error: Enrollment not found.";
        } catch (SQLException e) {
            return "Error: Drop failed.";
        }
    }

    @Override
    public Transcript viewTranscript(Long studentId) {
        List<Course> courses = new ArrayList<>();
        Map<String, String> grades = new HashMap<>();
        String sql = "SELECT * FROM student_transcript_view WHERE student_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Course c = new Course();
                c.setCourseCode(rs.getString("course_code"));
                c.setCourseName(rs.getString("course_name"));
                courses.add(c);
                grades.put(c.getCourseCode(), rs.getString("grade_code"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Transcript(studentId, courses, grades);
    }
}