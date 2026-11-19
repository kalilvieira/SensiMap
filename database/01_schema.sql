-- Oracle 19c ou superior (IDENTITY requer Oracle 12c+).
-- Executar no schema acadêmico fornecido pela FIAP, sem tabelas SensiMap existentes.
-- DDL Oracle faz COMMIT implícito: falhas podem deixar objetos parcialmente criados.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET DEFINE OFF

CREATE TABLE usuario (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    apelido VARCHAR2(60 CHAR) NOT NULL UNIQUE CHECK (TRIM(apelido) IS NOT NULL),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE conta_acesso (
    usuario_id NUMBER(19) PRIMARY KEY REFERENCES usuario(id) ON DELETE CASCADE,
    senha_hash VARCHAR2(100 CHAR) NOT NULL,
    senha_salt VARCHAR2(100 CHAR) NOT NULL,
    senha_iteracoes NUMBER(9) NOT NULL CHECK (senha_iteracoes >= 100000),
    perfil VARCHAR2(15 CHAR) DEFAULT 'USUARIO' NOT NULL CHECK (perfil = 'USUARIO'),
    ativo NUMBER(1) DEFAULT 1 NOT NULL CHECK (ativo IN (0, 1)),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE local (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR2(120 CHAR) NOT NULL CHECK (TRIM(nome) IS NOT NULL),
    endereco VARCHAR2(180 CHAR),
    bairro VARCHAR2(80 CHAR) NOT NULL CHECK (TRIM(bairro) IS NOT NULL),
    descricao VARCHAR2(500 CHAR),
    latitude NUMBER(9,6),
    longitude NUMBER(9,6),
    dados_demonstrativos NUMBER(1) DEFAULT 1 NOT NULL CHECK (dados_demonstrativos IN (0, 1)),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT ck_local_coordenadas_juntas CHECK ((latitude IS NULL AND longitude IS NULL) OR (latitude IS NOT NULL AND longitude IS NOT NULL)),
    CONSTRAINT ck_local_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_local_longitude CHECK (longitude BETWEEN -180 AND 180)
);

CREATE TABLE avaliacao (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id NUMBER(19) NOT NULL REFERENCES usuario(id),
    local_id NUMBER(19) NOT NULL REFERENCES local(id),
    data_visita DATE NOT NULL CHECK (data_visita = TRUNC(data_visita)),
    periodo VARCHAR2(10 CHAR) NOT NULL CHECK (periodo IN ('MANHA', 'TARDE', 'NOITE')),
    ruido NUMBER(1) NOT NULL CHECK (ruido BETWEEN 1 AND 5),
    movimento NUMBER(1) NOT NULL CHECK (movimento BETWEEN 1 AND 5),
    imprevisibilidade NUMBER(1) NOT NULL CHECK (imprevisibilidade BETWEEN 1 AND 5),
    comentario VARCHAR2(500 CHAR),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_avaliacao_visita UNIQUE (usuario_id, local_id, data_visita, periodo)
);

CREATE TABLE estimulo (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR2(80 CHAR) NOT NULL UNIQUE CHECK (TRIM(nome) IS NOT NULL),
    descricao VARCHAR2(300 CHAR)
);

CREATE TABLE local_estimulo (
    local_id NUMBER(19) NOT NULL REFERENCES local(id) ON DELETE CASCADE,
    estimulo_id NUMBER(19) NOT NULL REFERENCES estimulo(id) ON DELETE CASCADE,
    PRIMARY KEY (local_id, estimulo_id)
);

CREATE INDEX idx_local_bairro ON local(bairro);
CREATE UNIQUE INDEX uq_usuario_apelido_ci ON usuario(LOWER(apelido));
CREATE INDEX idx_avaliacao_local_periodo ON avaliacao(local_id, periodo);
-- O UNIQUE de avaliacao já indexa usuario_id; a PK associativa já indexa local_id.
CREATE INDEX idx_local_estimulo_estimulo ON local_estimulo(estimulo_id);

COMMENT ON TABLE avaliacao IS 'Relatos subjetivos; não são medições físicas ou avaliações clínicas.';
COMMENT ON COLUMN local.dados_demonstrativos IS 'Exibir: Dados simulados para demonstração.';
-- SYSDATE não é permitido em CHECK no Oracle. O gatilho protege também INSERTs SQL.
CREATE OR REPLACE TRIGGER trg_avaliacao_data_visita
BEFORE INSERT OR UPDATE OF data_visita ON avaliacao
FOR EACH ROW
BEGIN
    IF :NEW.data_visita > TRUNC(SYSDATE) THEN
        RAISE_APPLICATION_ERROR(-20001, 'Data da visita não pode ser futura.');
    END IF;
END;
/
SHOW ERRORS TRIGGER trg_avaliacao_data_visita
