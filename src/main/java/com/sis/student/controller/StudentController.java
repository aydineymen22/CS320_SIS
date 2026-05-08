package com.sis.student.controller;

import com.sis.student.service.StudentService;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/student-action")
public class StudentController extends HttpServlet {

    private final StudentService studentService = new StudentService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        String sIdRaw = req.getParameter("studentId");
        String secIdRaw = req.getParameter("sectionId");

        if (sIdRaw == null || secIdRaw == null || action == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Error: Missing parameters.");
            return;
        }

        try {
            Long studentId = Long.parseLong(sIdRaw);
            Long sectionId = Long.parseLong(secIdRaw);
            String result;

            if ("enroll".equals(action)) {
                result = studentService.addCourse(studentId, sectionId);
            } else if ("drop".equals(action)) {
                result = studentService.dropCourse(studentId, sectionId);
            } else {
                result = "Error: Invalid action.";
            }

            resp.setContentType("text/plain");
            resp.getWriter().write(result);

        } catch (NumberFormatException e) {
            resp.getWriter().write("Error: IDs must be numeric.");
        }
    }
}