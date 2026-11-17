# Secciones del front-end

El esqueleto y los formularios de todas las secciones están en
`src/main/resources/static/index.html`. El módulo
`src/main/resources/static/js/section-loader.js` solo controla cuál sección
permanece visible dentro del dashboard; `app.js` gestiona la navegación, las
peticiones HTTP JSON y la presentación de los datos de la API.

Las llamadas HTTP compartidas van en `src/main/resources/static/js/api.js`.
