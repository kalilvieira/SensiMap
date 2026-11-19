package br.com.sensimap.web;

import br.com.sensimap.model.*;
import com.sun.net.httpserver.HttpExchange;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Sessões opacas em memória; reiniciar o servidor encerra todos os acessos. */
final class GerenciadorSessoes {
    private static final String COOKIE="sensimap_session";
    private static final Duration DURACAO=Duration.ofHours(8);
    private final SecureRandom aleatorio=new SecureRandom();
    private final Map<String,Sessao> sessoes=new ConcurrentHashMap<>();
    private final boolean cookieSeguro;

    GerenciadorSessoes(boolean cookieSeguro){this.cookieSeguro=cookieSeguro;}

    Sessao criar(HttpExchange x,AcessoAutenticado acesso) {
        byte[] bytes=new byte[32];aleatorio.nextBytes(bytes);
        String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Sessao sessao=new Sessao(acesso.usuarioId(),acesso.apelido(),acesso.perfil(),Instant.now().plus(DURACAO),token);
        sessoes.put(token,sessao);return sessao;
    }
    Optional<Sessao> buscar(HttpExchange x) {
        String token=token(x);if(token==null)return Optional.empty();
        Sessao s=sessoes.get(token);
        if(s==null||s.expiraEm().isBefore(Instant.now())){sessoes.remove(token);return Optional.empty();}
        return Optional.of(s);
    }
    void remover(HttpExchange x){String token=token(x);if(token!=null)sessoes.remove(token);}
    void definirCookie(HttpExchange x,Sessao s){
        x.getResponseHeaders().add("Set-Cookie",cookie(s.token(),DURACAO.toSeconds()));
    }
    void limparCookie(HttpExchange x){x.getResponseHeaders().add("Set-Cookie",cookie("",0));}
    private String cookie(String token,long maxAge){return COOKIE+"="+token+"; Path=/; HttpOnly; SameSite=Strict; Max-Age="+maxAge+(cookieSeguro?"; Secure":"");}
    private String token(HttpExchange x){
        for(String cabecalho:x.getRequestHeaders().getOrDefault("Cookie",List.of()))
            for(String parte:cabecalho.split(";")){String[] kv=parte.strip().split("=",2);if(kv.length==2&&kv[0].equals(COOKIE))return kv[1];}
        return null;
    }
    record Sessao(long usuarioId,String apelido,PerfilAcesso perfil,Instant expiraEm,String token){ }
}
