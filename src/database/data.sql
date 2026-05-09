USE sis_db;

INSERT INTO grade_types (grade_code) VALUES
('AA'),
('BA'),
('BB'),
('CB'),
('CC'),
('DC'),
('DD'),
('FD'),
('FF');

INSERT INTO users (email, password_hash, first_name, last_name)
VALUES
('student1@ozu.edu.tr', 'hashed_pw_1', 'Ali', 'Yilmaz'),
('student2@ozu.edu.tr', 'hashed_pw_2', 'Zeynep', 'Kara'),
('instructor1@ozu.edu.tr', 'hashed_pw_3', 'Ayse', 'Demir'),
('admin1@ozu.edu.tr', 'hashed_pw_4', 'Mehmet', 'Kaya');

INSERT INTO students (student_id, student_number, admission_year, major, minor)
VALUES
(1, '20230001', 2023, 'Computer Engineering', 'Mathematics'),
(2, '20230002', 2023, 'Computer Engineering', NULL);

INSERT INTO instructors (instructor_id, title)
VALUES
(3, 'Dr.');

INSERT INTO admins (admin_id)
VALUES
(4);

INSERT INTO terms (term_name, start_date, end_date, registration_start, registration_end, add_drop_deadline)
VALUES
('2026-Spring', '2026-02-10', '2026-06-10', '2026-01-20', '2026-02-05', '2026-02-20');

INSERT INTO course_catalog (course_code, course_name, course_abstract, credits)
VALUES
('CS320', 'Software Engineering', 'Introduction to software engineering principles.', 4),
('CS210', 'Database Systems', 'Relational databases and SQL fundamentals.', 4),
('MATH201', 'Discrete Mathematics', 'Logic, relations, graphs and combinatorics.', 3);

INSERT INTO course_sections (catalog_course_id, term_id, section_no, quota, is_open)
VALUES
(1, 1, '1', 50, TRUE),
(2, 1, '1', 40, TRUE),
(3, 1, '1', 35, TRUE);

INSERT INTO section_meetings (section_id, day_of_week, start_time, end_time, room)
VALUES
(1, 'MON', '10:40:00', '11:40:00', 'AB1.237'),
(1, 'WED', '10:40:00', '11:40:00', 'AB1.409'),
(2, 'TUE', '13:40:00', '14:40:00', 'AB1.225'),
(3, 'THU', '09:40:00', '10:40:00', 'AB1.241');

INSERT INTO section_instructors (section_id, instructor_id)
VALUES
(1, 3),
(2, 3),
(3, 3);

INSERT INTO enrollments (student_id, section_id, status)
VALUES
(1, 1, 'ENROLLED'),
(1, 2, 'COMPLETED'),
(2, 1, 'ENROLLED'),
(2, 3, 'ENROLLED');

INSERT INTO grades (enrollment_id, grade_code)
VALUES
(2, 'B');

INSERT INTO syllabuses (section_id, file_name, file_type, mime_type, file_path)
VALUES
(1, 'cs320_syllabus.pdf', 'PDF', 'application/pdf', '/uploads/syllabuses/cs320_syllabus.pdf'),
(2, 'cs210_syllabus.docx', 'DOCX', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', '/uploads/syllabuses/cs210_syllabus.docx');