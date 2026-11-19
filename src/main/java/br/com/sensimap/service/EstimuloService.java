package br.com.sensimap.service;

import br.com.sensimap.dao.*;
import br.com.sensimap.model.Estimulo;
import java.util.List;

public final class EstimuloService {
    private final EstimuloDAO dao;
    public EstimuloService(EstimuloDAO dao) { this.dao = dao; }
    public List<Estimulo> listar() { return dao.listar(); }
    public Estimulo buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new PersistenciaException("NAO_ENCONTRADO", "Estímulo não encontrado."));
    }
    public Estimulo cadastrar(String nome, String descricao) { return dao.inserir(new Estimulo(nome, descricao)); }
    public void atualizar(long id, String nome, String descricao) {
        buscar(id);
        dao.atualizar(new Estimulo(id, nome, descricao));
    }
    public void excluir(long id) { dao.excluir(id); }
}
