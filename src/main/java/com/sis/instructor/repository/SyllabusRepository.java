package com.sis.instructor.repository;

import com.sis.instructor.model.SyllabusMetadata;
import java.util.Optional;

public interface SyllabusRepository {
    RepositoryChangeType saveOrReplaceSyllabus(
            Long sectionId,
            String fileName,
            String fileType,
            String mimeType,
            String filePath);

    Optional<SyllabusMetadata> findBySectionId(Long sectionId);
}
