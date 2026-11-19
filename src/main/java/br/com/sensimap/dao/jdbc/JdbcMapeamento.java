package br.com.sensimap.dao.jdbc;

import br.com.sensimap.model.*;
import java.sql.*;

final class JdbcMapeamento {
    private JdbcMapeamento() { }

    static Usuario usuario(ResultSet r, String p) throws SQLException {
        return new Usuario(r.getLong(p + "id"), r.getString(p + "apelido"), r.getTimestamp(p + "criado_em").toLocalDateTime());
    }

    static Local local(ResultSet r, String p) throws SQLException {
        return new Local(r.getLong(p + "id"), r.getString(p + "nome"), r.getString(p + "endereco"),
                r.getString(p + "bairro"), r.getString(p + "descricao"), r.getBigDecimal(p + "latitude"),
                r.getBigDecimal(p + "longitude"), r.getInt(p + "dados_demonstrativos") == 1,
                r.getTimestamp(p + "criado_em").toLocalDateTime());
    }

    static Estimulo estimulo(ResultSet r) throws SQLException {
        return new Estimulo(r.getLong("id"), r.getString("nome"), r.getString("descricao"));
    }

    static Avaliacao avaliacao(ResultSet r) throws SQLException {
        return new Avaliacao(r.getLong("id"), usuario(r, "u_"), local(r, "l_"),
                r.getDate("data_visita").toLocalDate(), PeriodoDia.valueOf(r.getString("periodo")),
                r.getInt("ruido"), r.getInt("movimento"), r.getInt("imprevisibilidade"),
                r.getString("comentario"), r.getTimestamp("criado_em").toLocalDateTime());
    }
}
