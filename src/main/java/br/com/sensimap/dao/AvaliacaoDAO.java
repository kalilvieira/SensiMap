package br.com.sensimap.dao;

import br.com.sensimap.model.*;
import java.util.List;

public interface AvaliacaoDAO extends CrudDAO<Avaliacao> {
    List<Avaliacao> listarPorLocal(long localId);
    IndicadoresSensoriais indicadores(long localId, PeriodoDia periodo);
}
