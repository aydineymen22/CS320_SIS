package com.sis.instructor.support;

import com.sis.instructor.model.RosterStudent;
import com.sis.instructor.model.SyllabusMetadata;
import com.sis.instructor.repository.GradeRepository;
import com.sis.instructor.repository.InstructorAssignmentRepository;
import com.sis.instructor.repository.RepositoryChangeType;
import com.sis.instructor.repository.RosterRepository;
import com.sis.instructor.repository.SyllabusRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class InstructorRepositoryFakes
        implements InstructorAssignmentRepository, RosterRepository, GradeRepository, SyllabusRepository {
    private final Map<Long, Long> sectionAssignments = new HashMap<>();
    private final Map<EnrollmentKey, Long> activeEnrollments = new HashMap<>();
    private final Map<Long, RosterStudent> rosterStudents = new HashMap<>();
    private final Map<EnrollmentKey, String> grades = new HashMap<>();
    private final Map<Long, SyllabusMetadata> syllabuses = new HashMap<>();
    private final Set<String> validGradeCodes = new LinkedHashSet<>();

    public InstructorRepositoryFakes() {
        seed();
    }

    @Override
    public boolean isInstructorAssignedToSection(Long instructorId, Long sectionId) {
        return Objects.equals(sectionAssignments.get(sectionId), instructorId);
    }

    @Override
    public List<RosterStudent> findEnrolledStudentsBySection(Long sectionId) {
        List<RosterStudent> roster = new ArrayList<>();
        for (EnrollmentKey key : activeEnrollments.keySet()) {
            if (Objects.equals(key.sectionId(), sectionId)) {
                RosterStudent student = rosterStudents.get(key.studentId());
                roster.add(new RosterStudent(
                        student.getStudentId(),
                        student.getStudentNumber(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getEmail(),
                        student.getEnrollmentStatus(),
                        grades.get(key)));
            }
        }
        roster.sort(Comparator
                .comparing(RosterStudent::getLastName)
                .thenComparing(RosterStudent::getFirstName));
        return roster;
    }

    @Override
    public boolean isStudentRegisteredInSection(Long studentId, Long sectionId) {
        return activeEnrollments.containsKey(new EnrollmentKey(studentId, sectionId));
    }

    @Override
    public RepositoryChangeType saveOrUpdateGrade(Long studentId, Long sectionId, String gradeCode) {
        EnrollmentKey key = new EnrollmentKey(studentId, sectionId);
        if (!activeEnrollments.containsKey(key)) {
            throw new IllegalArgumentException("Enrollment not found.");
        }

        boolean alreadyExists = grades.containsKey(key);
        grades.put(key, gradeCode);
        return alreadyExists ? RepositoryChangeType.UPDATED : RepositoryChangeType.CREATED;
    }

    @Override
    public Set<String> findValidGradeCodes() {
        return Set.copyOf(validGradeCodes);
    }

    @Override
    public RepositoryChangeType saveOrReplaceSyllabus(
            Long sectionId,
            String fileName,
            String fileType,
            String mimeType,
            String filePath) {
        boolean alreadyExists = syllabuses.containsKey(sectionId);
        syllabuses.put(
                sectionId,
                new SyllabusMetadata(sectionId, fileName, fileType, mimeType, filePath, LocalDateTime.now()));
        return alreadyExists ? RepositoryChangeType.UPDATED : RepositoryChangeType.CREATED;
    }

    @Override
    public Optional<SyllabusMetadata> findBySectionId(Long sectionId) {
        return Optional.ofNullable(syllabuses.get(sectionId));
    }

    public Optional<String> findGradeCode(Long studentId, Long sectionId) {
        return Optional.ofNullable(grades.get(new EnrollmentKey(studentId, sectionId)));
    }

    private void seed() {
        validGradeCodes.add("A");
        validGradeCodes.add("A-");
        validGradeCodes.add("B+");
        validGradeCodes.add("B");
        validGradeCodes.add("B-");
        validGradeCodes.add("C+");
        validGradeCodes.add("C");
        validGradeCodes.add("C-");
        validGradeCodes.add("D+");
        validGradeCodes.add("D");

        sectionAssignments.put(1L, 3L);
        sectionAssignments.put(2L, 3L);
        sectionAssignments.put(3L, 3L);

        rosterStudents.put(1L, new RosterStudent(
                1L, "20230001", "Ali", "Yilmaz", "student1@ozu.edu.tr", "ENROLLED", null));
        rosterStudents.put(2L, new RosterStudent(
                2L, "20230002", "Zeynep", "Kara", "student2@ozu.edu.tr", "ENROLLED", null));

        activeEnrollments.put(new EnrollmentKey(1L, 1L), 1L);
        activeEnrollments.put(new EnrollmentKey(1L, 2L), 2L);
        activeEnrollments.put(new EnrollmentKey(2L, 1L), 3L);
        activeEnrollments.put(new EnrollmentKey(2L, 3L), 4L);

        grades.put(new EnrollmentKey(1L, 2L), "B");

        syllabuses.put(
                1L,
                new SyllabusMetadata(
                        1L,
                        "cs320_syllabus.pdf",
                        "PDF",
                        "application/pdf",
                        "/uploads/syllabuses/cs320_syllabus.pdf",
                        LocalDateTime.of(2026, 2, 10, 9, 0)));
    }

    private record EnrollmentKey(Long studentId, Long sectionId) {
    }
}
