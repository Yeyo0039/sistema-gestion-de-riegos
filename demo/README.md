# Sistema de gestión de riegos

Aplicación web para registrar especies, cultivos y mediciones de temperatura y
humedad. El backend está construido con Spring Boot y JDBC; la interfaz es
HTML, CSS y JavaScript modular servido por la misma aplicación.

## Requisitos

- JDK 26, porque `pom.xml` define `java.version` como `26`.
- PostgreSQL 14 o una versión posterior compatible con el driver del proyecto.
- Git, si se va a clonar el proyecto.

El repositorio incluye Maven Wrapper (`mvnw` y `mvnw.cmd`), por lo que no es
necesario instalar Maven manualmente.

## Estructura principal

- `src/main/java/com/example/demo/`: backend Java.
  - `controller/`: página principal, salud de la aplicación y manejo de errores.
  - `service/`: reglas de negocio para mediciones.
  - `repository/`: consultas JDBC y persistencia.
  - `model/`: objetos que representan especies, cultivos, mediciones y alertas.
- `src/main/resources/static/`: interfaz web estática.
- `src/main/resources/schema.sql`: creación y actualización inicial de las tablas.
- `src/main/resources/application.properties`: puerto y conexión configurable a PostgreSQL.
- `src/test/`: pruebas de contexto, endpoints y clasificación de mediciones.

## Crear la base de datos

En PostgreSQL crea una base de datos y un usuario. El nombre usado por defecto
por el proyecto contiene espacios, por lo que debe escribirse entre comillas.

Ejemplo desde `psql`:

```sql
CREATE USER riegos_app WITH PASSWORD 'cambia_esta_clave';
CREATE DATABASE "sistema de riego" OWNER riegos_app;
```

Después, desde PowerShell, configura la conexión para la sesión actual:

```powershell
$env:DB_URL = "jdbc:postgresql://127.0.0.1:5432/sistema de riego"
$env:DB_USERNAME = "riegos_app"
$env:DB_PASSWORD = "cambia_esta_clave"
```

Al arrancar, Spring Boot ejecuta automáticamente
`src/main/resources/schema.sql`. El script usa `CREATE TABLE IF NOT EXISTS`,
por lo que está pensado para preparar la base o completar una base existente.

Si se desea ejecutar el esquema manualmente:

```powershell
psql -h 127.0.0.1 -U riegos_app -d "sistema de riego" -f .\src\main\resources\schema.sql
```

La ejecución automática continúa activa aunque el esquema se haya aplicado
manualmente.

## Iniciar el programa

Desde la carpeta `demo` ejecuta:

```powershell
.\mvnw.cmd spring-boot:run
```

Abre <http://localhost:9090/> en el navegador. La comprobación de conexión se
puede consultar en <http://localhost:9090/api/health>.

Para construir un archivo ejecutable:

```powershell
.\mvnw.cmd clean package
java -jar .\target\demo-0.0.1-SNAPSHOT.jar
```

Si cambiaste el puerto, puedes definirlo antes de iniciar:

```powershell
$env:SERVER_PORT = "9091"
.\mvnw.cmd spring-boot:run
```

## Ejecutar pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas utilizan una base H2 en memoria y no modifican la base PostgreSQL
de desarrollo.

## Replicar el proyecto en otra máquina

1. Instala JDK 26 y PostgreSQL.
2. Clona el repositorio y entra en la carpeta `demo`.
3. Crea la base de datos y el usuario como se indica arriba.
4. Define `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` en la terminal de esa máquina.
5. Ejecuta `./mvnw spring-boot:run` en Linux o `./mvnw.cmd spring-boot:run` en PowerShell.
6. Abre `http://localhost:9090/`.

No es necesario copiar una base de datos existente para obtener las tablas:
`schema.sql` las crea al iniciar. Si también se necesitan los datos de una
instalación anterior, realiza un respaldo y restáuralo con PostgreSQL:

```powershell
pg_dump -h 127.0.0.1 -U riegos_app -d "sistema de riego" -Fc -f .\respaldo-riegos.dump
pg_restore -h 127.0.0.1 -U riegos_app -d "sistema de riego" .\respaldo-riegos.dump
```

El respaldo contiene datos y estructura. Para una instalación limpia basta con
el archivo `schema.sql`.

## Endpoints disponibles

| Método | Ruta | Uso |
| --- | --- | --- |
| GET | `/api/health` | Comprueba la conexión con PostgreSQL. |
| GET | `/especies` | Lista las especies registradas. |
| POST | `/especies` | Registra una especie y sus rangos. |
| GET | `/cultivos` | Lista los cultivos registrados. |
| POST | `/cultivos` | Registra un cultivo asociado a una especie. |
| GET | `/mediciones` | Lista las mediciones y sus alertas. |
| POST | `/mediciones` | Registra temperatura y humedad para un cultivo. |

## Variables de configuración

| Variable | Valor predeterminado | Descripción |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://127.0.0.1:5432/sistema de riego` | URL JDBC de PostgreSQL. |
| `DB_USERNAME` | `postgres` | Usuario de la base de datos. |
| `DB_PASSWORD` | Vacío | Contraseña del usuario. |
| `SERVER_PORT` | `9090` | Puerto HTTP de Spring Boot. |

No guardes contraseñas reales en `application.properties`, el README ni en el
repositorio.
