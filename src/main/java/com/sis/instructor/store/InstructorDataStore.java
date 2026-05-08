package com.sis.instructor.store;

import com.sis.instructor.model.RosterStudent;
import com.sis.instructor.model.SyllabusMetadata;
import java.io.File;
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

public class InstructorDataStore {
    public enum ChangeType {
        CREATED,
        UPDATED
    }

    private static final String STATUS_ENROLLED = "ENROLLED";
    private static final String STATUS_DROPPED = "DROPPED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final LocalDateTime SEEDED_TIMESTAMP =
            LocalDateTime.of(2026, 2, 10, 9, 0);

    private final Map<Long, UserRecord> usersById = new HashMap<>();
    private final Map<Long, StudentRecord> studentsById = new HashMap<>();
    private final Map<Long, Long> sectionAssignments = new HashMap<>();
    private final Map<Long, EnrollmentRecord> enrollmentsById = new HashMap<>();
    private final Map<Long, GradeRecord> gradesByEnrollmentId = new HashMap<>();
    private final Map<Long, SyllabusMetadata> syllabusesBySection = new HashMap<>();
    private final Set<String> validGradeCodes = new LinkedHashSet<>();

    public InstructorDataStore() {
        seedFromDatabaseFiles();
    }

    public boolean isInstructorAssignedToSection(Long instructorId, Long sectionId) {
        return Objects.equals(sectionAssignments.get(sectionId), instructorId);
    }

    public List<RosterStudent> getRosterForSection(Long sectionId) {
        List<RosterStudent> roster = new ArrayList<>();

        for (EnrollmentRecord enrollment : enrollmentsById.values()) {
            if (!Objects.equals(enrollment.sectionId(), sectionId)) {
                continue;
            }
            if (!STATUS_ENROLLED.equals(enrollment.status())) {
                continue;
            }

            UserRecord user = usersById.get(enrollment.studentId());
            StudentRecord student = studentsById.get(enrollment.studentId());
            GradeRecord grade = gradesByEnrollmentId.get(enrollment.enrollmentId());

            roster.add(new RosterStudent(
                    student.studentId(),
                    student.studentNumber(),
                    user.firstName(),
                    user.lastName(),
                    user.email(),
                    enrollment.status(),
                    grade == null ? null : grade.gradeCode()));
        }

        roster.sort(Comparator
                .comparing(RosterStudent::getLastName)
                .thenComparing(RosterStudent::getFirstName));
        return roster;
    }

    public boolean isStudentRegisteredInSection(Long studentId, Long sectionId) {
        return findEnrollment(studentId, sectionId)
                .filter(enrollment -> !STATUS_DROPPED.equals(enrollment.status()))
                .isPresent();
    }

    public ChangeType saveOrUpdateGrade(Long studentId, Long sectionId, String gradeCode) {
        EnrollmentRecord enrollment = findEnrollment(studentId, sectionId)
                .filter(current -> !STATUS_DROPPED.equals(current.status()))
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found."));

        boolean alreadyExists = gradesByEnrollmentId.containsKey(enrollment.enrollmentId());
        gradesByEnrollmentId.put(
                enrollment.enrollmentId(),
                new GradeRecord(enrollment.enrollmentId(), gradeCode, LocalDateTime.now()));
        return alreadyExists ? ChangeType.UPDATED : ChangeType.CREATED;
    }

    public ChangeType saveOrReplaceSyllabus(
            Long sectionId,
            File syllabusFile,
            String fileType,
            String mimeType) {
        boolean alreadyExists = syllabusesBySection.containsKey(sectionId);
        String fileName = syllabusFile.getName().trim();
        String filePath = "/uploads/syllabuses/" + fileName;

        syllabusesBySection.put(
                sectionId,
                new SyllabusMetadata(
                        sectionId,
                        fileName,
                        fileType,
                        mimeType,
                        filePath,
                        LocalDateTime.now()));

        return alreadyExists ? ChangeType.UPDATED : ChangeType.CREATED;
    }

    public Optional<String> findGradeCode(Long studentId, Long sectionId) {
        return findEnrollment(studentId, sectionId)
                .map(EnrollmentRecord::enrollmentId)
                .map(gradesByEnrollmentId::get)
                .map(GradeRecord::gradeCode);
    }

    public Optional<SyllabusMetadata> findSyllabus(Long sectionId) {
        return Optional.ofNullable(syllabusesBySection.get(sectionId));
    }

    public Set<String> getValidGradeCodes() {
        return Set.copyOf(validGradeCodes);
    }

    private Optional<EnrollmentRecord> findEnrollment(Long studentId, Long sectionId) {
        for (EnrollmentRecord enrollment : enrollmentsById.values()) {
            if (Objects.equals(enrollment.studentId(), studentId)
                    && Objects.equals(enrollment.sectionId(), sectionId)) {
                return Optional.of(enrollment);
            }
        }
        return Optional.empty();
    }

    private void seedFromDatabaseFiles() {
        // The public contract still says courseId, but the schema models instructor
        // ownership and grades at the section level, so these IDs map to section_id.
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

        usersById.put(1L, new UserRecord(1L, "student1@ozu.edu.tr", "Ali", "Yilmaz"));
        usersById.put(2L, new UserRecord(2L, "student2@ozu.edu.tr", "Zeynep", "Kara"));
        usersById.put(3L, new UserRecord(3L, "instructor1@ozu.edu.tr", "Ayse", "Demir"));

        studentsById.put(1L, new StudentRecord(1L, "20230001", "Computer Engineering"));
        studentsById.put(2L, new StudentRecord(2L, "20230002", "Computer Engineering"));

        sectionAssignments.put(1L, 3L);
        sectionAssignments.put(2L, 3L);
        sectionAssignments.put(3L, 3L);

        enrollmentsById.put(1L, new EnrollmentRecord(1L, 1L, 1L, STATUS_ENROLLED));
        enrollmentsById.put(2L, new EnrollmentRecord(2L, 1L, 2L, STATUS_COMPLETED));
        enrollmentsById.put(3L, new EnrollmentRecord(3L, 2L, 1L, STATUS_ENROLLED));
        enrollmentsById.put(4L, new EnrollmentRecord(4L, 2L, 3L, STATUS_ENROLLED));

        gradesByEnrollmentId.put(2L, new GradeRecord(2L, "B", SEEDED_TIMESTAMP));

        syllabusesBySection.put(
                1L,
                new SyllabusMetadata(
                        1L,
                        "cs320_syllabus.pdf",
                        "PDF",
                        "application/pdf",
                        "/uploads/syllabuses/cs320_syllabus.pdf",
                        SEEDED_TIMESTAMP));
        syllabusesBySection.put(
                2L,
                new SyllabusMetadata(
                        2L,
                        "cs210_syllabus.docx",
                        "DOCX",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "/uploads/syllabuses/cs210_syllabus.docx",
                        SEEDED_TIMESTAMP));
    }

    private record UserRecord(
            Long userId,
            String email,
            String firstName,
            String lastName) {
    }

    private record StudentRecord(
            Long studentId,
            String studentNumber,
            String major) {
    }

    private record EnrollmentRecord(
            Long enrollmentId,
            Long studentId,
            Long sectionId,
            String status) {
    }

    private record GradeRecord(
            Long enrollmentId,
            String gradeCode,
            LocalDateTime updatedAt) {
    }
}
