package br.com.sensimap.dao.jdbc;

import br.com.sensimap.dao.LocalEstimuloDAO;
import br.com.sensimap.model.Estimulo;
import java.sql.*;
import java.util.*;

public final class JdbcLocalEstimuloDAO implements LocalEstimuloDAO {
    private final Connection conexao;
    public JdbcLocalEstimuloDAO(Connection conexao) { this.conexao = Objects.requireNonNull(conexao); }

    @Override
    public List<Estimulo> listarPorLocal(long localId) {
        List<Estimulo> itens = new ArrayList<>();
        try (PreparedStatement p = conexao.prepareStatement("SELECT e.* FROM estimulo e JOIN local_estimulo le ON le.estimulo_id = e.id WHERE le.local_id = ? ORDER BY e.nome, e.id")) {
            p.setLong(1, JdbcUtil.idPersistido(localId));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) itens.add(JdbcMapeamento.estimulo(r));
            }
            return itens;
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public void associar(long localId, long estimuloId) {
        executar("INSERT INTO local_estimulo (local_id, estimulo_id) VALUES (?, ?)", localId, estimuloId);
    }

    @Override
    public void desassociar(long localId, long estimuloId) {
        executar("DELETE FROM local_estimulo WHERE local_id = ? AND estimulo_id = ?", localId, estimuloId);
    }

    private void executar(String sql, long localId, long estimuloId) {
        try (PreparedStatement p = conexao.prepareStatement(sql)) {
            p.setLong(1, JdbcUtil.idPersistido(localId));
            p.setLong(2, JdbcUtil.idPersistido(estimuloId));
            JdbcUtil.exigirUmaLinha(p.executeUpdate());
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }
}
