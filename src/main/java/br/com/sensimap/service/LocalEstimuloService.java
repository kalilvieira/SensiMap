package br.com.sensimap.service;

import br.com.sensimap.dao.LocalEstimuloDAO;
import br.com.sensimap.model.Estimulo;
import java.util.List;

public final class LocalEstimuloService {
    private final LocalEstimuloDAO dao;
    private final LocalService locais;
    private final EstimuloService estimulos;

    public LocalEstimuloService(LocalEstimuloDAO dao, LocalService locais, EstimuloService estimulos) {
        this.dao = dao;
        this.locais = locais;
        this.estimulos = estimulos;
    }
    public List<Estimulo> listarPorLocal(long localId) {
        locais.buscar(localId);
        return dao.listarPorLocal(localId);
    }
    public void associar(long localId, long estimuloId) {
        locais.buscar(localId);
        estimulos.buscar(estimuloId);
        dao.associar(localId, estimuloId);
    }
    public void desassociar(long localId, long estimuloId) {
        locais.buscar(localId);
        estimulos.buscar(estimuloId);
        dao.desassociar(localId, estimuloId);
    }
}
