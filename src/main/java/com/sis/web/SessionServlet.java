package com.sis.web;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/session")
public class SessionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            AuthUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Login required");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        String name = (String) session.getAttribute("name");

        AuthUtil.writeJson(resp, JsonUtil.session(userId == null ? 0L : userId, role == null ? "" : role, name == null ? "" : name));
    }
}
