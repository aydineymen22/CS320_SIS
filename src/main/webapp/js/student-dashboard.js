(function () {
    const messageBox = document.getElementById('student-message');
    const courseBody = document.getElementById('course-catalog-body');
    const catalogBody = document.getElementById('catalog-body');
    const scheduleBody = document.getElementById('schedule-body');
    const transcriptBody = document.getElementById('transcript-body');
    const searchInput = document.getElementById('course-search');
    const catalogSearchInput = document.getElementById('catalog-search');
    const searchBtn = document.getElementById('search-courses-btn');
    const catalogSearchBtn = document.getElementById('search-catalog-btn');
    const resetBtn = document.getElementById('reset-courses-btn');
    const catalogResetBtn = document.getElementById('reset-catalog-btn');
    const addBtn = document.getElementById('add-course-btn');
    const dropBtn = document.getElementById('drop-course-btn');
    const syllabusLink = document.getElementById('detail-syllabus-link');

    let selectedSectionId = null;
    let courses = [];
    let catalogCourses = [];
    let schedule = [];
    let offeredLoaded = false;
    let catalogLoaded = false;

    document.addEventListener('DOMContentLoaded', async () => {
        try {
            await SIS.requireSession('STUDENT');
            bindEvents();
            clearCatalog();
            clearOfferedCourses();
            await Promise.all([loadSchedule(), loadTranscript()]);
            await renderSelectedCourse();
            updateMetrics();
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    });

    function bindEvents() {
        searchBtn?.addEventListener('click', () => {
            const query = searchInput.value.trim();
            if (!query) {
                clearOfferedCourses();
                renderSelectedCourse();
                updateMetrics();
                return;
            }
            refreshOfferedCourses(query);
        });
        resetBtn?.addEventListener('click', () => {
            searchInput.value = '';
            showAllOfferedCourses();
        });
        catalogSearchBtn?.addEventListener('click', () => {
            const query = catalogSearchInput.value.trim();
            if (!query) {
                clearCatalog();
                return;
            }
            refreshCatalog(query);
        });
        catalogResetBtn?.addEventListener('click', () => {
            catalogSearchInput.value = '';
            showAllCatalogCourses();
        });
        addBtn?.addEventListener('click', () => submitEnrollmentAction('enroll'));
        dropBtn?.addEventListener('click', () => submitEnrollmentAction('drop'));
    }

    function clearOfferedCourses() {
        courses = [];
        offeredLoaded = false;
        selectedSectionId = null;
        renderCourses();
    }

    function clearCatalog() {
        catalogCourses = [];
        catalogLoaded = false;
        renderCatalog();
    }

    async function showAllOfferedCourses() {
        await refreshOfferedCourses('');
    }

    async function showAllCatalogCourses() {
        await refreshCatalog('');
    }

    async function refreshOfferedCourses(query) {
        await Promise.all([loadCourses(query), loadSchedule(), loadTranscript()]);
        syncSelection();
        await renderSelectedCourse();
        updateMetrics();
    }

    async function refreshCatalog(query) {
        await loadCatalog(query);
    }

    async function loadCourses(query) {
        const url = query
            ? `api/student?action=search&query=${encodeURIComponent(query)}`
            : 'api/student?action=viewAvailableCourses';
        courses = await SIS.apiGet(url);
        offeredLoaded = true;
        renderCourses();
    }

    async function loadCatalog(query) {
        const url = query
            ? `api/student?action=getCatalog&query=${encodeURIComponent(query)}`
            : 'api/student?action=getCatalog';
        catalogCourses = await SIS.apiGet(url);
        catalogLoaded = true;
        renderCatalog();
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
            courseBody.innerHTML = offeredLoaded
                ? '<tr><td colspan="5" class="empty-state">No courses found.</td></tr>'
                : '<tr><td colspan="5" class="empty-state">Search offered courses to see results.</td></tr>';
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

    function renderCatalog() {
        if (!catalogCourses.length) {
            catalogBody.innerHTML = catalogLoaded
                ? '<tr><td colspan="4" class="empty-state">No catalog courses found.</td></tr>'
                : '<tr><td colspan="4" class="empty-state">Search the catalog to see results.</td></tr>';
            return;
        }

        catalogBody.innerHTML = catalogCourses.map(item => `
            <tr>
                <td>${SIS.escapeHtml(item.courseCode)}</td>
                <td>${SIS.escapeHtml(item.courseName)}</td>
                <td>${item.credits ?? '-'}</td>
                <td>${SIS.escapeHtml(item.courseAbstract || '-')}</td>
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
            await fillDetailFields(null);
            return;
        }

        if (detail) {
            await fillDetailFields(detail);
            return;
        }

        try {
            const fetched = await SIS.apiGet(`api/student?action=getCourseDetails&sectionId=${selectedSectionId}`);
            await fillDetailFields(fetched);
        } catch (error) {
            await fillDetailFields(null);
        }
    }

    function getSelectedCourseFromState() {
        return courses.find(item => item.sectionId === selectedSectionId)
            || schedule.find(item => item.sectionId === selectedSectionId)
            || null;
    }

    async function fillDetailFields(course) {
        document.getElementById('detail-course-code').textContent = course?.courseCode || '-';
        document.getElementById('detail-course-name').textContent = course?.courseName || '-';
        document.getElementById('detail-instructor').textContent = course?.instructorName || '-';
        document.getElementById('detail-section').textContent = course?.sectionNo || '-';
        document.getElementById('detail-term').textContent = course?.termName || '-';
        document.getElementById('detail-quota').textContent = course ? String(course.availableQuota ?? 0) : '-';
        await updateSyllabusLink(course?.sectionId);
    }

    async function updateSyllabusLink(sectionId) {
        if (!syllabusLink || !sectionId) {
            setSyllabusUnavailable();
            return;
        }

        const url = `syllabus?sectionId=${encodeURIComponent(sectionId)}`;
        try {
            const response = await fetch(url, {
                method: 'HEAD',
                credentials: 'same-origin'
            });

            if (response.ok) {
                syllabusLink.textContent = 'Open syllabus';
                syllabusLink.href = url;
                syllabusLink.classList.remove('disabled');
                syllabusLink.removeAttribute('aria-disabled');
                return;
            }
        } catch (error) {
            // ignore and fall back to disabled state
        }

        setSyllabusUnavailable();
    }

    function setSyllabusUnavailable() {
        if (!syllabusLink) return;
        syllabusLink.textContent = 'Not available';
        syllabusLink.href = '#';
        syllabusLink.classList.add('disabled');
        syllabusLink.setAttribute('aria-disabled', 'true');
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
            const offeredQuery = searchInput.value.trim();
            if (offeredQuery) {
                await refreshOfferedCourses(offeredQuery);
            } else {
                await showAllOfferedCourses();
            }
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
