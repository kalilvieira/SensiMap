-- Oracle: locais/endereço reais; avaliações e associações simuladas para demonstração.
-- Aplicar UMA vez após 01_schema.sql. Fontes oficiais registradas no README.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET DEFINE OFF

INSERT INTO usuario (apelido) VALUES ('demo_aurora');
INSERT INTO usuario (apelido) VALUES ('demo_brisa');
INSERT INTO usuario (apelido) VALUES ('demo_horizonte');

INSERT INTO local (nome, endereco, bairro, descricao, dados_demonstrativos)
VALUES ('Biblioteca Mário de Andrade', 'Rua da Consolação, 94', 'Consolação', 'Local real em São Paulo. As percepções desta carga são simuladas para demonstração.', 1);
INSERT INTO local (nome, endereco, bairro, descricao, dados_demonstrativos)
VALUES ('Praça das Artes', 'Avenida São João, 281', 'República', 'Local real em São Paulo. As percepções desta carga são simuladas para demonstração.', 1);
INSERT INTO local (nome, endereco, bairro, descricao, dados_demonstrativos)
VALUES ('Centro Cultural São Paulo', 'Rua Vergueiro, 1000', 'Liberdade', 'Local real em São Paulo. As percepções desta carga são simuladas para demonstração.', 1);
INSERT INTO local (nome, endereco, bairro, descricao, dados_demonstrativos)
VALUES ('Mercado Municipal Paulistano', 'Rua da Cantareira, 306', 'Centro Histórico', 'Local real em São Paulo. As percepções desta carga são simuladas para demonstração.', 1);
INSERT INTO local (nome, endereco, bairro, descricao, dados_demonstrativos)
VALUES ('Terminal Pinheiros', 'Rua Gilberto Sabino, 130', 'Pinheiros', 'Local real em São Paulo. As percepções desta carga são simuladas para demonstração.', 1);
INSERT INTO local (nome, endereco, bairro, descricao, dados_demonstrativos)
VALUES ('Parque Ibirapuera', 'Avenida Pedro Álvares Cabral, s/n', 'Vila Mariana', 'Local real em São Paulo. As percepções desta carga são simuladas para demonstração.', 1);
INSERT INTO local (nome, endereco, bairro, descricao, dados_demonstrativos)
VALUES ('Museu da Língua Portuguesa', 'Estação da Luz - Portão A', 'Luz', 'Local real em São Paulo. As percepções desta carga são simuladas para demonstração.', 1);
INSERT INTO local (nome, endereco, bairro, descricao, dados_demonstrativos)
VALUES ('Pinacoteca de São Paulo - Pina Luz', 'Praça da Luz, 2', 'Luz', 'Local real em São Paulo. As percepções desta carga são simuladas para demonstração.', 1);
INSERT INTO local (nome, endereco, bairro, descricao, dados_demonstrativos)
VALUES ('MASP', 'Avenida Paulista, 1578', 'Bela Vista', 'Local real em São Paulo. As percepções desta carga são simuladas para demonstração.', 1);
INSERT INTO local (nome, endereco, bairro, descricao, dados_demonstrativos)
VALUES ('Jardim Botânico de São Paulo', 'Avenida Miguel Estéfano, 3031', 'Água Funda', 'Local real em São Paulo. As percepções desta carga são simuladas para demonstração.', 1);

INSERT INTO estimulo (nome, descricao) VALUES ('Buzinas', 'Tipo de estímulo percebido; associação fictícia na carga demonstrativa.');
INSERT INTO estimulo (nome, descricao) VALUES ('Obras', 'Tipo de estímulo percebido; associação fictícia na carga demonstrativa.');
INSERT INTO estimulo (nome, descricao) VALUES ('Multidão', 'Percepção subjetiva, sem contagem real de pessoas.');
INSERT INTO estimulo (nome, descricao) VALUES ('Música alta', 'Percepção subjetiva, sem medição em decibéis.');
INSERT INTO estimulo (nome, descricao) VALUES ('Anúncios sonoros', 'Tipo de estímulo percebido; associação fictícia na carga demonstrativa.');

-- N:N: cada subconsulta escalar precisa encontrar exatamente um registro desta carga.
INSERT INTO local_estimulo (local_id, estimulo_id)
VALUES ((SELECT id FROM local WHERE nome = 'Biblioteca Mário de Andrade'),
        (SELECT id FROM estimulo WHERE nome = 'Obras'));
INSERT INTO local_estimulo (local_id, estimulo_id)
VALUES ((SELECT id FROM local WHERE nome = 'Biblioteca Mário de Andrade'),
        (SELECT id FROM estimulo WHERE nome = 'Anúncios sonoros'));
INSERT INTO local_estimulo (local_id, estimulo_id)
VALUES ((SELECT id FROM local WHERE nome = 'Praça das Artes'),
        (SELECT id FROM estimulo WHERE nome = 'Obras'));
INSERT INTO local_estimulo (local_id, estimulo_id)
VALUES ((SELECT id FROM local WHERE nome = 'Praça das Artes'),
        (SELECT id FROM estimulo WHERE nome = 'Buzinas'));
INSERT INTO local_estimulo (local_id, estimulo_id)
VALUES ((SELECT id FROM local WHERE nome = 'Centro Cultural São Paulo'),
        (SELECT id FROM estimulo WHERE nome = 'Música alta'));
INSERT INTO local_estimulo (local_id, estimulo_id)
VALUES ((SELECT id FROM local WHERE nome = 'Mercado Municipal Paulistano'),
        (SELECT id FROM estimulo WHERE nome = 'Multidão'));
INSERT INTO local_estimulo (local_id, estimulo_id)
VALUES ((SELECT id FROM local WHERE nome = 'Mercado Municipal Paulistano'),
        (SELECT id FROM estimulo WHERE nome = 'Anúncios sonoros'));
INSERT INTO local_estimulo (local_id, estimulo_id)
VALUES ((SELECT id FROM local WHERE nome = 'Terminal Pinheiros'),
        (SELECT id FROM estimulo WHERE nome = 'Buzinas'));
INSERT INTO local_estimulo (local_id, estimulo_id)
VALUES ((SELECT id FROM local WHERE nome = 'Terminal Pinheiros'),
        (SELECT id FROM estimulo WHERE nome = 'Multidão'));
INSERT INTO local_estimulo (local_id, estimulo_id)
VALUES ((SELECT id FROM local WHERE nome = 'MASP'),
        (SELECT id FROM estimulo WHERE nome = 'Multidão'));

-- Biblioteca: 4 (3 manhã, 1 tarde); Praça: 2; Centro cultural: 1; Mercado: 3 noite; demais: 0.
INSERT INTO avaliacao (usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade, comentario)
VALUES ((SELECT id FROM usuario WHERE apelido = 'demo_aurora'),
        (SELECT id FROM local WHERE nome = 'Biblioteca Mário de Andrade'),
        DATE '2026-11-01', 'MANHA', 1, 2, 3, 'Dados simulados para demonstração. Relato fictício e subjetivo.');
INSERT INTO avaliacao (usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade, comentario)
VALUES ((SELECT id FROM usuario WHERE apelido = 'demo_brisa'),
        (SELECT id FROM local WHERE nome = 'Biblioteca Mário de Andrade'),
        DATE '2026-11-01', 'MANHA', 2, 3, 4, 'Dados simulados para demonstração. Relato fictício e subjetivo.');
INSERT INTO avaliacao (usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade, comentario)
VALUES ((SELECT id FROM usuario WHERE apelido = 'demo_horizonte'),
        (SELECT id FROM local WHERE nome = 'Biblioteca Mário de Andrade'),
        DATE '2026-11-01', 'MANHA', 3, 4, 5, 'Dados simulados para demonstração. Relato fictício e subjetivo.');
INSERT INTO avaliacao (usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade, comentario)
VALUES ((SELECT id FROM usuario WHERE apelido = 'demo_aurora'),
        (SELECT id FROM local WHERE nome = 'Biblioteca Mário de Andrade'),
        DATE '2026-11-01', 'TARDE', 4, 5, 2, 'Dados simulados para demonstração. Relato fictício e subjetivo.');
INSERT INTO avaliacao (usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade, comentario)
VALUES ((SELECT id FROM usuario WHERE apelido = 'demo_aurora'),
        (SELECT id FROM local WHERE nome = 'Praça das Artes'),
        DATE '2026-11-01', 'MANHA', 3, 2, 1, 'Dados simulados para demonstração. Relato fictício e subjetivo.');
INSERT INTO avaliacao (usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade, comentario)
VALUES ((SELECT id FROM usuario WHERE apelido = 'demo_brisa'),
        (SELECT id FROM local WHERE nome = 'Praça das Artes'),
        DATE '2026-11-01', 'TARDE', 4, 3, 2, 'Dados simulados para demonstração. Relato fictício e subjetivo.');
INSERT INTO avaliacao (usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade, comentario)
VALUES ((SELECT id FROM usuario WHERE apelido = 'demo_horizonte'),
        (SELECT id FROM local WHERE nome = 'Centro Cultural São Paulo'),
        DATE '2026-11-01', 'NOITE', 5, 4, 3, 'Dados simulados para demonstração. Relato fictício e subjetivo.');
INSERT INTO avaliacao (usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade, comentario)
VALUES ((SELECT id FROM usuario WHERE apelido = 'demo_aurora'),
        (SELECT id FROM local WHERE nome = 'Mercado Municipal Paulistano'),
        DATE '2026-11-01', 'NOITE', 4, 1, 2, 'Dados simulados para demonstração. Relato fictício e subjetivo.');
INSERT INTO avaliacao (usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade, comentario)
VALUES ((SELECT id FROM usuario WHERE apelido = 'demo_brisa'),
        (SELECT id FROM local WHERE nome = 'Mercado Municipal Paulistano'),
        DATE '2026-11-01', 'NOITE', 4, 2, 3, 'Dados simulados para demonstração. Relato fictício e subjetivo.');
INSERT INTO avaliacao (usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade, comentario)
VALUES ((SELECT id FROM usuario WHERE apelido = 'demo_horizonte'),
        (SELECT id FROM local WHERE nome = 'Mercado Municipal Paulistano'),
        DATE '2026-11-01', 'NOITE', 5, 3, 4, 'Dados simulados para demonstração. Relato fictício e subjetivo.');

COMMIT;
