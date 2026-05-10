package com.sis.web;

import com.sis.instructor.model.SyllabusMetadata;
import com.sis.instructor.repository.JdbcSyllabusRepository;
import com.sis.instructor.repository.SyllabusRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@WebServlet("/syllabus")
public class SyllabusServlet extends HttpServlet {
    private final SyllabusRepository syllabusRepository = new JdbcSyllabusRepository();

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        serve(req, resp, false);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        serve(req, resp, true);
    }

    private void serve(HttpServletRequest req, HttpServletResponse resp, boolean includeBody) throws IOException {
        HttpSession session = AuthUtil.requireSession(req, resp);
        if (session == null) return;

        Long sectionId = parseLong(req.getParameter("sectionId"));
        if (sectionId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "sectionId is required");
            return;
        }

        Optional<SyllabusMetadata> optional = syllabusRepository.findBySectionId(sectionId);
        if (optional.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Syllabus not found");
            return;
        }

        SyllabusMetadata syllabus = optional.get();
        Path filePath = resolveStoredFile(syllabus);
        if (filePath == null || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Syllabus file not found on disk");
            return;
        }

        String mimeType = syllabus.getMimeType();
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = getServletContext().getMimeType(syllabus.getFileName());
        }
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "application/octet-stream";
        }

        resp.setContentType(mimeType);
        resp.setHeader("Content-Disposition", "inline; filename=\"" + safeHeaderValue(syllabus.getFileName()) + "\"");
        resp.setHeader("Content-Length", String.valueOf(Files.size(filePath)));

        if (includeBody) {
            Files.copy(filePath, resp.getOutputStream());
        }
    }

    private Path resolveStoredFile(SyllabusMetadata syllabus) {
        String fileName = syllabus.getFileName();
        if (fileName == null || fileName.isBlank()) return null;

        String realBase = getServletContext().getRealPath("/uploads/syllabuses");
        if (realBase != null && !realBase.isBlank()) {
            Path candidate = Path.of(realBase, fileName);
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        Path tempCandidate = Path.of(System.getProperty("java.io.tmpdir"), "sis-uploads", "syllabuses", fileName);
        if (Files.exists(tempCandidate)) {
            return tempCandidate;
        }

        String storedPath = syllabus.getFilePath();
        if (storedPath != null && !storedPath.isBlank()) {
            Path absoluteCandidate = Path.of(storedPath);
            if (absoluteCandidate.isAbsolute() && Files.exists(absoluteCandidate)) {
                return absoluteCandidate;
            }
        }

        return realBase == null || realBase.isBlank() ? tempCandidate : Path.of(realBase, fileName);
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String safeHeaderValue(String value) {
        return value == null ? "syllabus" : value.replace("\r", "").replace("\n", "").replace("\"", "");
    }
}
