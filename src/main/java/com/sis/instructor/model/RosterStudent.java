package com.sis.instructor.model;

public class RosterStudent {
    private final Long studentId;
    private final String studentNumber;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String enrollmentStatus;
    private final String letterGrade;

    public RosterStudent(
            Long studentId,
            String studentNumber,
            String firstName,
            String lastName,
            String email,
            String enrollmentStatus,
            String letterGrade) {
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.enrollmentStatus = enrollmentStatus;
        this.letterGrade = letterGrade;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public String getLetterGrade() {
        return letterGrade;
    }
}
