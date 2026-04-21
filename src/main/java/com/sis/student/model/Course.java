package com.sis.student.model;

public class Course {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private int quota;
    private String instructorName;

    // Constructor
    public Course(Long courseId, String courseCode, String courseName, int quota, String instructorName) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.quota = quota;
        this.instructorName = instructorName;
    }

    // Getters (Required for the UI to show the data)
    public Long getCourseId() { return courseId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public int getQuota() { return quota; }
    public String getInstructorName() { return instructorName; }

    // Setters (Required to update the quota when someone joins)
    public void setQuota(int quota) { this.quota = quota; }
}