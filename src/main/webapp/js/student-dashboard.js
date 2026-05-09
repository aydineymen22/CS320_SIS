(function () {
    const messageBox = document.getElementById('student-message');
    const courseBody = document.getElementById('course-catalog-body');
    const scheduleBody = document.getElementById('schedule-body');
    const transcriptBody = document.getElementById('transcript-body');
    const searchInput = document.getElementById('course-search');
    const searchBtn = document.getElementById('search-courses-btn');
    const resetBtn = document.getElementById('reset-courses-btn');
    const addBtn = document.getElementById('add-course-btn');
    const dropBtn = document.getElementById('drop-course-btn');

    let selectedSectionId = null;
    let courses = [];
    let schedule = [];

    document.addEventListener('DOMContentLoaded', async () => {
        try {
            await SIS.requireSession('STUDENT');
            bindEvents();
            await refreshAll('');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    });

    function bindEvents() {
        searchBtn?.addEventListener('click', () => refreshAll(searchInput.value.trim()));
        resetBtn?.addEventListener('click', () => {
            searchInput.value = '';
            refreshAll('');
        });
        addBtn?.addEventListener('click', () => submitEnrollmentAction('enroll'));
        dropBtn?.addEventListener('click', () => submitEnrollmentAction('drop'));
    }

    async function refreshAll(query) {
        await Promise.all([loadCourses(query), loadSchedule(), loadTranscript()]);
        syncSelection();
        renderSelectedCourse();
        updateMetrics();
    }

    async function loadCourses(query) {
        const url = query
            ? `api/student?action=search&query=${encodeURIComponent(query)}`
            : 'api/student?action=viewAvailableCourses';
        courses = await SIS.apiGet(url);
        renderCourses();
    }

    async function loadSchedule() {
        schedule = await SIS.apiGet('api/student?action=getSchedule');
        renderSchedule();
    }

    async function loadTranscript() {
        const transcript = await SIS.apiGet('api/student?action=getTranscript');
        renderTranscript(transcript);
    }

    function renderCourses() {
        if (!courses.length) {
            courseBody.innerHTML = '<tr><td colspan="5" class="empty-state">No courses found.</td></tr>';
            return;
        }

        courseBody.innerHTML = courses.map(item => `
            <tr data-section-id="${item.sectionId}" class="${item.sectionId === selectedSectionId ? 'selected-row' : ''}">
                <td><span class="course-code">${SIS.escapeHtml(item.courseCode)}</span><br><span class="muted">${SIS.escapeHtml(item.courseName)}</span></td>
                <td>${SIS.escapeHtml(item.instructorName || '-') }</td>
                <td>${SIS.escapeHtml(item.sectionNo || '-')}</td>
                <td>${SIS.escapeHtml(item.termName || '-')}</td>
                <td>${item.availableQuota ?? 0}</td>
            </tr>
        `).join('');

        courseBody.querySelectorAll('tr[data-section-id]').forEach(row => {
            row.addEventListener('click', async () => {
                selectedSectionId = Number(row.dataset.sectionId);
                renderCourses();
                await renderSelectedCourse();
            });
        });
    }

    function renderSchedule() {
        if (!schedule.length) {
            scheduleBody.innerHTML = '<tr><td colspan="4" class="empty-state">No enrolled courses.</td></tr>';
            return;
        }

        scheduleBody.innerHTML = schedule.map(item => `
            <tr>
                <td>${SIS.escapeHtml(item.courseCode)}</td>
                <td>${SIS.escapeHtml(item.courseName)}</td>
                <td>${SIS.escapeHtml(item.sectionNo || '-')}</td>
                <td>${SIS.escapeHtml(item.termName || '-')}</td>
            </tr>
        `).join('');
    }

    function renderTranscript(entries) {
        if (!entries.length) {
            transcriptBody.innerHTML = '<tr><td colspan="3" class="empty-state">No transcript records found.</td></tr>';
            return;
        }

        transcriptBody.innerHTML = entries.map(item => `
            <tr>
                <td>${SIS.escapeHtml(item.courseCode)}</td>
                <td>${SIS.escapeHtml(item.courseName)}</td>
                <td>${SIS.escapeHtml(item.grade || 'N/A')}</td>
            </tr>
        `).join('');
    }

    async function renderSelectedCourse() {
        const detail = getSelectedCourseFromState();
        if (!selectedSectionId) {
            fillDetailFields(null);
            return;
        }

        if (detail) {
            fillDetailFields(detail);
            return;
        }

        try {
            const fetched = await SIS.apiGet(`api/student?action=getCourseDetails&sectionId=${selectedSectionId}`);
            fillDetailFields(fetched);
        } catch (error) {
            fillDetailFields(null);
        }
    }

    function getSelectedCourseFromState() {
        return courses.find(item => item.sectionId === selectedSectionId)
            || schedule.find(item => item.sectionId === selectedSectionId)
            || null;
    }

    function fillDetailFields(course) {
        document.getElementById('detail-course-code').textContent = course?.courseCode || '-';
        document.getElementById('detail-course-name').textContent = course?.courseName || '-';
        document.getElementById('detail-instructor').textContent = course?.instructorName || '-';
        document.getElementById('detail-section').textContent = course?.sectionNo || '-';
        document.getElementById('detail-term').textContent = course?.termName || '-';
        document.getElementById('detail-quota').textContent = course ? String(course.availableQuota ?? 0) : '-';
    }

    function syncSelection() {
        if (selectedSectionId && courses.some(item => item.sectionId === selectedSectionId)) {
            return;
        }
        selectedSectionId = courses.length ? courses[0].sectionId : null;
    }

    async function submitEnrollmentAction(action) {
        if (!selectedSectionId) {
            SIS.showMessage(messageBox, 'Select a course first.', 'error');
            return;
        }

        try {
            const result = await SIS.apiPost('api/student', {
                action,
                sectionId: selectedSectionId
            });
            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');
            await refreshAll(searchInput.value.trim());
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    }

    function updateMetrics() {
        document.getElementById('metric-current-courses').textContent = schedule.length;
        document.getElementById('metric-current-credits').textContent = schedule.length * 3;
        document.getElementById('metric-open-seats').textContent = courses.reduce((sum, item) => sum + Number(item.availableQuota || 0), 0);
        document.getElementById('metric-transcript-count').textContent = transcriptBody.querySelectorAll('tr').length === 1 && transcriptBody.textContent.includes('No transcript') ? 0 : transcriptBody.querySelectorAll('tr').length;
    }
})();
