# Estructura del proyecto

## Java / Spring Boot

Todo el codigo Java pertenece bajo `src/main/java/com/example/demo/`, que es el
paquete base escaneado por Spring Boot.

- `controller/`: pagina principal, salud de la aplicacion y manejo de errores.
- `config/`: configuracion transversal de CORS.
- `service/`: reglas de negocio y controladores de especies, cultivos y mediciones.
- `repository/`: consultas JDBC y persistencia en PostgreSQL.
- `model/`: objetos que representan especies, cultivos, mediciones y alertas.

## Front-end

- HTML estatico: `src/main/resources/static/index.html`.
- CSS: `src/main/resources/static/css/style.css`.
- JavaScript: `src/main/resources/static/js/`.
- `app.js` inicializa el estado, la navegacion y los paneles.
- `api.js` centraliza las peticiones JSON.
- `section-loader.js` muestra un solo panel y actualiza sus atributos ARIA.
- `sections/` contiene la logica de inicio, especies, cultivos y mediciones.

Los recursos de `static` se publican desde la raiz del sitio. Por ejemplo,
`static/css/style.css` se solicita como `/css/style.css`.

## API

- `GET /api/health`: ejecuta una consulta en PostgreSQL y devuelve el estado y
  el nombre de la base conectada.
- `GET` y `POST /especies`: consulta y registra especies con sus rangos.
- `GET` y `POST /cultivos`: consulta y registra cultivos.
- `GET` y `POST /mediciones`: consulta y registra lecturas y alertas.

## Puerto y base de datos

El puerto predeterminado es **9090**; abre `http://localhost:9090/`. Puede
cambiarse con la variable `SERVER_PORT`.

La aplicacion usa PostgreSQL en `127.0.0.1:5432`, base `sistema de riego` y
usuario predeterminado `postgres`. `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`
permiten cambiar estos valores. `schema.sql` crea las tablas al iniciar.

Consulta `README.md` para crear la base, iniciar el programa y replicarlo en
otra maquina. No guardes contrasenas en el codigo ni en este archivo.
