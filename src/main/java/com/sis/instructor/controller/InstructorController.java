package com.sis.instructor.controller;

import com.sis.instructor.api.InstructorInterface;
import com.sis.instructor.model.RosterStudent;
import com.sis.instructor.service.GradeService;
import com.sis.instructor.service.RosterService;
import com.sis.instructor.service.SyllabusService;
import java.io.File;
import java.util.List;

public class InstructorController implements InstructorInterface {
    private final SyllabusService syllabusService;
    private final RosterService rosterService;
    private final GradeService gradeService;

    public InstructorController(
            SyllabusService syllabusService,
            RosterService rosterService,
            GradeService gradeService) {
        this.syllabusService = syllabusService;
        this.rosterService = rosterService;
        this.gradeService = gradeService;
    }

    @Override
    public String uploadSyllabus(Long instructorId, Long courseId, File syllabusFile) {
        if (instructorId == null || courseId == null || syllabusFile == null) {
            return "Error: Invalid syllabus request.";
        }
        return syllabusService.uploadOrReplaceSyllabus(instructorId, courseId, syllabusFile);
    }

    @Override
    public List<RosterStudent> viewRoster(Long instructorId, Long courseId) {
        if (instructorId == null || courseId == null) {
            return List.of();
        }
        return rosterService.getRoster(instructorId, courseId);
    }

    @Override
    public String updateGrade(Long instructorId, Long studentId, Long courseId, String letterGrade) {
        if (instructorId == null || studentId == null || courseId == null) {
            return "Error: Invalid grade request.";
        }
        return gradeService.saveOrUpdateGrade(instructorId, studentId, courseId, letterGrade);
    }
}
