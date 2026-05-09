package com.sis.web;

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

        String sql = "SELECT u.user_id, u.first_name, " +
                "CASE WHEN s.student_id IS NOT NULL THEN 'STUDENT' " +
                "     WHEN i.instructor_id IS NOT NULL THEN 'INSTRUCTOR' " +
                "     ELSE 'ADMIN' END as role " +
                "FROM users u " +
                "LEFT JOIN students s ON u.user_id = s.student_id " +
                "LEFT JOIN instructors i ON u.user_id = i.instructor_id " +
                "WHERE u.email = ? AND u.password_hash = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
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