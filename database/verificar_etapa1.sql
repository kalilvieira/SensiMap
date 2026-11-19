-- Executar em SQLcl/SQL*Plus após a carga inicial, antes de alterações manuais.
-- Não é relatório funcional da Etapa 2. Os testes de escrita são revertidos.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET SERVEROUTPUT ON
SET DEFINE OFF

DECLARE
    quantidade NUMBER;

    PROCEDURE conferir(consulta VARCHAR2, esperado NUMBER, mensagem VARCHAR2) IS
        atual NUMBER;
    BEGIN
        EXECUTE IMMEDIATE consulta INTO atual;
        IF atual <> esperado THEN
            RAISE_APPLICATION_ERROR(-20002, mensagem || ': esperado ' || esperado || ', obtido ' || atual);
        END IF;
    END;

    PROCEDURE rejeitar(comando VARCHAR2, codigo_esperado NUMBER) IS
    BEGIN
        EXECUTE IMMEDIATE comando;
        RAISE_APPLICATION_ERROR(-20003, 'Uma entrada inválida foi aceita.');
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE <> codigo_esperado THEN
                RAISE;
            END IF;
    END;
BEGIN
    SAVEPOINT inicio_testes;
    conferir('SELECT COUNT(*) FROM user_tables WHERE table_name IN (''USUARIO'', ''CONTA_ACESSO'', ''LOCAL'', ''AVALIACAO'', ''ESTIMULO'', ''LOCAL_ESTIMULO'')', 6, 'Tabelas');
    conferir('SELECT COUNT(*) FROM user_errors WHERE name = ''TRG_AVALIACAO_DATA_VISITA''', 0, 'Erros do gatilho');
    conferir('SELECT COUNT(*) FROM user_triggers WHERE trigger_name = ''TRG_AVALIACAO_DATA_VISITA'' AND status = ''ENABLED''', 1, 'Gatilho ativo');
    conferir('SELECT COUNT(*) FROM usuario', 3, 'Usuários demonstrativos');
    conferir('SELECT COUNT(*) FROM conta_acesso', 0, 'Contas antes do cadastro pela aplicação');
    conferir('SELECT COUNT(*) FROM local', 10, 'Locais');
    conferir('SELECT COUNT(*) FROM estimulo', 5, 'Estímulos');
    conferir('SELECT COUNT(*) FROM avaliacao', 10, 'Avaliações');
    conferir('SELECT COUNT(*) FROM local_estimulo', 10, 'Associações');
    conferir('SELECT COUNT(*) FROM local WHERE dados_demonstrativos = 1 AND latitude IS NULL AND longitude IS NULL', 10, 'Locais simulados sem coordenadas');
    conferir('SELECT COUNT(*) FROM local l WHERE NOT EXISTS (SELECT 1 FROM avaliacao a WHERE a.local_id = l.id)', 6, 'Locais sem avaliações');
    conferir('SELECT COUNT(*) FROM (SELECT local_id FROM avaliacao GROUP BY local_id HAVING COUNT(*) BETWEEN 1 AND 2)', 2, 'Locais com 1 ou 2 avaliações');
    conferir('SELECT COUNT(*) FROM (SELECT local_id FROM avaliacao GROUP BY local_id HAVING COUNT(*) >= 3)', 2, 'Locais com 3+ avaliações');
    SELECT COUNT(*) INTO quantidade FROM (SELECT local_id FROM local_estimulo GROUP BY local_id HAVING COUNT(*) > 1);
    IF quantidade = 0 THEN RAISE_APPLICATION_ERROR(-20004, 'Falta local com vários estímulos.'); END IF;
    SELECT COUNT(*) INTO quantidade FROM (SELECT estimulo_id FROM local_estimulo GROUP BY estimulo_id HAVING COUNT(*) > 1);
    IF quantidade = 0 THEN RAISE_APPLICATION_ERROR(-20005, 'Falta estímulo em vários locais.'); END IF;

    rejeitar('INSERT INTO usuario (apelido) VALUES (''demo_aurora'')', -1);
    rejeitar('INSERT INTO usuario (apelido) VALUES (''DEMO_AURORA'')', -1);
    rejeitar('UPDATE avaliacao SET ruido = 0', -2290);
    rejeitar('UPDATE avaliacao SET ruido = 6', -2290);
    rejeitar('UPDATE avaliacao SET movimento = 0', -2290);
    rejeitar('UPDATE avaliacao SET movimento = 6', -2290);
    rejeitar('UPDATE avaliacao SET imprevisibilidade = 0', -2290);
    rejeitar('UPDATE avaliacao SET imprevisibilidade = 6', -2290);
    rejeitar('UPDATE avaliacao SET periodo = ''INVALIDO''', -2290);
    rejeitar('UPDATE avaliacao SET data_visita = TRUNC(SYSDATE) + 1', -20001);
    rejeitar('UPDATE avaliacao SET local_id = -1', -2291);
    rejeitar('UPDATE avaliacao SET usuario_id = -1', -2291);
    rejeitar('UPDATE local SET latitude = 1', -2290);
    rejeitar('UPDATE local SET latitude = 91, longitude = 0', -2290);
    rejeitar('UPDATE local SET latitude = 0, longitude = 181', -2290);
    rejeitar('UPDATE local SET dados_demonstrativos = 2', -2290);
    rejeitar('INSERT INTO local_estimulo SELECT local_id, estimulo_id FROM local_estimulo', -1);
    rejeitar('INSERT INTO avaliacao (usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade) SELECT usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade FROM avaliacao', -1);
    rejeitar('DELETE FROM usuario WHERE apelido = ''demo_aurora''', -2292);
    rejeitar('DELETE FROM local WHERE nome = ''Biblioteca Mário de Andrade''', -2292);
    ROLLBACK TO inicio_testes;
    DBMS_OUTPUT.PUT_LINE('Etapa 1: carga, N:N e restrições verificadas. Alterações de teste revertidas.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK TO inicio_testes;
        RAISE;
END;
/
