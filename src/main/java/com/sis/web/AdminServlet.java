package com.sis.web;

import com.sis.admin.controller.AdminController;
import com.sis.admin.service.CourseManagementService;
import com.sis.admin.service.InstructorAssignmentService;
import com.sis.admin.service.UserManagementService;
import com.sis.admin.validation.CourseValidator;
import com.sis.admin.validation.SecurityService;
import com.sis.admin.validation.UserValidator;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/admin")
public class AdminServlet extends HttpServlet {
    private final AdminController adminController = new AdminController(
            new CourseManagementService(new CourseValidator()),
            new InstructorAssignmentService(),
            new UserManagementService(new UserValidator(), new SecurityService())
    );

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long adminId = AuthUtil.requireRole(req, resp, "ADMIN");
        if (adminId == null) return;

        String action = value(req.getParameter("action"));
        String result;

        switch (action) {
            case "createCourse":
                Integer quota = parseInt(req.getParameter("quota"));
                if (quota == null) {
                    AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "quota is required");
                    return;
                }
                result = adminController.createCourse(value(req.getParameter("courseCode")), value(req.getParameter("courseName")), quota);
                break;
            case "updateCourse":
                Long courseId = parseLong(req.getParameter("courseId"));
                Integer newQuota = parseInt(req.getParameter("newQuota"));
                if (courseId == null || newQuota == null) {
                    AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "courseId and newQuota are required");
                    return;
                }
                result = adminController.updateCourse(courseId, value(req.getParameter("newName")), newQuota);
                break;
            case "assignInstructor":
                Long assignCourseId = parseLong(req.getParameter("courseId"));
                Long instructorId = parseLong(req.getParameter("instructorId"));
                if (assignCourseId == null || instructorId == null) {
                    AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "courseId and instructorId are required");
                    return;
                }
                result = adminController.assignInstructor(assignCourseId, instructorId);
                break;
            case "removeInstructor":
                Long removeCourseId = parseLong(req.getParameter("courseId"));
                if (removeCourseId == null) {
                    AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "courseId is required");
                    return;
                }
                result = adminController.removeInstructor(removeCourseId);
                break;
            case "createUser":
                result = adminController.createUser(
                        value(req.getParameter("role")),
                        value(req.getParameter("firstName")),
                        value(req.getParameter("lastName")),
                        value(req.getParameter("email"))
                );
                break;
            case "updateUser":
                Long userId = parseLong(req.getParameter("userId"));
                if (userId == null) {
                    AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "userId is required");
                    return;
                }
                result = adminController.updateUser(userId, value(req.getParameter("firstName")), value(req.getParameter("lastName")));
                break;
            case "deactivateUser":
                Long deactivateUserId = parseLong(req.getParameter("userId"));
                if (deactivateUserId == null) {
                    AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "userId is required");
                    return;
                }
                result = adminController.deactivateUser(deactivateUserId);
                break;
            case "resetPassword":
                Long resetUserId = parseLong(req.getParameter("userId"));
                if (resetUserId == null) {
                    AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "userId is required");
                    return;
                }
                result = adminController.resetPassword(resetUserId);
                break;
            default:
                AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
                return;
        }

        AuthUtil.writeJson(resp, JsonUtil.message(result));
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String value(String text) {
        return text == null ? "" : text.trim();
    }
}
