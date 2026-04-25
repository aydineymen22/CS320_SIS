package com.sis.student.ui;

import com.sis.student.controller.StudentController;
import com.sis.student.model.Course;
import com.sis.student.model.Transcript;

import java.util.List;
import java.util.Scanner;

public class StudentDashboardUI {
    private final StudentController controller;
    private final Scanner scanner;
    private final Long currentStudentId = 1L; // Hardcoded for now, representing a logged-in student

    public StudentDashboardUI(StudentController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Student Information System ===");
            System.out.println("1. View Available Courses");
            System.out.println("2. Search for a Course");
            System.out.println("3. Enroll in a Course");
            System.out.println("4. Drop a Course");
            System.out.println("5. View Transcript");
            System.out.println("0. Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    displayCourses(controller.getAvailableCourses());
                    break;
                case "2":
                    System.out.print("Enter search keyword: ");
                    String keyword = scanner.nextLine();
                    displayCourses(controller.search(keyword));
                    break;
                case "3":
                    System.out.print("Enter Course ID to enroll: ");
                    Long enrollId = Long.parseLong(scanner.nextLine());
                    System.out.println(">> " + controller.enrollInCourse(currentStudentId, enrollId));
                    break;
                case "4":
                    System.out.print("Enter Course ID to drop: ");
                    Long dropId = Long.parseLong(scanner.nextLine());
                    System.out.println(">> " + controller.withdrawFromCourse(currentStudentId, dropId));
                    break;
                case "5":
                    displayTranscript(controller.getTranscript(currentStudentId));
                    break;
                case "0":
                    running = false;
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void displayCourses(List<Course> courses) {
        if (courses.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }
        System.out.println("\n--- Courses ---");
        for (Course c : courses) {
            System.out.printf("ID: %d | %s - %s | Quota: %d | Instr: %s%n",
                    c.getCourseId(), c.getCourseCode(), c.getCourseName(), c.getQuota(), c.getInstructorName());
        }
    }

    private void displayTranscript(Transcript transcript) {
        if (transcript == null || transcript.getCourses().isEmpty()) {
            System.out.println("\nYour transcript is currently empty.");
            return;
        }
        System.out.println("\n--- Your Transcript ---");
        for (Course c : transcript.getCourses()) {
            System.out.println("- " + c.getCourseCode() + ": " + c.getCourseName());
        }
    }
}