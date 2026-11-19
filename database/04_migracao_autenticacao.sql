-- Aplicar somente quando as tabelas SensiMap foram criadas por uma versão anterior.
-- É seguro executar novamente: cria a tabela/índice e desativa perfis administrativos legados.
SET DEFINE OFF

DECLARE
    quantidade NUMBER;
BEGIN
    SELECT COUNT(*) INTO quantidade FROM user_tables WHERE table_name = 'CONTA_ACESSO';
    IF quantidade = 0 THEN
        EXECUTE IMMEDIATE q'[
            CREATE TABLE conta_acesso (
                usuario_id NUMBER(19) PRIMARY KEY REFERENCES usuario(id) ON DELETE CASCADE,
                senha_hash VARCHAR2(100 CHAR) NOT NULL,
                senha_salt VARCHAR2(100 CHAR) NOT NULL,
                senha_iteracoes NUMBER(9) NOT NULL CHECK (senha_iteracoes >= 100000),
                perfil VARCHAR2(15 CHAR) DEFAULT 'USUARIO' NOT NULL CHECK (perfil = 'USUARIO'),
                ativo NUMBER(1) DEFAULT 1 NOT NULL CHECK (ativo IN (0, 1)),
                criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
            )
        ]';
    END IF;

    SELECT COUNT(*) INTO quantidade FROM user_indexes WHERE index_name = 'UQ_USUARIO_APELIDO_CI';
    IF quantidade = 0 THEN
        EXECUTE IMMEDIATE 'CREATE UNIQUE INDEX uq_usuario_apelido_ci ON usuario(LOWER(apelido))';
    END IF;
END;
/

UPDATE conta_acesso SET ativo = 0 WHERE perfil <> 'USUARIO';

COMMIT;
