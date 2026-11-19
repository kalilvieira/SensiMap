package br.com.sensimap.service;

import br.com.sensimap.dao.*;
import br.com.sensimap.model.*;
import br.com.sensimap.util.SenhaUtil;
import java.util.Objects;

public final class AuthService {
    private final UsuarioService usuarios;
    private final ContaAcessoDAO contas;

    public AuthService(UsuarioService usuarios,ContaAcessoDAO contas) {
        this.usuarios=Objects.requireNonNull(usuarios);this.contas=Objects.requireNonNull(contas);
    }

    public AcessoAutenticado cadastrar(String apelido,String senha) {
        SenhaUtil.validarFormato(senha);
        if(contas.buscarPorApelido(apelido==null?"":apelido.strip()).isPresent())
            throw new PersistenciaException("CONFLITO","Já existe uma conta com este pseudônimo.");
        Usuario usuario=usuarios.cadastrar(apelido);
        var protegida=SenhaUtil.proteger(senha);
        ContaAcesso conta=new ContaAcesso(usuario.getId(),usuario.getApelido(),protegida.hash(),protegida.salt(),
                protegida.iteracoes(),PerfilAcesso.USUARIO,true);
        contas.inserir(conta);
        return new AcessoAutenticado(conta);
    }

    public AcessoAutenticado entrar(String apelido,String senha) {
        ContaAcesso conta=contas.buscarPorApelido(apelido==null?"":apelido.strip()).orElse(null);
        if(conta==null||!conta.ativa()||!SenhaUtil.corresponde(senha,conta.senhaHash(),conta.senhaSalt(),conta.senhaIteracoes()))
            throw new PersistenciaException("CREDENCIAIS","Pseudônimo ou senha incorretos.");
        return new AcessoAutenticado(conta);
    }
}
