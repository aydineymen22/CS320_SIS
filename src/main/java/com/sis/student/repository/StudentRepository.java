package com.sis.student.repository;

import com.sis.common.DatabaseManager;
import com.sis.common.model.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {

    public List<Course> findCourses(String query) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course_listing_view WHERE course_name LIKE ? OR course_code LIKE ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + (query == null ? "" : query) + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                courses.add(new Course(
                        rs.getLong("section_id"), rs.getString("course_code"),
                        rs.getString("course_name"), rs.getString("section_no"),
                        rs.getString("term_name"), rs.getInt("available_quota"),
                        rs.getString("instructor_name")
                ));
            }
        }
        return courses;
    }

    public String getEnrollmentStatus(Long studentId, Long sectionId) throws SQLException {
        String sql = "SELECT status FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, sectionId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("status") : null;
        }
    }

    public void insertEnrollment(Long studentId, Long sectionId) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'ENROLLED')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, sectionId);
            ps.executeUpdate();
        }
    }

    public int updateEnrollmentStatus(Long studentId, Long sectionId, String status) throws SQLException {
        String sql = "UPDATE enrollments SET status = ? WHERE student_id = ? AND section_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, studentId);
            ps.setLong(3, sectionId);
            return ps.executeUpdate();
        }
    }

    public ResultSet getTranscriptData(Long studentId) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        String sql = "SELECT * FROM student_transcript_view WHERE student_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, studentId);
        return ps.executeQuery();
    }
    public List<Course> findEnrolledCourses(Long studentId) throws SQLException {
        List<Course> myCourses = new ArrayList<>();
        String sql = "SELECT v.* FROM course_listing_view v " +
                "JOIN enrollments e ON v.section_id = e.section_id " +
                "WHERE e.student_id = ? AND e.status = 'ENROLLED'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                myCourses.add(new Course(
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
        return myCourses;
    }


}