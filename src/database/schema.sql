DROP DATABASE IF EXISTS sis_db;
CREATE DATABASE sis_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sis_db;

CREATE TABLE users (
    user_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE students (
    student_id BIGINT UNSIGNED PRIMARY KEY,
    student_number VARCHAR(30) NOT NULL UNIQUE,
    admission_year SMALLINT NOT NULL,
    major VARCHAR(100) NOT NULL,
    minor VARCHAR(100) NULL,
    CONSTRAINT fk_students_user
        FOREIGN KEY (student_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE instructors (
    instructor_id BIGINT UNSIGNED PRIMARY KEY,
    title VARCHAR(50) NULL,
    CONSTRAINT fk_instructors_user
        FOREIGN KEY (instructor_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE admins (
    admin_id BIGINT UNSIGNED PRIMARY KEY,
    CONSTRAINT fk_admins_user
        FOREIGN KEY (admin_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE terms (
    term_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    term_name VARCHAR(50) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    registration_start DATE NOT NULL,
    registration_end DATE NOT NULL,
    add_drop_deadline DATE NOT NULL
);

CREATE TABLE course_catalog (
    catalog_course_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(150) NOT NULL UNIQUE,
    course_abstract TEXT NULL,
    credits TINYINT UNSIGNED NULL
);

CREATE TABLE course_sections (
    section_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    catalog_course_id BIGINT UNSIGNED NOT NULL,
    term_id BIGINT UNSIGNED NOT NULL,
    section_no VARCHAR(10) NOT NULL,
    quota INT UNSIGNED NOT NULL,
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_course_sections_quota CHECK (quota >= 0),
    CONSTRAINT uq_course_section UNIQUE (catalog_course_id, term_id, section_no),
    CONSTRAINT fk_course_sections_catalog
        FOREIGN KEY (catalog_course_id) REFERENCES course_catalog(catalog_course_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_course_sections_term
        FOREIGN KEY (term_id) REFERENCES terms(term_id)
        ON DELETE RESTRICT
);

CREATE TABLE section_meetings (
    meeting_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    section_id BIGINT UNSIGNED NOT NULL,
    day_of_week ENUM('MON','TUE','WED','THU','FRI','SAT','SUN') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(50) NULL,
    CONSTRAINT fk_section_meetings_section
        FOREIGN KEY (section_id) REFERENCES course_sections(section_id)
        ON DELETE CASCADE,
    CONSTRAINT chk_section_meeting_time CHECK (end_time > start_time)
);

CREATE TABLE section_instructors (
    section_id BIGINT UNSIGNED PRIMARY KEY,
    instructor_id BIGINT UNSIGNED NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_section_instructors_section
        FOREIGN KEY (section_id) REFERENCES course_sections(section_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_section_instructors_instructor
        FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id)
        ON DELETE RESTRICT
);

CREATE TABLE enrollments (
    enrollment_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT UNSIGNED NOT NULL,
    section_id BIGINT UNSIGNED NOT NULL,
    enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ENROLLED','DROPPED','COMPLETED') NOT NULL DEFAULT 'ENROLLED',
    CONSTRAINT uq_enrollment UNIQUE (student_id, section_id),
    CONSTRAINT fk_enrollments_student
        FOREIGN KEY (student_id) REFERENCES students(student_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_section
        FOREIGN KEY (section_id) REFERENCES course_sections(section_id)
        ON DELETE CASCADE
);

CREATE TABLE grade_types (
    grade_code VARCHAR(2) PRIMARY KEY
);

CREATE TABLE grades (
    grade_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    enrollment_id BIGINT UNSIGNED NOT NULL UNIQUE,
    grade_code VARCHAR(2) NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_grades_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_grades_grade_type
        FOREIGN KEY (grade_code) REFERENCES grade_types(grade_code)
        ON DELETE RESTRICT
);

CREATE TABLE syllabuses (
    syllabus_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    section_id BIGINT UNSIGNED NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    file_type ENUM('PDF','DOCX') NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_syllabuses_section
        FOREIGN KEY (section_id) REFERENCES course_sections(section_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_course_catalog_name ON course_catalog(course_name);
CREATE INDEX idx_course_sections_term ON course_sections(term_id);
CREATE INDEX idx_course_sections_catalog ON course_sections(catalog_course_id);
CREATE INDEX idx_section_meetings_section ON section_meetings(section_id);
CREATE INDEX idx_section_instructors_instructor ON section_instructors(instructor_id);
CREATE INDEX idx_enrollments_student ON enrollments(student_id);
CREATE INDEX idx_enrollments_section ON enrollments(section_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);
CREATE INDEX idx_grades_grade_code ON grades(grade_code);

CREATE OR REPLACE VIEW student_transcript_view AS
SELECT
    s.student_id,
    s.student_number,
    u.first_name,
    u.last_name,
    s.admission_year,
    s.major,
    s.minor,
    cc.course_code,
    cc.course_name,
    t.term_name,
    g.grade_code,
    e.status
FROM students s
JOIN users u
    ON u.user_id = s.student_id
JOIN enrollments e
    ON e.student_id = s.student_id
JOIN course_sections cs
    ON cs.section_id = e.section_id
JOIN course_catalog cc
    ON cc.catalog_course_id = cs.catalog_course_id
JOIN terms t
    ON t.term_id = cs.term_id
LEFT JOIN grades g
    ON g.enrollment_id = e.enrollment_id
WHERE e.status IN ('ENROLLED', 'COMPLETED');

CREATE OR REPLACE VIEW course_listing_view AS
SELECT
    cs.section_id,
    cc.course_code,
    cc.course_name,
    cs.section_no,
    t.term_name,
    cs.quota,
    COUNT(CASE WHEN e.status = 'ENROLLED' THEN 1 END) AS enrolled_count,
    (cs.quota - COUNT(CASE WHEN e.status = 'ENROLLED' THEN 1 END)) AS available_quota,
    u.user_id AS instructor_id,
    CONCAT(u.first_name, ' ', u.last_name) AS instructor_name
FROM course_sections cs
JOIN course_catalog cc
    ON cc.catalog_course_id = cs.catalog_course_id
JOIN terms t
    ON t.term_id = cs.term_id
LEFT JOIN section_instructors si
    ON si.section_id = cs.section_id
LEFT JOIN instructors i
    ON i.instructor_id = si.instructor_id
LEFT JOIN users u
    ON u.user_id = i.instructor_id
LEFT JOIN enrollments e
    ON e.section_id = cs.section_id
GROUP BY
    cs.section_id,
    cc.course_code,
    cc.course_name,
    cs.section_no,
    t.term_name,
    cs.quota,
    u.user_id,
    u.first_name,
    u.last_name;