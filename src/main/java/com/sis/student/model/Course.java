package com.sis.student.model;

public class Course {
    private Long sectionId;
    private String courseCode;
    private String courseName;
    private String sectionNo;
    private String termName;
    private Integer availableQuota;
    private String instructorName;

    public Course() {}

    public Course(Long sectionId, String courseCode, String courseName, String sectionNo,
                  String termName, Integer availableQuota, String instructorName) {
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.sectionNo = sectionNo;
        this.termName = termName;
        this.availableQuota = availableQuota;
        this.instructorName = instructorName;
    }

    public Long getCourseId() { return sectionId; } // Alias for getSectionId
    public void setQuota(int quota) { this.availableQuota = quota; } // Alias for setAvailableQuota

    public Long getSectionId() { return sectionId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public Integer getAvailableQuota() { return availableQuota; }
    public String getSectionNo() { return sectionNo; }
    public String getTermName() { return termName; }
    public String getInstructorName() { return instructorName; }

    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}