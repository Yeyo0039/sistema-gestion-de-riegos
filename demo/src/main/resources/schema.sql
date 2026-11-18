-- Esquema principal del sistema. Spring Boot ejecuta este archivo al iniciar.
CREATE TABLE IF NOT EXISTS tipo (
    id INTEGER PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tipo_sustrato (
    id INTEGER PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS especie (
    id INTEGER PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo_id INTEGER NOT NULL,
    descripcion VARCHAR(500),
    CONSTRAINT especie_tipo_id_fkey FOREIGN KEY (tipo_id) REFERENCES tipo(id)
);

CREATE TABLE IF NOT EXISTS temperatura (
    id INTEGER PRIMARY KEY,
    especie_id INTEGER,
    optima_minima NUMERIC NOT NULL,
    optima_maxima NUMERIC NOT NULL,
    maximo_permitido NUMERIC NOT NULL,
    minimo_permitido NUMERIC NOT NULL,
    CONSTRAINT temperatura_especie_id_fkey FOREIGN KEY (especie_id) REFERENCES especie(id)
);

ALTER TABLE temperatura
    ADD COLUMN IF NOT EXISTS especie_id INTEGER;

CREATE TABLE IF NOT EXISTS humedad (
    id INTEGER PRIMARY KEY,
    especie_id INTEGER NOT NULL,
    optima_minima NUMERIC NOT NULL,
    optima_maxima NUMERIC NOT NULL,
    maximo_permitido NUMERIC NOT NULL,
    minimo_permitido NUMERIC NOT NULL,
    CONSTRAINT humedad_especie_id_fkey FOREIGN KEY (especie_id) REFERENCES especie(id)
);

CREATE TABLE IF NOT EXISTS etapa (
    id INTEGER PRIMARY KEY,
    especie_id INTEGER NOT NULL,
    tipo_sustrato_id INTEGER,
    fase VARCHAR(30) NOT NULL,
    intervalo INTEGER,
    volumen INTEGER,
    CONSTRAINT etapa_especie_id_fkey FOREIGN KEY (especie_id) REFERENCES especie(id),
    CONSTRAINT etapa_tipo_sustrato_id_fkey FOREIGN KEY (tipo_sustrato_id) REFERENCES tipo_sustrato(id),
    CONSTRAINT etapa_especie_id_fase_key UNIQUE (especie_id, fase)
);

CREATE TABLE IF NOT EXISTS cultivo (
    id INTEGER PRIMARY KEY,
    fecha_inicio DATE NOT NULL,
    especie INTEGER NOT NULL
);

ALTER TABLE cultivo
    ADD COLUMN IF NOT EXISTS nombre VARCHAR(100);

UPDATE cultivo
SET nombre = CONCAT('Cultivo ', id)
WHERE nombre IS NULL;

ALTER TABLE cultivo
    ALTER COLUMN nombre SET NOT NULL;

CREATE TABLE IF NOT EXISTS medicion (
    id INTEGER PRIMARY KEY,
    cultivo_id INTEGER NOT NULL,
    temperatura NUMERIC NOT NULL,
    humedad NUMERIC NOT NULL,
    fecha_hora TIMESTAMP NOT NULL,
    estado_temperatura VARCHAR(20) NOT NULL,
    mensaje_temperatura VARCHAR(255) NOT NULL,
    estado_humedad VARCHAR(20) NOT NULL,
    mensaje_humedad VARCHAR(255) NOT NULL,
    estado_general VARCHAR(20) NOT NULL,
    mensaje VARCHAR(255) NOT NULL,
    CONSTRAINT medicion_cultivo_id_fkey FOREIGN KEY (cultivo_id) REFERENCES cultivo(id)
);

CREATE TABLE IF NOT EXISTS alerta (
    id INTEGER PRIMARY KEY,
    medicion_id INTEGER NOT NULL,
    variable VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    mensaje VARCHAR(255) NOT NULL,
    CONSTRAINT alerta_medicion_id_fkey FOREIGN KEY (medicion_id) REFERENCES medicion(id) ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS tipo_id_seq;
CREATE SEQUENCE IF NOT EXISTS tipo_sustrato_id_seq;
CREATE SEQUENCE IF NOT EXISTS especie_id_seq;
CREATE SEQUENCE IF NOT EXISTS temperatura_id_seq;
CREATE SEQUENCE IF NOT EXISTS humedad_id_seq;
CREATE SEQUENCE IF NOT EXISTS etapa_id_seq;
CREATE SEQUENCE IF NOT EXISTS cultivo_id_seq;
CREATE SEQUENCE IF NOT EXISTS medicion_id_seq;
CREATE SEQUENCE IF NOT EXISTS alerta_id_seq;

-- Defaults para que los INSERT que no incluyen un ID reciban uno nuevo.
ALTER TABLE tipo ALTER COLUMN id SET DEFAULT nextval('tipo_id_seq');
ALTER TABLE tipo_sustrato ALTER COLUMN id SET DEFAULT nextval('tipo_sustrato_id_seq');
ALTER TABLE especie ALTER COLUMN id SET DEFAULT nextval('especie_id_seq');
ALTER TABLE temperatura ALTER COLUMN id SET DEFAULT nextval('temperatura_id_seq');
ALTER TABLE humedad ALTER COLUMN id SET DEFAULT nextval('humedad_id_seq');
ALTER TABLE etapa ALTER COLUMN id SET DEFAULT nextval('etapa_id_seq');
ALTER TABLE cultivo ALTER COLUMN id SET DEFAULT nextval('cultivo_id_seq');
ALTER TABLE medicion ALTER COLUMN id SET DEFAULT nextval('medicion_id_seq');
ALTER TABLE alerta ALTER COLUMN id SET DEFAULT nextval('alerta_id_seq');
