package com.sis.common.model;

public class Course {
    private Long sectionId;
    private String courseCode;
    private String courseName;
    private String sectionNo;
    private String termName;
    private int availableQuota;
    private String instructorName;

    public Course() {}

    public Course(Long sectionId, String courseCode, String courseName, int availableQuota, String instructorName) {
        this(sectionId, courseCode, courseName, "", "", availableQuota, instructorName);
    }

    public Course(Long sectionId, String courseCode, String courseName, String sectionNo,
                  String termName, int availableQuota, String instructorName) {
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.sectionNo = sectionNo;
        this.termName = termName;
        this.availableQuota = availableQuota;
        this.instructorName = instructorName;
    }


    public Long getSectionId() {
        return sectionId;
    }

    public Long getCourseId() {
        return sectionId;
    }

    public int getAvailableQuota() {
        return availableQuota;
    }

    public int getQuota() {
        return availableQuota;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getSectionNo() {
        return sectionNo;
    }

    public String getTermName() {
        return termName;
    }

    public String getInstructorName() {
        return instructorName;
    }


    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setAvailableQuota(int availableQuota) {
        this.availableQuota = availableQuota;
    }

    public void setQuota(int quota) {
        this.availableQuota = quota;
    }
}
