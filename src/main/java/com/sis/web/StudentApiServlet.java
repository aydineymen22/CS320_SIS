package com.sis.web;

import com.sis.common.model.Course;
import com.sis.student.service.StudentService;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/student")
public class StudentApiServlet extends HttpServlet {
    private final StudentService studentService = new StudentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long studentId = AuthUtil.requireRole(req, resp, "STUDENT");
        if (studentId == null) return;

        String action = value(req.getParameter("action"));
        if ("viewAvailableCourses".equals(action)) {
            AuthUtil.writeJson(resp, JsonUtil.courses(studentService.viewAvailableCourses()));
            return;
        }
        if ("search".equals(action)) {
            AuthUtil.writeJson(resp, JsonUtil.courses(studentService.searchCourses(value(req.getParameter("query")))));
            return;
        }
        if ("getSchedule".equals(action)) {
            AuthUtil.writeJson(resp, JsonUtil.courses(studentService.getMySchedule(studentId)));
            return;
        }
        if ("getTranscript".equals(action)) {
            AuthUtil.writeJson(resp, JsonUtil.transcript(studentService.viewTranscript(studentId)));
            return;
        }
        if ("getCourseDetails".equals(action)) {
            Long sectionId = parseLong(req.getParameter("sectionId"));
            if (sectionId == null) {
                AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "sectionId is required");
                return;
            }
            Course course = studentService.viewCourseDetails(sectionId);
            if (course == null) {
                AuthUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Course not found");
                return;
            }
            AuthUtil.writeJson(resp, JsonUtil.course(course));
            return;
        }

        AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long studentId = AuthUtil.requireRole(req, resp, "STUDENT");
        if (studentId == null) return;

        String action = value(req.getParameter("action"));
        Long sectionId = parseLong(req.getParameter("sectionId"));
        if (sectionId == null) {
            AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "sectionId is required");
            return;
        }

        String result;
        if ("enroll".equals(action)) {
            result = studentService.addCourse(studentId, sectionId);
        } else if ("drop".equals(action)) {
            result = studentService.dropCourse(studentId, sectionId);
        } else {
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

    private String value(String text) {
        return text == null ? "" : text.trim();
    }
}
