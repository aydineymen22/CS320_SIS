package com.sis.web;

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
        if (!"viewRoster".equals(action)) {
            AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
            return;
        }

        Long courseId = parseLong(req.getParameter("courseId"));
        if (courseId == null) {
            AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "courseId is required");
            return;
        }

        List<RosterStudent> roster = instructorController.viewRoster(instructorId, courseId);
        AuthUtil.writeJson(resp, JsonUtil.roster(roster));
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
        Long courseId = parseLong(req.getParameter("courseId"));
        Long studentId = parseLong(req.getParameter("studentId"));
        String grade = value(req.getParameter("letterGrade"));

        if (courseId == null || studentId == null || grade.isEmpty()) {
            AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "courseId, studentId and letterGrade are required");
            return;
        }

        String result = instructorController.updateGrade(instructorId, studentId, courseId, grade);
        AuthUtil.writeJson(resp, JsonUtil.message(result));
    }

    private void handleSyllabusUpload(HttpServletRequest req, HttpServletResponse resp, Long instructorId) throws IOException, ServletException {
        Long courseId = parseLong(req.getParameter("courseId"));
        Part filePart = req.getPart("syllabusFile");
        if (courseId == null || filePart == null || filePart.getSize() == 0) {
            AuthUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "courseId and syllabusFile are required");
            return;
        }

        String originalName = safeFileName(filePart.getSubmittedFileName());
        Path uploadDirectory = resolveUploadDirectory();
        Files.createDirectories(uploadDirectory);

        String storedName = buildStoredFileName(courseId, originalName);
        Path storedFile = uploadDirectory.resolve(storedName);

        try (InputStream inputStream = filePart.getInputStream()) {
            Files.copy(inputStream, storedFile, StandardCopyOption.REPLACE_EXISTING);
            String result = instructorController.uploadSyllabus(instructorId, courseId, storedFile.toFile());
            if (!result.toLowerCase().startsWith("success")) {
                Files.deleteIfExists(storedFile);
            }
            AuthUtil.writeJson(resp, JsonUtil.message(result));
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(storedFile);
            throw e;
        }
    }

    private Path resolveUploadDirectory() {
        String realPath = getServletContext().getRealPath("/uploads/syllabuses");
        if (realPath != null && !realPath.isBlank()) {
            return Path.of(realPath);
        }
        return Path.of(System.getProperty("java.io.tmpdir"), "sis-uploads", "syllabuses");
    }

    private String buildStoredFileName(Long courseId, String originalName) {
        String cleanName = safeFileName(originalName).replaceAll("\\s+", "_");
        return "section-" + courseId + "-" + System.currentTimeMillis() + "-" + cleanName;
    }

    private String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return "upload.bin";
        return new File(fileName).getName();
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
