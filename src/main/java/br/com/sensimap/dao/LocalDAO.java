package br.com.sensimap.dao;

import br.com.sensimap.model.Local;
import java.util.List;

public interface LocalDAO extends CrudDAO<Local> {
    List<Local> pesquisar(String nome, String bairro);
}
