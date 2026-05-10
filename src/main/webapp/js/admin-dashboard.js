(function () {
    const messageBox = document.getElementById('admin-message');
    const createUserBtn = document.getElementById('create-user-btn');
    const updateUserBtn = document.getElementById('update-user-btn');
    const deactivateUserBtn = document.getElementById('deactivate-user-btn');
    const resetPasswordBtn = document.getElementById('reset-password-btn');
    const createCourseBtn = document.getElementById('create-course-btn');
    const updateCourseBtn = document.getElementById('update-course-btn');
    const assignInstructorBtn = document.getElementById('assign-instructor-btn');
    const removeInstructorBtn = document.getElementById('remove-instructor-btn');

    document.addEventListener('DOMContentLoaded', async () => {
        try {
            await SIS.requireSession('ADMIN');
            bindEvents();
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    });

    function bindEvents() {
        createUserBtn?.addEventListener('click', createUser);
        updateUserBtn?.addEventListener('click', updateUser);
        deactivateUserBtn?.addEventListener('click', deactivateUser);
        resetPasswordBtn?.addEventListener('click', resetPassword);
        createCourseBtn?.addEventListener('click', createCourse);
        updateCourseBtn?.addEventListener('click', updateCourse);
        assignInstructorBtn?.addEventListener('click', assignInstructor);
        removeInstructorBtn?.addEventListener('click', removeInstructor);
    }

    async function createUser() {
        try {
            const result = await SIS.apiPost('api/admin', {
                action: 'createUser',
                role: valueOf('user-role'),
                firstName: valueOf('first-name'),
                lastName: valueOf('last-name'),
                email: valueOf('email-address')
            });
            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');
            clearFields('first-name', 'last-name', 'email-address');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    }

    async function updateUser() {
        try {
            const result = await SIS.apiPost('api/admin', {
                action: 'updateUser',
                userId: valueOf('manage-user-id'),
                firstName: valueOf('manage-first-name'),
                lastName: valueOf('manage-last-name')
            });
            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    }

    async function deactivateUser() {
        try {
            const result = await SIS.apiPost('api/admin', {
                action: 'deactivateUser',
                userId: valueOf('manage-user-id')
            });
            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    }

    async function resetPassword() {
        try {
            const result = await SIS.apiPost('api/admin', {
                action: 'resetPassword',
                userId: valueOf('manage-user-id')
            });
            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    }

    async function createCourse() {
        try {
            const result = await SIS.apiPost('api/admin', {
                action: 'createCourse',
                courseCode: valueOf('new-course-code'),
                courseName: valueOf('new-course-name'),
                quota: valueOf('new-course-quota')
            });
            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');
            clearFields('new-course-code', 'new-course-name', 'new-course-quota');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    }

    async function updateCourse() {
        try {
            const result = await SIS.apiPost('api/admin', {
                action: 'updateCourse',
                courseId: valueOf('update-course-id'),
                newName: valueOf('update-course-name'),
                newQuota: valueOf('update-course-quota')
            });
            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    }

    async function assignInstructor() {
        try {
            const result = await SIS.apiPost('api/admin', {
                action: 'assignInstructor',
                courseId: valueOf('assignment-course-id'),
                instructorId: valueOf('assignment-instructor-id')
            });
            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    }

    async function removeInstructor() {
        try {
            const result = await SIS.apiPost('api/admin', {
                action: 'removeInstructor',
                courseId: valueOf('assignment-course-id')
            });
            SIS.showMessage(messageBox, result.message, result.success ? 'success' : 'error');
        } catch (error) {
            SIS.showMessage(messageBox, error.message, 'error');
        }
    }

    function valueOf(id) {
        return document.getElementById(id)?.value?.trim() || '';
    }

    function clearFields(...ids) {
        ids.forEach(id => {
            const node = document.getElementById(id);
            if (node) node.value = '';
        });
    }
})();
