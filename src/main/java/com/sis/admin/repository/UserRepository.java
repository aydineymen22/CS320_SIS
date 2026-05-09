package com.sis.admin.repository;

import com.sis.common.DatabaseManager;
import java.sql.*;

public class UserRepository {

    public boolean existsByEmail(String email) {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public Long saveUser(String role, String firstName, String lastName, String email, String passwordHash) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            String userSql = "INSERT INTO users (email, password_hash, first_name, last_name) VALUES (?, ?, ?, ?)";
            PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, email);
            userStmt.setString(2, passwordHash);
            userStmt.setString(3, firstName);
            userStmt.setString(4, lastName);
            userStmt.executeUpdate();

            ResultSet keys = userStmt.getGeneratedKeys();
            keys.next();
            long userId = keys.getLong(1);

            if (role.equals("STUDENT")) {
                String studentSql = "INSERT INTO students (student_id, student_number, admission_year, major) VALUES (?, ?, 2026, 'Undeclared')";
                PreparedStatement studentStmt = conn.prepareStatement(studentSql);
                studentStmt.setLong(1, userId);
                studentStmt.setString(2, "S" + userId); // Generate unique student number
                studentStmt.executeUpdate();
            } else if (role.equals("INSTRUCTOR")) {
                String instSql = "INSERT INTO instructors (instructor_id, title) VALUES (?, 'Instructor')";
                PreparedStatement instStmt = conn.prepareStatement(instSql);
                instStmt.setLong(1, userId);
                instStmt.executeUpdate();
            } else if (role.equals("ADMIN")) {
                String adminSql = "INSERT INTO admins (admin_id) VALUES (?)";
                PreparedStatement adminStmt = conn.prepareStatement(adminSql);
                adminStmt.setLong(1, userId);
                adminStmt.executeUpdate();
            } else {
                throw new SQLException("Unsupported user role: " + role);
            }

            conn.commit();
            return userId;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
            if (conn != null) conn.close();
        }
    }

    public boolean setStatus(Long userId, boolean isActive) {
        String query = "UPDATE users SET is_active = ? WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isActive);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserDetails(Long userId, String firstName, String lastName) {
        String query = "UPDATE users SET first_name = ?, last_name = ? WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setLong(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(Long userId, String newPasswordHash) {
        String query = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newPasswordHash);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
