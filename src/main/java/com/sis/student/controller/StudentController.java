package com.sis.student.controller;

import com.sis.common.model.Course;
import com.sis.student.service.StudentService;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/student-action")
public class StudentController extends HttpServlet {

    private final StudentService studentService = new StudentService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !"STUDENT".equals(session.getAttribute("role"))) {
            resp.sendError(403, "Unauthorized");
            return;
        }

        Long studentId = (Long) session.getAttribute("userId"); // SECURE ID
        Long sectionId = Long.parseLong(req.getParameter("sectionId"));
        String action = req.getParameter("action");

        String result = "enroll".equals(action) ?
                studentService.addCourse(studentId, sectionId) :
                studentService.dropCourse(studentId, sectionId);

        resp.getWriter().write(result);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Long studentId = (Long) session.getAttribute("userId");
        String action = req.getParameter("action");

        resp.setContentType("application/json");
        String jsonResponse = "[]";

        if ("getSchedule".equals(action)) {
            List<Course> myCourses = studentService.getMySchedule(studentId);
            jsonResponse = convertListToJson(myCourses);
        } else if ("search".equals(action)) {
            String query = req.getParameter("query");
            List<Course> results = studentService.searchCourses(query);
            jsonResponse = convertListToJson(results);
        }

        resp.getWriter().write(jsonResponse);
    }

    private String convertListToJson(List<Course> courses) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            sb.append(String.format(
                    "{\"sectionId\":%d, \"courseCode\":\"%s\", \"courseName\":\"%s\", \"availableQuota\":%d, \"instructorName\":\"%s\"}",
                    c.getSectionId(), c.getCourseCode(), c.getCourseName(), c.getAvailableQuota(), c.getInstructorName()
            ));
            if (i < courses.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}