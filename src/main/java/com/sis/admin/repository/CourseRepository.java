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

    public boolean updateCourseDetails(Long sectionId, String newName, int newQuota) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            Long catalogCourseId = null;
            String findCatalogSql = "SELECT catalog_course_id FROM course_sections WHERE section_id = ?";
            try (PreparedStatement findStmt = conn.prepareStatement(findCatalogSql)) {
                findStmt.setLong(1, sectionId);
                ResultSet rs = findStmt.executeQuery();
                if (rs.next()) {
                    catalogCourseId = rs.getLong("catalog_course_id");
                }
            }

            if (catalogCourseId == null) {
                conn.rollback();
                return false;
            }

            String updateCatalogSql = "UPDATE course_catalog SET course_name = ? WHERE catalog_course_id = ?";
            try (PreparedStatement catalogStmt = conn.prepareStatement(updateCatalogSql)) {
                catalogStmt.setString(1, newName);
                catalogStmt.setLong(2, catalogCourseId);
                if (catalogStmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            String updateSectionSql = "UPDATE course_sections SET quota = ? WHERE section_id = ?";
            try (PreparedStatement sectionStmt = conn.prepareStatement(updateSectionSql)) {
                sectionStmt.setInt(1, newQuota);
                sectionStmt.setLong(2, sectionId);
                if (sectionStmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeError) {
                    closeError.printStackTrace();
                }
            }
        }
    }

    public boolean assignInstructor(Long sectionId, Long instructorId) {
        String query = "REPLACE INTO section_instructors (section_id, instructor_id) " +
                "SELECT ?, ? " +
                "WHERE EXISTS (SELECT 1 FROM course_sections WHERE section_id = ?) " +
                "AND EXISTS (SELECT 1 FROM instructors WHERE instructor_id = ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, sectionId);
            stmt.setLong(2, instructorId);
            stmt.setLong(3, sectionId);
            stmt.setLong(4, instructorId);
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
