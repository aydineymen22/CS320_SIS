package com.sis.instructor.repository;

import com.sis.common.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JdbcInstructorAssignmentRepository implements InstructorAssignmentRepository {
    @Override
    public boolean isInstructorAssignedToSection(Long instructorId, Long sectionId) {
        String sql = """
                SELECT 1
                FROM section_instructors
                WHERE instructor_id = ? AND section_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, instructorId);
            statement.setLong(2, sectionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (Exception exception) {
            throw new RepositoryException("Failed to check instructor assignment.", exception);
        }
    }
}
