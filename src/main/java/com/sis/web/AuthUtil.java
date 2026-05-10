package com.sis.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public final class AuthUtil {
    private AuthUtil() {}

    public static HttpSession requireSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Login required");
        }
        return session;
    }

    public static Long requireRole(HttpServletRequest req, HttpServletResponse resp, String role) throws IOException {
        HttpSession session = requireSession(req, resp);
        if (session == null) return null;

        Object sessionRole = session.getAttribute("role");
        if (!(sessionRole instanceof String) || !role.equalsIgnoreCase((String) sessionRole)) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return null;
        }

        Object userId = session.getAttribute("userId");
        if (!(userId instanceof Long)) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid session");
            return null;
        }
        return (Long) userId;
    }

    public static void writeJson(HttpServletResponse resp, String json) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }

    public static void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        writeJson(resp, JsonUtil.message(message));
    }
}
