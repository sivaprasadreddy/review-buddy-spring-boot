const menuButton = document.getElementById('mobile-menu-button');
const menu = document.getElementById('mobile-menu');
const userMenu = document.getElementById('user-menu');

function closeMobileMenu() {
    menu.classList.add('hidden');
    menu.classList.remove('flex');
    menuButton.setAttribute('aria-expanded', 'false');
}

menuButton.addEventListener('click', () => {
    const expanded = menuButton.getAttribute('aria-expanded') !== 'true';
    menu.classList.toggle('hidden', !expanded);
    menu.classList.toggle('flex', expanded);
    menuButton.setAttribute('aria-expanded', String(expanded));
});

document.addEventListener('click', (event) => {
    if (userMenu && !userMenu.contains(event.target)) userMenu.open = false;
});

document.addEventListener('keydown', (event) => {
    if (event.key !== 'Escape') return;
    if (userMenu?.open) {
        userMenu.open = false;
        userMenu.querySelector('summary').focus();
    } else if (menuButton.getAttribute('aria-expanded') === 'true') {
        closeMobileMenu();
        menuButton.focus();
    }
});

document.querySelectorAll('[data-dismiss-alert]').forEach((button) => {
    button.addEventListener('click', () => button.closest('[role]').parentElement.remove());
});
