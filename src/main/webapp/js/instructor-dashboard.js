(function () {
    const messageBox = document.getElementById('instructor-message');
    const rosterBody = document.getElementById('roster-body');
    const rosterCourseId = document.getElementById('roster-course-id');
    const gradeCourseId = document.getElementById('grade-course-id');
    const gradeStudentId = document.getElementById('grade-student-id');
    const syllabusCourseId = document.getElementById('syllabus-course-id');
    const syllabusFile = document.getElementById('syllabus-file');
    const loadRosterBtn = document.getElementById('load-roster-btn');
    const saveGradeBtn = document.getElementById('save-grade-btn');
    const uploadBtn = document.getElementById('upload-syllabus-btn');
    const letterGrade = document.getElementById('letter-grade');

    let currentCourseId = null;
    let currentRoster = [];

    document.addEventListener('DOMContentLoaded', async () => {
        try {
            await SIS.requireSession('INSTRUCTOR');
            bindEvents();
            renderRoster();
            updateMetrics('Ready');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    });

    function bindEvents() {
        loadRosterBtn?.addEventListener('click', loadRosterFromInput);
        saveGradeBtn?.addEventListener('click', saveGrade);
        uploadBtn?.addEventListener('click', uploadSyllabus);
    }

    async function loadRosterFromInput() {
        const courseId = Number(rosterCourseId.value);
        if (!courseId) {
            SIS.showMessage(messageBox, 'Enter a course ID first.', 'error');
            return;
        }
        await loadRoster(courseId);
    }

    async function loadRoster(courseId) {
        try {
            currentCourseId = courseId;
            rosterCourseId.value = String(courseId);
            gradeCourseId.value = String(courseId);
            syllabusCourseId.value = String(courseId);
            currentRoster = await SIS.apiGet(`api/instructor?action=viewRoster&courseId=${courseId}`);
            renderRoster();
            updateMetrics('Roster loaded');
            SIS.showMessage(messageBox, currentRoster.length ? 'Roster loaded successfully.' : 'No enrolled students found or access denied for this course.', currentRoster.length ? 'success' : 'info');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
            currentRoster = [];
            renderRoster();
            updateMetrics('Roster failed');
        }
    }

    function renderRoster() {
        if (!currentRoster.length) {
            rosterBody.innerHTML = '<tr><td colspan="4" class="empty-state">No roster loaded.</td></tr>';
            return;
        }

        rosterBody.innerHTML = currentRoster.map(item => `
            <tr data-student-id="${item.studentId}">
                <td>${SIS.escapeHtml(item.studentNumber)}</td>
                <td>${SIS.escapeHtml(item.firstName)} ${SIS.escapeHtml(item.lastName)}</td>
                <td>${SIS.escapeHtml(item.email)}</td>
                <td><span class="mini-tag">${SIS.escapeHtml(item.letterGrade || '-')}</span></td>
            </tr>
        `).join('');

        rosterBody.querySelectorAll('tr[data-student-id]').forEach(row => {
            row.addEventListener('click', () => {
                gradeStudentId.value = row.dataset.studentId;
            });
        });
    }

    async function saveGrade() {
        const courseId = Number(gradeCourseId.value);
        const studentId = Number(gradeStudentId.value);
        if (!courseId || !studentId) {
            SIS.showMessage(messageBox, 'Enter both course ID and student ID.', 'error');
            return;
        }

        try {
            const result = await SIS.apiPost('api/instructor', {
                action: 'updateGrade',
                courseId,
                studentId,
                letterGrade: letterGrade.value
            });
            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');
            await loadRoster(courseId);
            updateMetrics('Grade saved');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
            updateMetrics('Grade failed');
        }
    }

    async function uploadSyllabus() {
        const courseId = Number(syllabusCourseId.value);
        if (!courseId) {
            SIS.showMessage(messageBox, 'Enter a course ID first.', 'error');
            return;
        }
        if (!syllabusFile.files.length) {
            SIS.showMessage(messageBox, 'Choose a file first.', 'error');
            return;
        }

        try {
            const formData = new FormData();
            formData.append('action', 'uploadSyllabus');
            formData.append('courseId', String(courseId));
            formData.append('syllabusFile', syllabusFile.files[0]);
            const result = await SIS.apiPost('api/instructor', formData, true);
            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');
            syllabusFile.value = '';
            updateMetrics('Syllabus uploaded');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
            updateMetrics('Upload failed');
        }
    }

    function updateMetrics(lastAction) {
        document.getElementById('metric-course-id').textContent = currentCourseId || '-';
        document.getElementById('metric-roster-size').textContent = currentRoster.length;
        document.getElementById('metric-pending-grades').textContent = currentRoster.filter(item => !item.letterGrade || item.letterGrade === '-').length;
        document.getElementById('metric-last-action').textContent = lastAction || '-';
    }
})();
