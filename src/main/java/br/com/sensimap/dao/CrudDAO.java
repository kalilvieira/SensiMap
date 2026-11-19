package br.com.sensimap.dao;

import java.util.List;
import java.util.Optional;

/** Objetos novos têm ID nulo; inserir devolve o objeto reconstituído do banco. */
public interface CrudDAO<T> {
    List<T> listar();
    Optional<T> buscarPorId(long id);
    T inserir(T entidade);
    void atualizar(T entidade);
    void excluir(long id);
}
