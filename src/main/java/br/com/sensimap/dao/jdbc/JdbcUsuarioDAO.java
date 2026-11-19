package br.com.sensimap.dao.jdbc;

import br.com.sensimap.dao.*;
import br.com.sensimap.model.*;
import java.sql.*;
import java.util.*;

/** A conexão pertence ao chamador; este DAO não faz commit nem fecha a conexão. */
public final class JdbcUsuarioDAO implements UsuarioDAO {
    private final Connection conexao;
    private static final String SELECT = """
            SELECT * FROM usuario
            """;

    public JdbcUsuarioDAO(Connection conexao) { this.conexao = Objects.requireNonNull(conexao); }

    @Override
    public List<Usuario> listar() { return consultar(SELECT + " ORDER BY id"); }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        return consultar(SELECT + " WHERE id = ?", JdbcUtil.idPersistido(id)).stream().findFirst();
    }

    private List<Usuario> consultar(String sql, Object... parametros) {
        List<Usuario> itens = new ArrayList<>();
        try (PreparedStatement p = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) p.setObject(i + 1, parametros[i]);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) itens.add(JdbcMapeamento.usuario(r, ""));
            }
            return itens;
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public Usuario inserir(Usuario v) {
        Objects.requireNonNull(v, "Informe o registro.");
        JdbcUtil.exigirNovo(v.getId());
        // Oracle: pedir a coluna ID explicitamente evita receber ROWID.
        try (PreparedStatement p = conexao.prepareStatement(
                "INSERT INTO usuario (apelido) VALUES (?)", new String[] {"ID"})) {
            preencher(p, v);
            p.executeUpdate();
            long id = JdbcUtil.chaveGerada(p);
            return buscarPorId(id).orElseThrow(() -> new PersistenciaException("PERSISTENCIA", "Não foi possível reler o registro inserido."));
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public void atualizar(Usuario v) {
        Objects.requireNonNull(v, "Informe o registro.");
        long id = JdbcUtil.idPersistido(v.getId());
        try (PreparedStatement p = conexao.prepareStatement(
                "UPDATE usuario SET apelido = ? WHERE id = ?")) {
            preencher(p, v);
            p.setLong(2, id);
            JdbcUtil.exigirUmaLinha(p.executeUpdate());
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public void excluir(long id) {
        try (PreparedStatement p = conexao.prepareStatement("DELETE FROM usuario WHERE id = ?")) {
            p.setLong(1, JdbcUtil.idPersistido(id));
            JdbcUtil.exigirUmaLinha(p.executeUpdate());
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    private void preencher(PreparedStatement p, Usuario v) throws SQLException {
        p.setString(1, v.getApelido());
    }
}
