package br.com.sensimap.dao.jdbc;

import br.com.sensimap.dao.*;
import br.com.sensimap.model.*;
import java.sql.*;
import java.util.*;

/** A conexão pertence ao chamador; este DAO não faz commit nem fecha a conexão. */
public final class JdbcEstimuloDAO implements EstimuloDAO {
    private final Connection conexao;
    private static final String SELECT = """
            SELECT * FROM estimulo
            """;

    public JdbcEstimuloDAO(Connection conexao) { this.conexao = Objects.requireNonNull(conexao); }

    @Override
    public List<Estimulo> listar() { return consultar(SELECT + " ORDER BY id"); }

    @Override
    public Optional<Estimulo> buscarPorId(long id) {
        return consultar(SELECT + " WHERE id = ?", JdbcUtil.idPersistido(id)).stream().findFirst();
    }

    private List<Estimulo> consultar(String sql, Object... parametros) {
        List<Estimulo> itens = new ArrayList<>();
        try (PreparedStatement p = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) p.setObject(i + 1, parametros[i]);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) itens.add(JdbcMapeamento.estimulo(r));
            }
            return itens;
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public Estimulo inserir(Estimulo v) {
        Objects.requireNonNull(v, "Informe o registro.");
        JdbcUtil.exigirNovo(v.getId());
        // Oracle: pedir a coluna ID explicitamente evita receber ROWID.
        try (PreparedStatement p = conexao.prepareStatement(
                "INSERT INTO estimulo (nome, descricao) VALUES (?, ?)", new String[] {"ID"})) {
            preencher(p, v);
            p.executeUpdate();
            long id = JdbcUtil.chaveGerada(p);
            return buscarPorId(id).orElseThrow(() -> new PersistenciaException("PERSISTENCIA", "Não foi possível reler o registro inserido."));
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public void atualizar(Estimulo v) {
        Objects.requireNonNull(v, "Informe o registro.");
        long id = JdbcUtil.idPersistido(v.getId());
        try (PreparedStatement p = conexao.prepareStatement(
                "UPDATE estimulo SET nome = ?, descricao = ? WHERE id = ?")) {
            preencher(p, v);
            p.setLong(3, id);
            JdbcUtil.exigirUmaLinha(p.executeUpdate());
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public void excluir(long id) {
        try (PreparedStatement p = conexao.prepareStatement("DELETE FROM estimulo WHERE id = ?")) {
            p.setLong(1, JdbcUtil.idPersistido(id));
            JdbcUtil.exigirUmaLinha(p.executeUpdate());
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    private void preencher(PreparedStatement p, Estimulo v) throws SQLException {
        p.setString(1, v.getNome());
        p.setString(2, v.getDescricao());
    }
}
