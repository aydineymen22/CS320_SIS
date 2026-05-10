package com.sis.web;

import com.sis.common.model.Course;
import com.sis.common.model.Transcript;
import com.sis.instructor.model.RosterStudent;
import java.util.List;
import java.util.Map;

public final class JsonUtil {
    private JsonUtil() {}

    public static String message(String message) {
        boolean success = isSuccess(message);
        return "{\"success\":" + success + ",\"message\":\"" + escape(message) + "\"}";
    }

    public static String session(Long userId, String role, String name) {
        return "{"
                + "\"authenticated\":true,"
                + "\"userId\":" + userId + ","
                + "\"role\":\"" + escape(role) + "\","
                + "\"name\":\"" + escape(name) + "\","
                + "\"fullName\":\"" + escape(name) + "\""
                + "}";
    }

    public static String courses(List<Course> courses) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < courses.size(); i++) {
            sb.append(course(courses.get(i)));
            if (i < courses.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String course(Course c) {
        return "{"
                + "\"sectionId\":" + c.getSectionId() + ","
                + "\"courseCode\":\"" + escape(c.getCourseCode()) + "\","
                + "\"courseName\":\"" + escape(c.getCourseName()) + "\","
                + "\"sectionNo\":\"" + escape(c.getSectionNo()) + "\","
                + "\"termName\":\"" + escape(c.getTermName()) + "\","
                + "\"availableQuota\":" + c.getAvailableQuota() + ","
                + "\"quota\":" + c.getQuota() + ","
                + "\"instructorName\":\"" + escape(c.getInstructorName()) + "\","
                + "\"credits\":" + (c.getCredits() == null ? "null" : c.getCredits()) + ","
                + "\"courseAbstract\":\"" + escape(c.getCourseAbstract()) + "\""
                + "}";
    }

    public static String transcript(Transcript transcript) {
        StringBuilder sb = new StringBuilder("[");
        List<Course> courses = transcript.getCourses();
        Map<String, String> grades = transcript.getGrades();
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            String grade = grades.get(c.getCourseCode());
            sb.append("{")
                    .append("\"sectionId\":").append(c.getSectionId()).append(",")
                    .append("\"courseCode\":\"").append(escape(c.getCourseCode())).append("\",")
                    .append("\"courseName\":\"").append(escape(c.getCourseName())).append("\",")
                    .append("\"grade\":\"").append(escape(grade == null ? "N/A" : grade)).append("\"")
                    .append("}");
            if (i < courses.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String roster(List<RosterStudent> students) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < students.size(); i++) {
            RosterStudent s = students.get(i);
            sb.append("{")
                    .append("\"studentId\":").append(s.getStudentId()).append(",")
                    .append("\"studentNumber\":\"").append(escape(s.getStudentNumber())).append("\",")
                    .append("\"firstName\":\"").append(escape(s.getFirstName())).append("\",")
                    .append("\"lastName\":\"").append(escape(s.getLastName())).append("\",")
                    .append("\"email\":\"").append(escape(s.getEmail())).append("\",")
                    .append("\"enrollmentStatus\":\"").append(escape(s.getEnrollmentStatus())).append("\",")
                    .append("\"letterGrade\":\"").append(escape(s.getLetterGrade())).append("\"")
                    .append("}");
            if (i < students.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static boolean isSuccess(String message) {
        if (message == null) return false;
        String normalized = message.trim().toLowerCase();
        return normalized.startsWith("success") || normalized.startsWith("successfully");
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
