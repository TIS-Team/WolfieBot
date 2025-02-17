document.addEventListener("DOMContentLoaded", function() {
    const sidebar = document.querySelector('.sidebar');
    const sidebarButton = document.querySelector('.sidebar-button');

    function updateSidebarButtonVisibility() {
        if (sidebar.children.length > 0) {
            sidebarButton.classList.add('visible');
        } else {
            sidebarButton.classList.remove('visible');
        }
    }

    updateSidebarButtonVisibility();

    const observer = new MutationObserver(function(mutationsList) {
        for (const mutation of mutationsList) {
            if (mutation.type === 'childList') {
                updateSidebarButtonVisibility();
                break;
            }
        }
    });

    observer.observe(sidebar, { childList: true, subtree: false });
});