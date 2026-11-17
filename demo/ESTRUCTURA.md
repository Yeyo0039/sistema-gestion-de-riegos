# Estructura inicial

## Java / Spring Boot

Todo el codigo Java pertenece bajo `src/main/java/com/example/demo/`, que es el
paquete base escaneado por Spring Boot.

- `controller/`: rutas de paginas y endpoints REST.
- `service/`: reglas y logica de negocio.
- `repository/`: lectura y escritura de datos.
- `model/`: modelos del dominio.
- `dto/`: datos de entrada y salida de la API.
- `config/`: configuracion transversal, incluido CORS.

Spring Boot expone la API JSON desde los controladores de `controller/`. La
logica de negocio se mantiene en `service/` y el acceso a PostgreSQL en
`repository/`.

## Front-end

- HTML estatico: `src/main/resources/static/index.html`.
- CSS: `src/main/resources/static/css/style.css`.
- JavaScript: `src/main/resources/static/js/`.
- Las peticiones HTTP JSON compartidas estan en `static/js/api.js`; `app.js`
  envia los formularios como JSON y muestra las respuestas de la API.

Los recursos de `static` se publican desde la raiz del sitio: por ejemplo,
`static/css/style.css` se solicita como `/css/style.css`.

## API

- `GET /api/health`: ejecuta una consulta en PostgreSQL y devuelve el nombre de
  la base conectada.
- `GET /api/tablas`: devuelve un objeto JSON con los registros de todas las
  tablas reales de `public`.
- `GET /api/tablas/{nombre}`: devuelve como JSON los registros de una tabla
  (`tipo`, `tipo_sustrato`, `especie`, `temperatura` o `etapa`).
- `GET` y `POST /api/species`: consulta y registra especies.
- `GET` y `POST /api/crops`: consulta y registra cultivos.
- `GET` y `POST /api/measurements`: consulta y registra mediciones.

Las respuestas de `/api/tablas` conservan los nombres de columnas de
PostgreSQL, por ejemplo `tipo_id`, `tipo_sustrato_id` y `optima_minima`.
Los endpoints de registro reciben `Content-Type: application/json`.

## Puerto y CORS

El puerto configurado en `application.properties` es **9090**; abre
`http://localhost:9090/`. CORS solo se necesita si el front-end se ejecuta en
otro origen. La configuracion inicial permite el Live Server local de VS Code
en el puerto 5500 y se aplica solo a `/api/**`.

## Base de datos

La aplicacion usa PostgreSQL en `127.0.0.1:5432`, base `sistema de riego` y
usuario predeterminado `postgres`; `DB_URL` y `DB_USERNAME` permiten cambiar
estos valores. Para ejecutar desde VS Code, copia `.env.example` a `.env` en
esta carpeta y define alli `DB_PASSWORD`; `.env` esta excluido de Git. No
guardes contrasenas en el codigo ni en este archivo. `/api/health` confirma la
conexion ejecutando una consulta real a la base.
