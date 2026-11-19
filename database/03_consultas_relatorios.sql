-- Oracle 19c+. Consultas somente de leitura; relatos fictícios, sem medições físicas.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET DEFINE OFF

-- Relação N:N; inclui locais sem estímulos associados.
SELECT l.id, l.nome, l.bairro, e.nome AS estimulo
FROM local l
LEFT JOIN local_estimulo le ON le.local_id = l.id
LEFT JOIN estimulo e ON e.id = le.estimulo_id
ORDER BY l.id, e.nome;

-- Todos os períodos, incluindo locais ainda não avaliados.
-- Classificar pela média bruta, arredondar apenas as colunas de apresentação.
WITH indicadores AS (
    SELECT l.id, l.nome, COUNT(a.id) AS n, AVG(a.ruido) AS ruido,
           AVG(a.movimento) AS movimento, AVG(a.imprevisibilidade) AS imprevisibilidade,
           MAX(a.data_visita) AS ultima_visita
    FROM local l LEFT JOIN avaliacao a ON a.local_id = l.id
    GROUP BY l.id, l.nome
)
SELECT id, nome, n, ROUND(ruido, 2) AS media_ruido,
       ROUND(movimento, 2) AS media_movimento,
       ROUND(imprevisibilidade, 2) AS media_imprevisibilidade, ultima_visita,
       CASE WHEN n < 3 THEN 'Dados insuficientes' WHEN ruido <= 2 THEN 'Baixa'
            WHEN ruido <= 3.5 THEN 'Moderada' ELSE 'Alta' END AS classe_ruido,
       CASE WHEN n < 3 THEN 'Dados insuficientes' WHEN movimento <= 2 THEN 'Baixa'
            WHEN movimento <= 3.5 THEN 'Moderada' ELSE 'Alta' END AS classe_movimento,
       CASE WHEN n < 3 THEN 'Dados insuficientes' WHEN imprevisibilidade <= 2 THEN 'Baixa'
            WHEN imprevisibilidade <= 3.5 THEN 'Moderada' ELSE 'Alta' END AS classe_imprevisibilidade
FROM indicadores ORDER BY id;

-- Comparação com local-ver ID PERIODO. Não arredonda as médias para conferência.
SELECT local_id, periodo, COUNT(*) AS n, AVG(ruido) AS media_ruido,
       AVG(movimento) AS media_movimento, AVG(imprevisibilidade) AS media_imprevisibilidade,
       MAX(data_visita) AS ultima_visita
FROM avaliacao
GROUP BY local_id, periodo ORDER BY local_id, periodo;

-- Ausência de avaliações não significa ausência de estímulos.
SELECT l.id, l.nome, l.bairro
FROM local l LEFT JOIN avaliacao a ON a.local_id = l.id
WHERE a.id IS NULL ORDER BY l.id;
