package com.sis.web;

import com.sis.common.DatabaseManager;
import com.sis.common.model.Course;
import com.sis.instructor.controller.InstructorController;
import com.sis.instructor.model.RosterStudent;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/instructor")
@MultipartConfig
public class InstructorServlet extends HttpServlet {
    private final InstructorController instructorController = new InstructorController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long instructorId = AuthUtil.requireRole(req, resp, "INSTRUCTOR");
        if (instructorId == null) return;

        String action = value(req.getParameter("action"));
        if ("listAssignedSections".equals(action)) {
            AuthUtil.writeJson(resp, JsonUtil.courses(findAssignedSections(instructorId)));
            return;
        }
        if ("viewRoster".equals(action)) {
            Long sectionId = parseSectionId(req);
            if (sectionId == null) {
                AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "sectionId is required");
                return;
            }
            List<RosterStudent> roster = instructorController.viewRoster(instructorId, sectionId);
            AuthUtil.writeJson(resp, JsonUtil.roster(roster));
            return;
        }

        AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Long instructorId = AuthUtil.requireRole(req, resp, "INSTRUCTOR");
        if (instructorId == null) return;

        String action = value(req.getParameter("action"));
        if ("updateGrade".equals(action)) {
            handleGradeUpdate(req, resp, instructorId);
            return;
        }
        if ("uploadSyllabus".equals(action)) {
            handleSyllabusUpload(req, resp, instructorId);
            return;
        }

        AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
    }

    private void handleGradeUpdate(HttpServletRequest req, HttpServletResponse resp, Long instructorId) throws IOException {
        Long sectionId = parseSectionId(req);
        Long studentId = parseLong(req.getParameter("studentId"));
        String grade = value(req.getParameter("letterGrade"));

        if (sectionId == null || studentId == null || grade.isEmpty()) {
            AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "sectionId, studentId and letterGrade are required");
            return;
        }

        String result = instructorController.updateGrade(instructorId, studentId, sectionId, grade);
        AuthUtil.writeJson(resp, JsonUtil.message(result));
    }

    private void handleSyllabusUpload(HttpServletRequest req, HttpServletResponse resp, Long instructorId) throws IOException, ServletException {
        Long sectionId = parseSectionId(req);
        Part filePart = req.getPart("syllabusFile");
        if (sectionId == null || filePart == null || filePart.getSize() == 0) {
            AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "sectionId and syllabusFile are required");
            return;
        }

        String originalName = safeFileName(filePart.getSubmittedFileName());
        Path uploadDirectory = resolveUploadDirectory();
        Files.createDirectories(uploadDirectory);

        String storedName = buildStoredFileName(sectionId, originalName);
        Path storedFile = uploadDirectory.resolve(storedName);

        try (InputStream inputStream = filePart.getInputStream()) {
            Files.copy(inputStream, storedFile, StandardCopyOption.REPLACE_EXISTING);
            String result = instructorController.uploadSyllabus(instructorId, sectionId, storedFile.toFile());
            if (!result.toLowerCase().startsWith("success")) {
                Files.deleteIfExists(storedFile);
            }
            AuthUtil.writeJson(resp, JsonUtil.message(result));
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(storedFile);
            throw e;
        }
    }

    private List<Course> findAssignedSections(Long instructorId) {
        String sql = """
                SELECT
                    cs.section_id,
                    cc.course_code,
                    cc.course_name,
                    cs.section_no,
                    t.term_name,
                    cs.quota - COUNT(CASE WHEN e.status = 'ENROLLED' THEN 1 END) AS available_quota,
                    CONCAT(u.first_name, ' ', u.last_name) AS instructor_name,
                    cc.credits,
                    cc.course_abstract
                FROM section_instructors si
                JOIN course_sections cs ON cs.section_id = si.section_id
                JOIN course_catalog cc ON cc.catalog_course_id = cs.catalog_course_id
                JOIN terms t ON t.term_id = cs.term_id
                JOIN users u ON u.user_id = si.instructor_id
                LEFT JOIN enrollments e ON e.section_id = cs.section_id
                WHERE si.instructor_id = ?
                GROUP BY cs.section_id, cc.course_code, cc.course_name, cs.section_no,
                         t.term_name, cs.quota, u.first_name, u.last_name, cc.credits, cc.course_abstract
                ORDER BY t.term_name, cc.course_code, cs.section_no
                """;

        List<Course> sections = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, instructorId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    sections.add(new Course(
                            rs.getLong("section_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("section_no"),
                            rs.getString("term_name"),
                            Math.max(0, rs.getInt("available_quota")),
                            rs.getString("instructor_name"),
                            (Integer) rs.getObject("credits"),
                            rs.getString("course_abstract")
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load assigned sections.", e);
        }
        return sections;
    }

    private Path resolveUploadDirectory() {
        String realPath = getServletContext().getRealPath("/uploads/syllabuses");
        if (realPath != null && !realPath.isBlank()) {
            return Path.of(realPath);
        }
        return Path.of(System.getProperty("java.io.tmpdir"), "sis-uploads", "syllabuses");
    }

    private String buildStoredFileName(Long sectionId, String originalName) {
        String cleanName = safeFileName(originalName).replaceAll("\\s+", "_");
        return "section-" + sectionId + "-" + System.currentTimeMillis() + "-" + cleanName;
    }

    private String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return "upload.bin";
        return new File(fileName).getName();
    }

    private Long parseSectionId(HttpServletRequest req) {
        Long sectionId = parseLong(req.getParameter("sectionId"));
        if (sectionId != null) {
            return sectionId;
        }
        return parseLong(req.getParameter("courseId"));
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String value(String text) {
        return text == null ? "" : text.trim();
    }
}
