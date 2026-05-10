(function () {
    const messageBox = document.getElementById('instructor-message');
    const rosterBody = document.getElementById('roster-body');
    const sectionSelect = document.getElementById('section-select');
    const sectionInfo = document.getElementById('section-info');
    const gradeStudentId = document.getElementById('grade-student-id');
    const syllabusFile = document.getElementById('syllabus-file');
    const loadRosterBtn = document.getElementById('load-roster-btn');
    const saveGradeBtn = document.getElementById('save-grade-btn');
    const uploadBtn = document.getElementById('upload-syllabus-btn');
    const letterGrade = document.getElementById('letter-grade');

    let assignedSections = [];
    let currentSectionId = null;
    let currentRoster = [];

    document.addEventListener('DOMContentLoaded', async () => {
        try {
            await SIS.requireSession('INSTRUCTOR');
            bindEvents();
            renderRoster();
            updateMetrics('Ready');
            await loadAssignedSections();
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    });

    function bindEvents() {
        loadRosterBtn?.addEventListener('click', loadRosterForSelectedSection);
        saveGradeBtn?.addEventListener('click', saveGrade);
        uploadBtn?.addEventListener('click', uploadSyllabus);
        sectionSelect?.addEventListener('change', () => {
            currentSectionId = selectedSectionId();
            updateSectionInfo();
            updateMetrics(currentRoster.length ? 'Section changed' : 'Ready');
        });
    }

    async function loadAssignedSections() {
        try {
            assignedSections = await SIS.apiGet('api/instructor?action=listAssignedSections');
            sectionSelect.innerHTML = '';

            if (!assignedSections.length) {
                sectionSelect.innerHTML = '<option value="">No assigned sections found</option>';
                sectionSelect.disabled = true;
                loadRosterBtn.disabled = true;
                saveGradeBtn.disabled = true;
                uploadBtn.disabled = true;
                sectionInfo.textContent = 'You do not have any assigned sections.';
                updateMetrics('No sections');
                return;
            }

            assignedSections.forEach(section => {
                const option = document.createElement('option');
                option.value = String(section.sectionId);
                option.textContent = `${section.courseCode} - ${section.courseName} - Sec ${section.sectionNo} (${section.termName})`;
                sectionSelect.appendChild(option);
            });

            currentSectionId = selectedSectionId();
            updateSectionInfo();
            updateMetrics('Sections loaded');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    }

    function selectedSectionId() {
        const value = Number(sectionSelect?.value || 0);
        return value || null;
    }

    function updateSectionInfo() {
        const section = assignedSections.find(item => Number(item.sectionId) === Number(currentSectionId));
        sectionInfo.textContent = section
            ? `Selected ${section.courseCode} section ${section.sectionNo} for ${section.termName}.`
            : 'Select one of your assigned sections.';
        document.getElementById('metric-section-id').textContent = currentSectionId || '-';
    }

    async function loadRosterForSelectedSection() {
        const sectionId = selectedSectionId();
        if (!sectionId) {
            SIS.showMessage(messageBox, 'Choose an assigned section first.', 'error');
            return;
        }
        await loadRoster(sectionId);
    }

    async function loadRoster(sectionId, silent = false) {
        try {
            currentSectionId = sectionId;
            currentRoster = await SIS.apiGet(`api/instructor?action=viewRoster&sectionId=${sectionId}`);
            renderRoster();
            updateSectionInfo();
            updateMetrics(silent ? 'Grade saved' : 'Roster loaded');

            if (!silent) {
                SIS.showMessage(
                    messageBox,
                    currentRoster.length ? 'Roster loaded successfully.' : 'No enrolled students found for this section.',
                    currentRoster.length ? 'success' : 'info'
                );
            }
        } catch (error) {
            currentRoster = [];
            renderRoster();
            updateMetrics('Roster failed');
            SIS.showMessage(messageBox, error.message, 'error');
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
        const sectionId = selectedSectionId();
        const studentId = Number(gradeStudentId.value);

        if (!sectionId || !studentId) {
            SIS.showMessage(messageBox, 'Choose a section and enter a student ID.', 'error');
            return;
        }

        try {
            const result = await SIS.apiPost('api/instructor', {
                action: 'updateGrade',
                sectionId,
                studentId,
                letterGrade: letterGrade.value
            });

            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');

            if (result.success) {
                await loadRoster(sectionId, true);
                updateMetrics('Grade saved');
            } else {
                updateMetrics('Grade failed');
            }
        } catch (error) {
            updateMetrics('Grade failed');
            SIS.showMessage(messageBox, error.message, 'error');
        }
    }

    async function uploadSyllabus() {
        const sectionId = selectedSectionId();
        if (!sectionId) {
            SIS.showMessage(messageBox, 'Choose a section first.', 'error');
            return;
        }
        if (!syllabusFile.files.length) {
            SIS.showMessage(messageBox, 'Choose a file first.', 'error');
            return;
        }

        try {
            const formData = new FormData();
            formData.append('action', 'uploadSyllabus');
            formData.append('sectionId', String(sectionId));
            formData.append('syllabusFile', syllabusFile.files[0]);
            const result = await SIS.apiPost('api/instructor', formData, true);
            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');
            syllabusFile.value = '';
            updateMetrics('Syllabus uploaded');
        } catch (error) {
            updateMetrics('Upload failed');
            SIS.showMessage(messageBox, error.message, 'error');
        }
    }

    function updateMetrics(lastAction) {
        document.getElementById('metric-section-id').textContent = currentSectionId || '-';
        document.getElementById('metric-roster-size').textContent = currentRoster.length;
        document.getElementById('metric-pending-grades').textContent = currentRoster.filter(item => !item.letterGrade || item.letterGrade === '-').length;
        document.getElementById('metric-last-action').textContent = lastAction || '-';
    }
})();
