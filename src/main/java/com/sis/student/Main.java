package com.sis.student;

import com.sis.student.controller.StudentController;
import com.sis.student.service.StudentService;
import com.sis.student.ui.StudentDashboardUI;

public class Main {
    public static void main(String[] args) {
        // Dependency Injection for Reliability
        StudentService service = new StudentService();
        StudentController controller = new StudentController(service);
        new StudentDashboardUI(controller).start();
    }
}