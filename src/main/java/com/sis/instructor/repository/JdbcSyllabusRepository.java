package com.sis.instructor.repository;

import com.sis.common.DatabaseManager;
import com.sis.instructor.model.SyllabusMetadata;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class JdbcSyllabusRepository implements SyllabusRepository {
    @Override
    public RepositoryChangeType saveOrReplaceSyllabus(
            Long sectionId,
            String fileName,
            String fileType,
            String mimeType,
            String filePath) {
        try (Connection connection = DatabaseManager.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                boolean alreadyExists = syllabusExistsForUpdate(connection, sectionId);
                upsertSyllabus(connection, sectionId, fileName, fileType, mimeType, filePath);
                connection.commit();
                return alreadyExists ? RepositoryChangeType.UPDATED : RepositoryChangeType.CREATED;
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (Exception exception) {
            throw new RepositoryException("Failed to save syllabus.", exception);
        }
    }

    @Override
    public Optional<SyllabusMetadata> findBySectionId(Long sectionId) {
        String sql = """
                SELECT section_id, file_name, file_type, mime_type, file_path, uploaded_at
                FROM syllabuses
                WHERE section_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sectionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                Timestamp uploadedAt = resultSet.getTimestamp("uploaded_at");
                return Optional.of(new SyllabusMetadata(
                        resultSet.getLong("section_id"),
                        resultSet.getString("file_name"),
                        resultSet.getString("file_type"),
                        resultSet.getString("mime_type"),
                        resultSet.getString("file_path"),
                        uploadedAt == null ? null : uploadedAt.toLocalDateTime()));
            }
        } catch (Exception exception) {
            throw new RepositoryException("Failed to load syllabus.", exception);
        }
    }

    private boolean syllabusExistsForUpdate(Connection connection, Long sectionId) throws SQLException {
        String sql = "SELECT 1 FROM syllabuses WHERE section_id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void upsertSyllabus(
            Connection connection,
            Long sectionId,
            String fileName,
            String fileType,
            String mimeType,
            String filePath) throws SQLException {
        String sql = """
                INSERT INTO syllabuses (section_id, file_name, file_type, mime_type, file_path)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    file_name = VALUES(file_name),
                    file_type = VALUES(file_type),
                    mime_type = VALUES(mime_type),
                    file_path = VALUES(file_path)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sectionId);
            statement.setString(2, fileName);
            statement.setString(3, fileType);
            statement.setString(4, mimeType);
            statement.setString(5, filePath);
            statement.executeUpdate();
        }
    }
}
