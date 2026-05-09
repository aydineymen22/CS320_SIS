package com.sis.admin.repository;

import com.sis.common.DatabaseManager;
import java.sql.*;

public class CourseRepository {

    public boolean existsByCodeOrName(String courseCode, String courseName) {
        String query = "SELECT 1 FROM course_catalog WHERE course_code = ? OR course_name = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, courseName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public Long saveCourse(String courseCode, String courseName, int quota) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            String catalogSql = "INSERT INTO course_catalog (course_code, course_name) VALUES (?, ?)";
            PreparedStatement catalogStmt = conn.prepareStatement(catalogSql, Statement.RETURN_GENERATED_KEYS);
            catalogStmt.setString(1, courseCode);
            catalogStmt.setString(2, courseName);
            catalogStmt.executeUpdate();

            ResultSet catalogKeys = catalogStmt.getGeneratedKeys();
            catalogKeys.next();
            long catalogId = catalogKeys.getLong(1);

            String sectionSql = "INSERT INTO course_sections (catalog_course_id, term_id, section_no, quota, is_open) VALUES (?, 1, '1', ?, TRUE)";
            PreparedStatement sectionStmt = conn.prepareStatement(sectionSql, Statement.RETURN_GENERATED_KEYS);
            sectionStmt.setLong(1, catalogId);
            sectionStmt.setInt(2, quota);
            sectionStmt.executeUpdate();

            ResultSet sectionKeys = sectionStmt.getGeneratedKeys();
            sectionKeys.next();
            long sectionId = sectionKeys.getLong(1);

            conn.commit();
            return sectionId;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
            if (conn != null) conn.close();
        }
    }

    public boolean updateCourseQuota(Long sectionId, int newQuota) {
        String query = "UPDATE course_sections SET quota = ? WHERE section_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, newQuota);
            stmt.setLong(2, sectionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean assignInstructor(Long sectionId, Long instructorId) {
        String query = "REPLACE INTO section_instructors (section_id, instructor_id) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, sectionId);
            stmt.setLong(2, instructorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeInstructor(Long sectionId) {
        String query = "DELETE FROM section_instructors WHERE section_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, sectionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}