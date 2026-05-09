(function () {
    const todayTargets = document.querySelectorAll('[data-today]');

    if (todayTargets.length) {
        const now = new Date();
        const text = now.toLocaleDateString('en-GB', {
            day: '2-digit',
            month: 'short',
            year: 'numeric'
        });
        todayTargets.forEach(node => {
            node.textContent = text;
        });
    }
})();
