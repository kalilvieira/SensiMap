package br.com.sensimap.dao.jdbc;

import br.com.sensimap.dao.PersistenciaException;
import java.sql.*;

final class JdbcUtil {
    private JdbcUtil() { }

    static PersistenciaException traduzir(SQLException erro) {
        return switch (erro.getErrorCode()) {
            case 1 -> new PersistenciaException("CONFLITO", "Registro duplicado: confira apelido, nome do estímulo, visita ou associação.");
            case 2292 -> new PersistenciaException("CONFLITO", "Remova as avaliações vinculadas antes de excluir este usuário ou local.");
            case 2291 -> new PersistenciaException("VALIDACAO", "Usuário, local ou estímulo informado não existe.");
            case 1400, 2290, 12899, 1438, 20001 -> new PersistenciaException("VALIDACAO", "Campos inválidos: confira limites, notas, período e data da visita.");
            default -> new PersistenciaException("PERSISTENCIA", "Não foi possível concluir a operação no banco. Confira a conexão e a instalação das tabelas.");
        };
    }

    static void exigirUmaLinha(int linhas) {
        if (linhas == 0) throw new PersistenciaException("NAO_ENCONTRADO", "Registro não encontrado.");
    }

    static long chaveGerada(PreparedStatement comando) throws SQLException {
        try (ResultSet chaves = comando.getGeneratedKeys()) {
            if (!chaves.next()) throw new SQLException("ID não retornado.");
            return chaves.getLong(1);
        }
    }

    static long idPersistido(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Informe um ID persistido positivo.");
        return id;
    }

    static void exigirNovo(Long id) {
        if (id != null) throw new IllegalArgumentException("Novo registro deve ter ID nulo.");
    }

    static String buscaLiteral(String texto) {
        return "%" + (texto == null ? "" : texto.strip()).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }
}
