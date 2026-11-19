-- Substitui apenas os dados cadastrais dos 10 locais da carga inicial.
-- Avaliações, associações, usuários e IDs são preservados.
-- Os locais/endereço são reais; percepções e associações permanecem simuladas.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET DEFINE OFF

DECLARE
    PROCEDURE atualizar_local(
        p_nome_antigo VARCHAR2,
        p_nome_novo VARCHAR2,
        p_endereco VARCHAR2,
        p_bairro VARCHAR2
    ) IS
    BEGIN
        UPDATE local
           SET nome = p_nome_novo,
               endereco = p_endereco,
               bairro = p_bairro,
               descricao = 'Local real em São Paulo. As percepções desta carga são simuladas para demonstração.',
               dados_demonstrativos = 1
         WHERE nome = p_nome_antigo;
        IF SQL%ROWCOUNT <> 1 THEN
            RAISE_APPLICATION_ERROR(-20020, 'Local inicial ausente ou duplicado: ' || p_nome_antigo);
        END IF;
    END;
BEGIN
    atualizar_local('Local demonstrativo 01 - Biblioteca', 'Biblioteca Mário de Andrade', 'Rua da Consolação, 94', 'Consolação');
    atualizar_local('Local demonstrativo 02 - Praça', 'Praça das Artes', 'Avenida São João, 281', 'República');
    atualizar_local('Local demonstrativo 03 - Centro cultural', 'Centro Cultural São Paulo', 'Rua Vergueiro, 1000', 'Liberdade');
    atualizar_local('Local demonstrativo 04 - Mercado', 'Mercado Municipal Paulistano', 'Rua da Cantareira, 306', 'Centro Histórico');
    atualizar_local('Local demonstrativo 05 - Terminal', 'Terminal Pinheiros', 'Rua Gilberto Sabino, 130', 'Pinheiros');
    atualizar_local('Local demonstrativo 06 - Parque', 'Parque Ibirapuera', 'Avenida Pedro Álvares Cabral, s/n', 'Vila Mariana');
    atualizar_local('Local demonstrativo 07 - Museu', 'Museu da Língua Portuguesa', 'Estação da Luz - Portão A', 'Luz');
    atualizar_local('Local demonstrativo 08 - Galeria', 'Pinacoteca de São Paulo - Pina Luz', 'Praça da Luz, 2', 'Luz');
    atualizar_local('Local demonstrativo 09 - Feira', 'MASP', 'Avenida Paulista, 1578', 'Bela Vista');
    atualizar_local('Local demonstrativo 10 - Jardim', 'Jardim Botânico de São Paulo', 'Avenida Miguel Estéfano, 3031', 'Água Funda');
END;
/

COMMIT;

SELECT id, nome, endereco, bairro, dados_demonstrativos
  FROM local
 ORDER BY id;
