package com.sis.web;

import com.sis.admin.validation.SecurityService;
import com.sis.common.DatabaseManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final SecurityService securityService = new SecurityService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            resp.sendRedirect("index.html?error=missing");
            return;
        }

        String sql =
                "SELECT u.user_id, u.first_name, u.last_name, u.password_hash, u.is_active, " +
                        "CASE WHEN s.student_id IS NOT NULL THEN 'STUDENT' " +
                        "     WHEN i.instructor_id IS NOT NULL THEN 'INSTRUCTOR' " +
                        "     ELSE 'ADMIN' END AS role " +
                        "FROM users u " +
                        "LEFT JOIN students s ON u.user_id = s.student_id " +
                        "LEFT JOIN instructors i ON u.user_id = i.instructor_id " +
                        "LEFT JOIN admins a ON u.user_id = a.admin_id " +
                        "WHERE u.email = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    resp.sendRedirect("index.html?error=invalid");
                    return;
                }

                boolean isActive = rs.getBoolean("is_active");
                if (!isActive) {
                    resp.sendRedirect("index.html?error=inactive");
                    return;
                }

                String storedPassword = rs.getString("password_hash");

                boolean passwordMatches =
                        password.equals(storedPassword) ||
                                securityService.verifyPassword(password, storedPassword);

                if (!passwordMatches) {
                    resp.sendRedirect("index.html?error=invalid");
                    return;
                }

                HttpSession session = req.getSession();
                session.setAttribute("userId", rs.getLong("user_id"));
                session.setAttribute("role", rs.getString("role"));
                session.setAttribute("name",
                        rs.getString("first_name") + " " + rs.getString("last_name"));

                String role = rs.getString("role");
                if ("STUDENT".equals(role)) {
                    resp.sendRedirect("student_dashboard.html");
                } else if ("INSTRUCTOR".equals(role)) {
                    resp.sendRedirect("instructor_dashboard.html");
                } else {
                    resp.sendRedirect("admin_dashboard.html");
                }
            }

        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}