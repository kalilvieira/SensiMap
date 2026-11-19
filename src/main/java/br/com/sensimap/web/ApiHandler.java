package br.com.sensimap.web;

import br.com.sensimap.config.DatabaseConfig;
import br.com.sensimap.dao.PersistenciaException;
import br.com.sensimap.dao.jdbc.*;
import br.com.sensimap.model.*;
import br.com.sensimap.service.*;
import br.com.sensimap.util.JsonUtil;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Set;

final class ApiHandler implements HttpHandler {
    private final DatabaseConfig config;
    private final GerenciadorSessoes sessoes=new GerenciadorSessoes();
    ApiHandler(DatabaseConfig config){this.config=config;}

    @Override public void handle(HttpExchange x)throws IOException {
        try {
            String method=x.getRequestMethod(),path=x.getRequestURI().getPath();
            if(!Set.of("GET","POST","PUT","DELETE").contains(method)){responder(x,405,erro("Método não permitido.","METODO"));return;}
            if(path.equals("/api/config")&&method.equals("GET")){responder(x,200,RespostaJson.objeto("modo","ORACLE"));return;}
            if(!method.equals("GET")&&!"local".equals(x.getRequestHeaders().getFirst("X-SensiMap"))){responder(x,403,erro("Use os formulários do SensiMap local.","ORIGEM"));return;}
            if(path.equals("/api/auth/sessao")&&method.equals("GET")){
                responder(x,200,sessaoJson(sessoes.buscar(x).orElseThrow(()->new HttpFalha(401,"Faça login para continuar."))));return;
            }
            if(path.equals("/api/auth/logout")&&method.equals("POST")){
                sessoes.remover(x);sessoes.limparCookie(x);responder(x,200,ok());return;
            }
            Parametros p=parametros(x,method);
            Object result;int status=200;
            try(Connection c=config.abrirConexao()) {
                c.setAutoCommit(false);
                try {
                    if(path.startsWith("/api/auth/")){result=autenticacao(x,c,method,path,p);status=path.endsWith("cadastro")?201:200;}
                    else {
                        var sessao=sessoes.buscar(x).orElseThrow(()->new HttpFalha(401,"Faça login para continuar."));
                        if(path.equals("/api/health")&&method.equals("GET")){
                            if(!c.isValid(3))throw new SQLException();
                            result=RespostaJson.objeto("banco","conectado","modo","ORACLE");
                        }else result=rotear(c,method,path,p,sessao);
                        if(method.equals("POST"))status=201;
                    }
                    c.commit();
                }catch(RuntimeException|SQLException e){try{c.rollback();}catch(SQLException ignored){ }throw e;}
            }
            responder(x,status,RespostaJson.converter(result));
        }catch(HttpFalha e){responder(x,e.status,erro(e.getMessage(),"ACESSO"));}
        catch(IllegalArgumentException e){responder(x,400,erro(e.getMessage(),"VALIDACAO"));}
        catch(PersistenciaException e){
            int status=switch(e.getCodigo()){case "CREDENCIAIS"->401;case "CONFLITO"->409;case "NAO_ENCONTRADO"->404;case "VALIDACAO"->400;default->503;};
            responder(x,status,erro(e.getMessage(),e.getCodigo()));
        }catch(SQLException e){responder(x,503,erro("Oracle indisponível. Confira as variáveis de ambiente, o driver e a rede institucional. Não houve confirmação de gravação; consulte os registros antes de repetir.","BANCO"));}
        catch(RuntimeException e){responder(x,500,erro("Não foi possível concluir a operação.","INTERNO"));}
        finally{x.close();}
    }

    private Parametros parametros(HttpExchange x,String method)throws IOException {
        if(method.equals("POST")||method.equals("PUT")){
            String type=x.getRequestHeaders().getFirst("Content-Type");
            if(type==null||!type.split(";",2)[0].trim().equalsIgnoreCase("application/x-www-form-urlencoded"))
                throw new HttpFalha(415,"Envie um formulário URL-encoded.");
            byte[] body=x.getRequestBody().readNBytes(16385);
            if(body.length>16384)throw new HttpFalha(413,"Formulário excede o limite permitido.");
            return new Parametros(new String(body,StandardCharsets.UTF_8));
        }
        return new Parametros(x.getRequestURI().getRawQuery());
    }

    private Object autenticacao(HttpExchange x,Connection c,String method,String path,Parametros p) {
        var auth=new AuthService(new UsuarioService(new JdbcUsuarioDAO(c)),new JdbcContaAcessoDAO(c));
        if(path.equals("/api/auth/cadastro")&&method.equals("POST")){
            var sessao=sessoes.criar(x,auth.cadastrar(p.texto("apelido"),p.texto("senha")));sessoes.definirCookie(x,sessao);return sessaoJson(sessao);
        }
        if(path.equals("/api/auth/login")&&method.equals("POST")){
            var sessao=sessoes.criar(x,auth.entrar(p.texto("apelido"),p.texto("senha")));sessoes.definirCookie(x,sessao);return sessaoJson(sessao);
        }
        throw new HttpFalha(404,"Rota de autenticação não encontrada.");
    }

    private Object rotear(Connection c,String method,String path,Parametros p,GerenciadorSessoes.Sessao sessao) {
        String[] s=path.substring(1).split("/",-1);
        if(s.length<2||s.length>5)throw new HttpFalha(404,"Rota não encontrada.");
        String tipo=s[1];
        if(!Set.of("usuarios","locais","estimulos","avaliacoes").contains(tipo))throw new HttpFalha(404,"Rota não encontrada.");
        Long id=s.length>=3?Parametros.idValor(s[2]):null;
        var usuarios=new UsuarioService(new JdbcUsuarioDAO(c));
        var locais=new LocalService(new JdbcLocalDAO(c));
        var estimulos=new EstimuloService(new JdbcEstimuloDAO(c));
        var avaliacoes=new AvaliacaoService(new JdbcAvaliacaoDAO(c),usuarios,locais);
        var associacoes=new LocalEstimuloService(new JdbcLocalEstimuloDAO(c),locais,estimulos);
        if(sessao.perfil()!=PerfilAcesso.USUARIO)throw new HttpFalha(403,"Acesso não disponível.");
        if(!method.equals("GET")&&!tipo.equals("avaliacoes"))throw new HttpFalha(403,"Operação não disponível para usuários.");
        if(method.equals("DELETE"))throw new HttpFalha(403,"Exclusão não disponível pela interface.");

        if(s.length>=4){
            if(!tipo.equals("locais"))throw new HttpFalha(404,"Rota não encontrada.");
            if(s.length==4&&method.equals("GET"))return switch(s[3]){
                case "indicadores"->avaliacoes.indicadores(id,p.periodo(true));
                case "avaliacoes"->avaliacoes.listarPorLocal(id);
                case "estimulos"->associacoes.listarPorLocal(id);
                default->throw new HttpFalha(404,"Rota não encontrada.");};
            throw new HttpFalha(405,"Método ou rota não permitido.");
        }
        if(method.equals("GET"))return switch(tipo){
            case "usuarios"->id==null?usuarios.listar():usuarios.buscar(id);
            case "locais"->id==null?locais.pesquisar(p.texto("nome"),p.texto("bairro")):locais.buscar(id);
            case "estimulos"->id==null?estimulos.listar():estimulos.buscar(id);
            default->id==null?avaliacoes.listar():avaliacoes.buscar(id);};
        boolean inserir=method.equals("POST")&&id==null,editar=method.equals("PUT")&&id!=null;
        if(!inserir&&!editar)throw new HttpFalha(405,"Método não permitido nesta rota.");
        if(!tipo.equals("avaliacoes"))throw new HttpFalha(403,"Operação não disponível para usuários.");
        switch(tipo){
            case "avaliacoes":
                long usuarioId=p.id("usuario_id");
                if(usuarioId!=sessao.usuarioId())throw new HttpFalha(403,"Você só pode registrar percepções no seu perfil.");
                if(editar&&avaliacoes.buscar(id).getUsuario().getId()!=sessao.usuarioId())throw new HttpFalha(403,"Você só pode editar suas próprias percepções.");
                if(inserir)return avaliacoes.cadastrar(usuarioId,p.id("local_id"),p.data(),p.periodo(false),p.nota("ruido"),p.nota("movimento"),p.nota("imprevisibilidade"),p.texto("comentario"));
                avaliacoes.atualizar(id,usuarioId,p.id("local_id"),p.data(),p.periodo(false),p.nota("ruido"),p.nota("movimento"),p.nota("imprevisibilidade"),p.texto("comentario"));return avaliacoes.buscar(id);
            default:throw new HttpFalha(404,"Rota não encontrada.");
        }
    }

    private static Object sessaoJson(GerenciadorSessoes.Sessao s){return RespostaJson.objeto("perfil",s.perfil().name(),"apelido",s.apelido(),"usuarioId",s.usuarioId());}
    private static Object ok(){return RespostaJson.objeto("ok",true);}
    private static Object erro(String msg,String code){return RespostaJson.objeto("erro",msg,"codigo",code);}
    static void responder(HttpExchange x,int status,Object value)throws IOException{
        byte[] b=JsonUtil.escrever(value).getBytes(StandardCharsets.UTF_8);x.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");x.getResponseHeaders().set("Cache-Control","no-store");x.sendResponseHeaders(status,b.length);x.getResponseBody().write(b);
    }
    private static final class HttpFalha extends RuntimeException{
        private static final long serialVersionUID=1L;private final int status;
        HttpFalha(int status,String msg){super(msg);this.status=status;}
    }
}
