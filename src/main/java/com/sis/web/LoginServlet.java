package com.sis.web;

import com.sis.admin.validation.SecurityService;
import com.sis.common.DatabaseManager;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        String sql = "SELECT u.user_id, u.first_name, u.password_hash, u.is_active, " +
                "CASE WHEN s.student_id IS NOT NULL THEN 'STUDENT' " +
                "     WHEN i.instructor_id IS NOT NULL THEN 'INSTRUCTOR' " +
                "     WHEN a.admin_id IS NOT NULL THEN 'ADMIN' " +
                "     ELSE NULL END as role " +
                "FROM users u " +
                "LEFT JOIN students s ON u.user_id = s.student_id " +
                "LEFT JOIN instructors i ON u.user_id = i.instructor_id " +
                "LEFT JOIN admins a ON u.user_id = a.admin_id " +
                "WHERE u.email = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            SecurityService securityService = new SecurityService();
            if (rs.next()
                    && rs.getBoolean("is_active")
                    && rs.getString("role") != null
                    && securityService.verifyPassword(password, rs.getString("password_hash"))) {
                // SUCCESS: Create a Session (The "Security Badge")
                HttpSession session = req.getSession();
                session.setAttribute("userId", rs.getLong("user_id"));
                session.setAttribute("role", rs.getString("role"));
                session.setAttribute("name", rs.getString("first_name"));

                resp.sendRedirect("student_dashboard.html");
            } else {
                resp.getWriter().write("Invalid Credentials");
            }
        } catch (SQLException e) {
            resp.sendError(500, e.getMessage());
        }
    }
}
