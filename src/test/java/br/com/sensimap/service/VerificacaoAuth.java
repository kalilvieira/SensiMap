package br.com.sensimap.service;

import br.com.sensimap.dao.*;
import br.com.sensimap.model.*;
import br.com.sensimap.util.SenhaUtil;
import java.time.LocalDateTime;
import java.util.*;

public final class VerificacaoAuth {
    private static int n;
    public static void main(String[] args) {
        var usuarios=new UsuariosMemoria();var contas=new ContasMemoria();var auth=new AuthService(new UsuarioService(usuarios),contas);
        var acesso=auth.cadastrar("perfil_teste","senha-segura");
        verificar(acesso.perfil()==PerfilAcesso.USUARIO);
        verificar(acesso.usuarioId()==1);
        verificar(contas.conta!=null&&!contas.conta.senhaHash().contains("senha-segura"));
        verificar(SenhaUtil.corresponde("senha-segura",contas.conta.senhaHash(),contas.conta.senhaSalt(),contas.conta.senhaIteracoes()));
        verificar(auth.entrar("PERFIL_TESTE","senha-segura").usuarioId()==1);
        rejeitarCredenciais(()->auth.entrar("perfil_teste","incorreta"));
        rejeitarConflito(()->auth.cadastrar("PERFIL_TESTE","outra-senha"));
        System.out.println(n+" verificações de autenticação aprovadas (serviço em memória; sem Oracle).");
    }
    private static void verificar(boolean v){if(!v)throw new AssertionError("Contrato de autenticação divergente.");n++;}
    private static void rejeitarCredenciais(Runnable r){rejeitar(r,"CREDENCIAIS");}
    private static void rejeitarConflito(Runnable r){rejeitar(r,"CONFLITO");}
    private static void rejeitar(Runnable r,String codigo){try{r.run();}catch(PersistenciaException e){verificar(e.getCodigo().equals(codigo));return;}throw new AssertionError("Operação inválida aceita.");}

    private static final class ContasMemoria implements ContaAcessoDAO {
        private ContaAcesso conta;
        public Optional<ContaAcesso> buscarPorApelido(String apelido){return conta!=null&&conta.apelido().equalsIgnoreCase(apelido)?Optional.of(conta):Optional.empty();}
        public void inserir(ContaAcesso valor){conta=valor;}
    }
    private static final class UsuariosMemoria implements UsuarioDAO {
        private final Map<Long,Usuario> itens=new LinkedHashMap<>();private long proximo=1;
        public List<Usuario> listar(){return List.copyOf(itens.values());}
        public Optional<Usuario> buscarPorId(long id){return Optional.ofNullable(itens.get(id));}
        public Usuario inserir(Usuario v){var salvo=new Usuario(proximo++,v.getApelido(),LocalDateTime.now());itens.put(salvo.getId(),salvo);return salvo;}
        public void atualizar(Usuario v){itens.put(v.getId(),v);}
        public void excluir(long id){itens.remove(id);}
    }
}
