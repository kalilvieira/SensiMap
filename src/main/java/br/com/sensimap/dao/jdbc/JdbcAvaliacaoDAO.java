package br.com.sensimap.dao.jdbc;

import br.com.sensimap.dao.*;
import br.com.sensimap.model.*;
import java.sql.*;
import java.util.*;

/** A conexão pertence ao chamador; este DAO não faz commit nem fecha a conexão. */
public final class JdbcAvaliacaoDAO implements AvaliacaoDAO {
    private final Connection conexao;
    private static final String SELECT = """
            SELECT a.*, u.id u_id, u.apelido u_apelido, u.criado_em u_criado_em,
            l.id l_id, l.nome l_nome, l.endereco l_endereco, l.bairro l_bairro,
            l.descricao l_descricao, l.latitude l_latitude, l.longitude l_longitude,
            l.dados_demonstrativos l_dados_demonstrativos, l.criado_em l_criado_em
            FROM avaliacao a JOIN usuario u ON u.id = a.usuario_id JOIN local l ON l.id = a.local_id
            """;

    public JdbcAvaliacaoDAO(Connection conexao) { this.conexao = Objects.requireNonNull(conexao); }

    @Override
    public List<Avaliacao> listar() { return consultar(SELECT + " ORDER BY a.id"); }

    @Override
    public Optional<Avaliacao> buscarPorId(long id) {
        return consultar(SELECT + " WHERE a.id = ?", JdbcUtil.idPersistido(id)).stream().findFirst();
    }

    private List<Avaliacao> consultar(String sql, Object... parametros) {
        List<Avaliacao> itens = new ArrayList<>();
        try (PreparedStatement p = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) p.setObject(i + 1, parametros[i]);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) itens.add(JdbcMapeamento.avaliacao(r));
            }
            return itens;
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public Avaliacao inserir(Avaliacao v) {
        Objects.requireNonNull(v, "Informe o registro.");
        JdbcUtil.exigirNovo(v.getId());
        // Oracle: pedir a coluna ID explicitamente evita receber ROWID.
        try (PreparedStatement p = conexao.prepareStatement(
                "INSERT INTO avaliacao (usuario_id, local_id, data_visita, periodo, ruido, movimento, imprevisibilidade, comentario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", new String[] {"ID"})) {
            preencher(p, v);
            p.executeUpdate();
            long id = JdbcUtil.chaveGerada(p);
            return buscarPorId(id).orElseThrow(() -> new PersistenciaException("PERSISTENCIA", "Não foi possível reler o registro inserido."));
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public void atualizar(Avaliacao v) {
        Objects.requireNonNull(v, "Informe o registro.");
        long id = JdbcUtil.idPersistido(v.getId());
        try (PreparedStatement p = conexao.prepareStatement(
                "UPDATE avaliacao SET usuario_id = ?, local_id = ?, data_visita = ?, periodo = ?, ruido = ?, movimento = ?, imprevisibilidade = ?, comentario = ? WHERE id = ?")) {
            preencher(p, v);
            p.setLong(9, id);
            JdbcUtil.exigirUmaLinha(p.executeUpdate());
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    @Override
    public void excluir(long id) {
        try (PreparedStatement p = conexao.prepareStatement("DELETE FROM avaliacao WHERE id = ?")) {
            p.setLong(1, JdbcUtil.idPersistido(id));
            JdbcUtil.exigirUmaLinha(p.executeUpdate());
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }

    private void preencher(PreparedStatement p, Avaliacao v) throws SQLException {
        p.setLong(1, v.getUsuario().getId());
        p.setLong(2, v.getLocal().getId());
        p.setDate(3, java.sql.Date.valueOf(v.getDataVisita()));
        p.setString(4, v.getPeriodo().name());
        p.setInt(5, v.getRuido());
        p.setInt(6, v.getMovimento());
        p.setInt(7, v.getImprevisibilidade());
        p.setString(8, v.getComentario());
    }

    @Override
    public List<Avaliacao> listarPorLocal(long localId) {
        return consultar(SELECT + " WHERE a.local_id = ? ORDER BY a.data_visita DESC, a.id DESC", JdbcUtil.idPersistido(localId));
    }

    @Override
    public IndicadoresSensoriais indicadores(long localId, PeriodoDia periodo) {
        String sql = "SELECT COUNT(*) n, AVG(ruido) ruido, AVG(movimento) movimento, "
                + "AVG(imprevisibilidade) imprevisibilidade, MAX(data_visita) ultima_visita FROM avaliacao WHERE local_id = ?"
                + (periodo == null ? "" : " AND periodo = ?");
        try (PreparedStatement p = conexao.prepareStatement(sql)) {
            p.setLong(1, JdbcUtil.idPersistido(localId));
            if (periodo != null) p.setString(2, periodo.name());
            try (ResultSet r = p.executeQuery()) {
                r.next();
                java.sql.Date ultima = r.getDate("ultima_visita");
                return new IndicadoresSensoriais(r.getLong("n"), r.getBigDecimal("ruido"),
                        r.getBigDecimal("movimento"), r.getBigDecimal("imprevisibilidade"),
                        periodo, ultima == null ? null : ultima.toLocalDate());
            }
        } catch (SQLException e) { throw JdbcUtil.traduzir(e); }
    }
}
