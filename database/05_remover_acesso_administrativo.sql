-- Aplicar uma vez em bases criadas por versões que possuíam perfil administrativo.
-- A desativação é reversível e preserva a integridade referencial dos dados existentes.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET DEFINE OFF

UPDATE conta_acesso
   SET ativo = 0
 WHERE perfil <> 'USUARIO'
   AND ativo = 1;

COMMIT;
