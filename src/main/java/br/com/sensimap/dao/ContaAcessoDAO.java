package br.com.sensimap.dao;

import br.com.sensimap.model.ContaAcesso;
import java.util.Optional;

public interface ContaAcessoDAO {
    Optional<ContaAcesso> buscarPorApelido(String apelido);
    void inserir(ContaAcesso conta);
}
