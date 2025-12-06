document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('nav a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();

            document.querySelector(this.getAttribute('href')).scrollIntoView({
                behavior: 'smooth'
            });
        });
    });

    const splash = document.getElementById('splash-screen');
    if (splash) {
        setTimeout(() => {
            splash.remove();
        }, 3100);
    }
});
