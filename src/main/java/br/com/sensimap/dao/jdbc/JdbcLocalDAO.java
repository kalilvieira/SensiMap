package br.com.sensimap.dao.jdbc;

import br.com.sensimap.dao.*;
import br.com.sensimap.model.*;
import java.sql.*;
import java.util.*;

/** A conexão pertence ao chamador; este DAO não faz commit nem fecha a conexão. */
public final class JdbcLocalDAO implements LocalDAO {
    private final Connection conexao;
    private static final String SELECT = """
            SELECT * FROM local
            """;

    public JdbcLocalDAO(Connection conexao) { this.conexao = Objects.requireNonNull(conexao); }

    @Override
    public List<Local> listar() { return consultar(SELECT + " ORDER BY id"); }

    @Override
    public Optional<Local> buscarPorId(long id) {
        return consultar(SELECT + " WHERE id = ?", JdbcUtil.idPersistido(id)).stream().findFirst();
    }

    private List<Local> consultar(String sql, Object... parametros) {
        List<Local> itens = new ArrayList<>();
        try (PreparedStatement p = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) p.setObject(i + 1, parametros[i]);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) itens.add(JdbcMapeamento.local(r, ""));
            }
            return itens;
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public Local inserir(Local v) {
        Objects.requireNonNull(v, "Informe o registro.");
        JdbcUtil.exigirNovo(v.getId());
        // Oracle: pedir a coluna ID explicitamente evita receber ROWID.
        try (PreparedStatement p = conexao.prepareStatement(
                "INSERT INTO local (nome, endereco, bairro, descricao, latitude, longitude, dados_demonstrativos) VALUES (?, ?, ?, ?, ?, ?, ?)", new String[] {"ID"})) {
            preencher(p, v);
            p.executeUpdate();
            long id = JdbcUtil.chaveGerada(p);
            return buscarPorId(id).orElseThrow(() -> new PersistenciaException("PERSISTENCIA", "Não foi possível reler o registro inserido."));
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public void atualizar(Local v) {
        Objects.requireNonNull(v, "Informe o registro.");
        long id = JdbcUtil.idPersistido(v.getId());
        try (PreparedStatement p = conexao.prepareStatement(
                "UPDATE local SET nome = ?, endereco = ?, bairro = ?, descricao = ?, latitude = ?, longitude = ?, dados_demonstrativos = ? WHERE id = ?")) {
            preencher(p, v);
            p.setLong(8, id);
            JdbcUtil.exigirUmaLinha(p.executeUpdate());
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public void excluir(long id) {
        try (PreparedStatement p = conexao.prepareStatement("DELETE FROM local WHERE id = ?")) {
            p.setLong(1, JdbcUtil.idPersistido(id));
            JdbcUtil.exigirUmaLinha(p.executeUpdate());
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    private void preencher(PreparedStatement p, Local v) throws SQLException {
        p.setString(1, v.getNome());
        p.setString(2, v.getEndereco());
        p.setString(3, v.getBairro());
        p.setString(4, v.getDescricao());
        p.setBigDecimal(5, v.getLatitude());
        p.setBigDecimal(6, v.getLongitude());
        p.setInt(7, v.isDadosDemonstrativos() ? 1 : 0);
    }

    @Override
    public List<Local> pesquisar(String nome, String bairro) {
        return consultar(SELECT + " WHERE LOWER(nome) LIKE LOWER(?) ESCAPE '!' AND LOWER(bairro) LIKE LOWER(?) ESCAPE '!' ORDER BY nome, id",
                JdbcUtil.buscaLiteral(nome), JdbcUtil.buscaLiteral(bairro));
    }
}
