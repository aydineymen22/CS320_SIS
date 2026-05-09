package com.sis.instructor.repository;

import com.sis.common.DatabaseManager;
import com.sis.instructor.model.RosterStudent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JdbcRosterRepository implements RosterRepository {
    @Override
    public List<RosterStudent> findEnrolledStudentsBySection(Long sectionId) {
        String sql = """
                SELECT
                    s.student_id,
                    s.student_number,
                    u.first_name,
                    u.last_name,
                    u.email,
                    e.status AS enrollment_status,
                    g.grade_code
                FROM enrollments e
                JOIN students s
                    ON s.student_id = e.student_id
                JOIN users u
                    ON u.user_id = s.student_id
                LEFT JOIN grades g
                    ON g.enrollment_id = e.enrollment_id
                WHERE e.section_id = ?
                  AND e.status = 'ENROLLED'
                ORDER BY u.last_name, u.first_name
                """;

        List<RosterStudent> roster = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sectionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    roster.add(new RosterStudent(
                            resultSet.getLong("student_id"),
                            resultSet.getString("student_number"),
                            resultSet.getString("first_name"),
                            resultSet.getString("last_name"),
                            resultSet.getString("email"),
                            resultSet.getString("enrollment_status"),
                            resultSet.getString("grade_code")));
                }
            }
        } catch (Exception exception) {
            throw new RepositoryException("Failed to load section roster.", exception);
        }
        return roster;
    }
}
