package com.sis.instructor.repository;

import com.sis.common.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

public class JdbcGradeRepository implements GradeRepository {
    @Override
    public boolean isStudentRegisteredInSection(Long studentId, Long sectionId) {
        String sql = """
                SELECT 1
                FROM enrollments
                WHERE student_id = ?
                  AND section_id = ?
                  AND status <> 'DROPPED'
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, studentId);
            statement.setLong(2, sectionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (Exception exception) {
            throw new RepositoryException("Failed to check student enrollment.", exception);
        }
    }

    @Override
    public RepositoryChangeType saveOrUpdateGrade(Long studentId, Long sectionId, String gradeCode) {
        try (Connection connection = DatabaseManager.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Long enrollmentId = findEnrollmentIdForUpdate(connection, studentId, sectionId);
                if (enrollmentId == null) {
                    connection.rollback();
                    throw new IllegalArgumentException("Enrollment not found.");
                }

                boolean alreadyExists = gradeExistsForUpdate(connection, enrollmentId);
                upsertGrade(connection, enrollmentId, gradeCode);
                connection.commit();
                return alreadyExists ? RepositoryChangeType.UPDATED : RepositoryChangeType.CREATED;
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RepositoryException("Failed to save grade.", exception);
        }
    }

    @Override
    public Set<String> findValidGradeCodes() {
        String sql = "SELECT grade_code FROM grade_types ORDER BY grade_code";
        Set<String> gradeCodes = new LinkedHashSet<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                gradeCodes.add(resultSet.getString("grade_code"));
            }
        } catch (Exception exception) {
            throw new RepositoryException("Failed to load grade codes.", exception);
        }
        return gradeCodes;
    }

    private Long findEnrollmentIdForUpdate(Connection connection, Long studentId, Long sectionId)
            throws SQLException {
        String sql = """
                SELECT enrollment_id
                FROM enrollments
                WHERE student_id = ?
                  AND section_id = ?
                  AND status <> 'DROPPED'
                FOR UPDATE
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, studentId);
            statement.setLong(2, sectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong("enrollment_id") : null;
            }
        }
    }

    private boolean gradeExistsForUpdate(Connection connection, Long enrollmentId) throws SQLException {
        String sql = "SELECT 1 FROM grades WHERE enrollment_id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, enrollmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void upsertGrade(Connection connection, Long enrollmentId, String gradeCode) throws SQLException {
        String sql = """
                INSERT INTO grades (enrollment_id, grade_code)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE grade_code = VALUES(grade_code)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, enrollmentId);
            statement.setString(2, gradeCode);
            statement.executeUpdate();
        }
    }
}
