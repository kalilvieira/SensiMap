package br.com.sensimap.service;

import br.com.sensimap.dao.*;
import br.com.sensimap.model.Usuario;
import java.util.List;

public final class UsuarioService {
    private final UsuarioDAO dao;
    public UsuarioService(UsuarioDAO dao) { this.dao = dao; }
    public List<Usuario> listar() { return dao.listar(); }
    public Usuario buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new PersistenciaException("NAO_ENCONTRADO", "Usuário não encontrado."));
    }
    public Usuario cadastrar(String apelido) { return dao.inserir(new Usuario(apelido)); }
    public void atualizar(long id, String apelido) {
        Usuario atual = buscar(id);
        dao.atualizar(new Usuario(id, apelido, atual.getCriadoEm()));
    }
    public void excluir(long id) { dao.excluir(id); }
}
