package com.sis.student;

import com.sis.student.api.StudentServiceAPI;
import com.sis.student.controller.StudentController;
import com.sis.student.model.Course;
import com.sis.student.service.StudentService;
import com.sis.student.ui.StudentDashboardUI;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize the Brain (Service)
        StudentServiceAPI service = new StudentService();

        ((StudentService) service).viewAvailableCourses().addAll(List.of(
                new Course(101L, "CS320", "Software Engineering", 2, "Dr. Smith"),
                new Course(102L, "CS101", "Intro to CS", 0, "Dr. Jones"), // Full course
                new Course(103L, "MATH201", "Calculus I", 30, "Dr. Adams")
        ));

        StudentController controller = new StudentController(service);

        StudentDashboardUI ui = new StudentDashboardUI(controller);

        ui.start();
    }
}