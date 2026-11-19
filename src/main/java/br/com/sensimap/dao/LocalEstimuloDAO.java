package br.com.sensimap.dao;

import br.com.sensimap.model.Estimulo;
import java.util.List;

public interface LocalEstimuloDAO {
    List<Estimulo> listarPorLocal(long localId);
    void associar(long localId, long estimuloId);
    void desassociar(long localId, long estimuloId);
}
