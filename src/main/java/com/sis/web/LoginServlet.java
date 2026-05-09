package com.sis.web;

import com.sis.admin.validation.SecurityService;
import com.sis.common.DatabaseManager;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final SecurityService securityService = new SecurityService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = trim(req.getParameter("email"));
        String password = trim(req.getParameter("password"));

        if (email.isEmpty() || password.isEmpty()) {
            resp.sendRedirect("index.html?error=missing");
            return;
        }

        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.password_hash, u.is_active, "
                + "CASE WHEN s.student_id IS NOT NULL THEN 'STUDENT' "
                + "WHEN i.instructor_id IS NOT NULL THEN 'INSTRUCTOR' ELSE 'ADMIN' END AS role "
                + "FROM users u "
                + "LEFT JOIN students s ON u.user_id = s.student_id "
                + "LEFT JOIN instructors i ON u.user_id = i.instructor_id "
                + "LEFT JOIN admins a ON u.user_id = a.admin_id "
                + "WHERE u.email = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                resp.sendRedirect("index.html?error=invalid");
                return;
            }

            String storedPassword = rs.getString("password_hash");
            boolean passwordMatches = password.equals(storedPassword)
                    || securityService.verifyPassword(password, storedPassword);

            if (!passwordMatches) {
                resp.sendRedirect("index.html?error=invalid");
                return;
            }

            if (!rs.getBoolean("is_active")) {
                resp.sendRedirect("index.html?error=inactive");
                return;
            }

            HttpSession session = req.getSession();
            session.setAttribute("userId", rs.getLong("user_id"));
            session.setAttribute("role", rs.getString("role"));
            session.setAttribute("name", rs.getString("first_name") + " " + rs.getString("last_name"));

            String role = rs.getString("role");
            if ("INSTRUCTOR".equals(role)) {
                resp.sendRedirect("instructor_dashboard.html");
            } else if ("ADMIN".equals(role)) {
                resp.sendRedirect("admin_dashboard.html");
            } else {
                resp.sendRedirect("student_dashboard.html");
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
