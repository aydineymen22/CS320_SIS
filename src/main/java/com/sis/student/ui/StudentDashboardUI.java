package com.sis.student.ui;

import com.sis.student.controller.StudentController;
import com.sis.student.model.Course;
import com.sis.student.model.Transcript;
import java.util.Scanner;

public class StudentDashboardUI {
    private final StudentController controller;
    private final Scanner scanner = new Scanner(System.in);
    private final Long loggedInStudentId = 1L; // Matches 'Ali Yilmaz' in your schema.sql

    public StudentDashboardUI(StudentController controller) { this.controller = controller; }

    public void start() {
        while (true) {
            System.out.println("\n--- SIS Student Dashboard ---");
            System.out.println("1. View Courses\n2. Search\n3. Enroll\n4. Drop\n5. Transcript\n0. Exit");
            System.out.print("Choice: ");
            String choice = scanner.nextLine();
            if (choice.equals("0")) break;

            switch (choice) {
                case "1" -> controller.getAvailableCourses().forEach(c -> System.out.println(c.getCourseCode() + " - " + c.getCourseName() + " (Quota: " + c.getAvailableQuota() + ")"));
                case "2" -> {
                    System.out.print("Query: ");
                    controller.search(scanner.nextLine()).forEach(c -> System.out.println(c.getCourseCode() + " - " + c.getCourseName()));
                }
                case "3" -> {
                    System.out.print("Section ID: ");
                    System.out.println(controller.enrollInCourse(loggedInStudentId, Long.parseLong(scanner.nextLine())));
                }
                case "4" -> {
                    System.out.print("Section ID to Drop: ");
                    System.out.println(controller.withdrawFromCourse(loggedInStudentId, Long.parseLong(scanner.nextLine())));
                }
                case "5" -> {
                    Transcript t = controller.getTranscript(loggedInStudentId);
                    t.getCourses().forEach(c -> System.out.println(c.getCourseCode() + " Grade: " + t.getGrades().get(c.getCourseCode())));
                }
            }
        }
    }
}