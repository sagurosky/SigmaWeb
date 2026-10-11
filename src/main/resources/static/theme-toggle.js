/**
 * SigmaWeb Theme Toggle Script (Light / Dark mode switcher)
 */
(function() {
    const THEME_KEY = 'sigma-theme-mode';

    function getPreferredTheme() {
        const storedTheme = localStorage.getItem(THEME_KEY);
        if (storedTheme) {
            return storedTheme;
        }
        return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        document.body.setAttribute('data-theme', theme);
        localStorage.setItem(THEME_KEY, theme);
        
        // Update toggle icons if present
        const iconElement = document.getElementById('theme-toggle-icon');
        if (iconElement) {
            if (theme === 'dark') {
                iconElement.className = 'fas fa-sun';
                iconElement.setAttribute('title', 'Cambiar a Modo Claro');
            } else {
                iconElement.className = 'fas fa-moon';
                iconElement.setAttribute('title', 'Cambiar a Modo Oscuro');
            }
        }
    }

    // Apply theme immediately before DOM content loads to avoid flash
    const initialTheme = getPreferredTheme();
    document.documentElement.setAttribute('data-theme', initialTheme);

    window.toggleSigmaTheme = function() {
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        applyTheme(newTheme);
    };

    document.addEventListener('DOMContentLoaded', function() {
        applyTheme(getPreferredTheme());
    });
})();
