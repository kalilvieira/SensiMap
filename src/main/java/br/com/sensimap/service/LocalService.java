package br.com.sensimap.service;

import br.com.sensimap.dao.*;
import br.com.sensimap.model.Local;
import br.com.sensimap.util.ValidacaoUtil;
import java.util.List;

public final class LocalService {
    private final LocalDAO dao;
    public LocalService(LocalDAO dao) { this.dao = dao; }
    public List<Local> listar() { return dao.listar(); }
    public List<Local> pesquisar(String nome, String bairro) {
        return dao.pesquisar(ValidacaoUtil.texto(nome, "Nome", 120, false),
                ValidacaoUtil.texto(bairro, "Bairro", 80, false));
    }
    public Local buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new PersistenciaException("NAO_ENCONTRADO", "Local não encontrado."));
    }
    public Local cadastrar(Local local) {
        if (local == null || local.getId() != null) throw new IllegalArgumentException("Informe um novo local, sem ID.");
        return dao.inserir(local);
    }
    public void atualizar(Local local) {
        if (local == null || local.getId() == null) throw new IllegalArgumentException("Informe o local com ID.");
        buscar(local.getId());
        dao.atualizar(local);
    }
    public void excluir(long id) { dao.excluir(id); }
}
