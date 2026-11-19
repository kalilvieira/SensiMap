package br.com.sensimap.service;

import br.com.sensimap.dao.*;
import br.com.sensimap.model.*;
import java.time.LocalDate;
import java.util.List;

public final class AvaliacaoService {
    private final AvaliacaoDAO dao;
    private final UsuarioService usuarios;
    private final LocalService locais;

    public AvaliacaoService(AvaliacaoDAO dao, UsuarioService usuarios, LocalService locais) {
        this.dao = dao;
        this.usuarios = usuarios;
        this.locais = locais;
    }

    public List<Avaliacao> listar() { return dao.listar(); }
    public Avaliacao buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new PersistenciaException("NAO_ENCONTRADO", "Avaliação não encontrada."));
    }
    public List<Avaliacao> listarPorLocal(long localId) {
        locais.buscar(localId);
        return dao.listarPorLocal(localId);
    }
    public IndicadoresSensoriais indicadores(long localId, PeriodoDia periodo) {
        locais.buscar(localId);
        return dao.indicadores(localId, periodo);
    }
    public Avaliacao cadastrar(long usuarioId, long localId, LocalDate data, PeriodoDia periodo,
            int ruido, int movimento, int imprevisibilidade, String comentario) {
        return dao.inserir(new Avaliacao(usuarios.buscar(usuarioId), locais.buscar(localId),
                data, periodo, ruido, movimento, imprevisibilidade, comentario));
    }
    public void atualizar(long id, long usuarioId, long localId, LocalDate data, PeriodoDia periodo,
            int ruido, int movimento, int imprevisibilidade, String comentario) {
        Avaliacao atual = buscar(id);
        dao.atualizar(new Avaliacao(id, usuarios.buscar(usuarioId), locais.buscar(localId),
                data, periodo, ruido, movimento, imprevisibilidade, comentario, atual.getCriadoEm()));
    }
    public void excluir(long id) { dao.excluir(id); }
}
