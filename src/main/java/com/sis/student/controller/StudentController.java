package com.sis.student.controller;

import com.sis.common.model.Course;
import com.sis.common.model.Transcript;
import com.sis.student.service.StudentService;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        if (session == null) {
            resp.setStatus(401);
            resp.getWriter().write("[]");
            return;
        }

        Long studentId = (Long) session.getAttribute("userId");
        String action = req.getParameter("action");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if ("getSchedule".equals(action)) {
            List<Course> myCourses = studentService.getMySchedule(studentId);
            resp.getWriter().write(convertListToJson(myCourses));
            return;
        }

        else if ("search".equals(action)) {
            String query = req.getParameter("query");
            List<Course> results = studentService.searchCourses(query);
            resp.getWriter().write(convertListToJson(results));
            return;
        }

        else if ("getTranscript".equals(action)) {
            Transcript t = studentService.viewTranscript(studentId);

            StringBuilder sb = new StringBuilder("[");
            List<Course> courseList = t.getCourses();
            Map<String, String> gradeMap = t.getGrades();

            for (int i = 0; i < courseList.size(); i++) {
                Course c = courseList.get(i);
                String grade = gradeMap.get(c.getCourseCode());

                sb.append("{")
                        .append("\"code\":\"").append(c.getCourseCode()).append("\",")
                        .append("\"name\":\"").append(c.getCourseName()).append("\",")
                        .append("\"grade\":\"").append(grade != null ? grade : "N/A").append("\"")
                        .append("}");

                if (i < courseList.size() - 1) sb.append(",");
            }
            sb.append("]");

            resp.getWriter().write(sb.toString());
            return;
        }

        resp.getWriter().write("[]");
    }


    private String convertListToJson(List<Course> courses) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);

            sb.append(String.format(
                    "{\"sectionId\":%s,\"courseCode\":\"%s\",\"courseName\":\"%s\",\"availableQuota\":%d,\"instructorName\":\"%s\",\"credits\":%d,\"courseAbstract\":\"%s\"}",
                    c.getSectionId(),
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getAvailableQuota(),
                    c.getInstructorName(),
                    c.getCredits(),
                    c.getCourseAbstract()
            ));

            if (i < courses.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}