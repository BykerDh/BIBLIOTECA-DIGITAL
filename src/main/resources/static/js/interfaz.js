// Mejora la navegación sin cambiar las rutas ni los formularios del servidor.
document.documentElement.classList.add('js');
const ruta = window.location.pathname;
document.querySelectorAll('[data-nav]').forEach(enlace => {
    const destino = enlace.dataset.nav;
    const activa = destino === '/' ? ruta === '/' : ruta === destino || ruta.startsWith(destino + '/');
    if (activa || (destino === '/mi-cuenta' && ruta === '/planes')) enlace.setAttribute('aria-current', 'page');
});
const menu = document.querySelector('#navegacion');
const boton = document.querySelector('[data-menu]');
boton?.addEventListener('click', () => {
    const abierto = boton.getAttribute('aria-expanded') !== 'true';
    boton.setAttribute('aria-expanded', String(abierto));
    menu.classList.toggle('is-open', abierto);
});
document.addEventListener('keydown', evento => {
    if (evento.key === 'Escape' && boton?.getAttribute('aria-expanded') === 'true') {
        boton.setAttribute('aria-expanded', 'false');
        menu.classList.remove('is-open');
        boton.focus();
    }
});
document.querySelectorAll('[data-password]').forEach(boton => {
    boton.addEventListener('click', () => {
        const campo = document.getElementById(boton.dataset.password);
        const mostrar = campo.type === 'password';
        campo.type = mostrar ? 'text' : 'password';
        boton.textContent = mostrar ? 'Ocultar' : 'Mostrar';
        boton.setAttribute('aria-pressed', String(mostrar));
    });
});
