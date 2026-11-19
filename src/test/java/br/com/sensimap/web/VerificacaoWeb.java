package br.com.sensimap.web;

import br.com.sensimap.util.JsonUtil;
import br.com.sensimap.util.SenhaUtil;
import java.util.List;
import java.util.Map;

public final class VerificacaoWeb {
    private static int n;
    public static void main(String[] args) {
        verificar(JsonUtil.escrever("\"\\\n\r\t\u0000").equals("\"\\\"\\\\\\n\\r\\t\\u0000\""));
        verificar(JsonUtil.escrever(RespostaJson.objeto("n",null,"itens",List.of(1,true,"ação"))).equals("{\"n\":null,\"itens\":[1,true,\"ação\"]}"));
        Parametros p=new Parametros("apelido=ol%C3%A1+teste&local_id=23&periodo=MANHA&ruido=5&data_visita=2026-11-01");
        verificar(p.texto("apelido").equals("olá teste"));
        verificar(p.id("local_id")==23);
        verificar(p.nota("ruido")==5);
        verificar(p.data().toString().equals("2026-11-01"));
        verificar(p.periodo(false).name().equals("MANHA"));
        verificar(new Parametros("").periodo(true)==null);
        rejeitar(()->new Parametros("id=1&id=2"));
        rejeitar(()->new Parametros("id=%ZZ"));
        rejeitar(()->new Parametros("id=0").id("id"));
        rejeitar(()->new Parametros("id=9223372036854775808").id("id"));
        rejeitar(()->new Parametros("ruido=6").nota("ruido"));
        rejeitar(()->new Parametros("ruido=1.5").nota("ruido"));
        rejeitar(()->new Parametros("periodo=MADRUGADA").periodo(false));
        rejeitar(()->new Parametros("data_visita=2026-02-30").data());
        rejeitar(()->JsonUtil.escrever(Double.NaN));
        var senha=SenhaUtil.proteger("senha-segura");
        verificar(senha.iteracoes()>=100_000);
        verificar(!senha.hash().contains("senha-segura"));
        verificar(SenhaUtil.corresponde("senha-segura",senha.hash(),senha.salt(),senha.iteracoes()));
        verificar(!SenhaUtil.corresponde("senha-incorreta",senha.hash(),senha.salt(),senha.iteracoes()));
        rejeitar(()->SenhaUtil.proteger("curta"));
        var local=ServidorConfig.fromEnvironment(Map.of());
        verificar(local.host().equals("127.0.0.1")&&local.port()==8080&&!local.remoto());
        verificar(local.hostAllowed("127.0.0.1:8080")&&!local.hostAllowed("outro.example"));
        verificar(local.originAllowed(null)&&local.originAllowed("http://127.0.0.1:8080"));
        verificar(local.redirectLocalhost("localhost:8080"));
        var remoto=ServidorConfig.fromEnvironment(Map.of(
                "PORT","10000",
                "APP_ORIGIN","https://sensimap.vercel.app/, https://preview.vercel.app"));
        verificar(remoto.host().equals("0.0.0.0")&&remoto.port()==10000&&remoto.remoto());
        verificar(remoto.hostAllowed("sensimap-api.onrender.com"));
        verificar(remoto.originAllowed("https://sensimap.vercel.app"));
        verificar(remoto.originAllowed("https://preview.vercel.app"));
        verificar(!remoto.originAllowed("https://outro.example"));
        verificar(!remoto.redirectLocalhost("localhost:8080"));
        rejeitar(()->ServidorConfig.fromEnvironment(Map.of("PORT","abc","APP_ORIGIN","https://sensimap.vercel.app")));
        rejeitar(()->ServidorConfig.fromEnvironment(Map.of("PORT","70000","APP_ORIGIN","https://sensimap.vercel.app")));
        rejeitar(()->ServidorConfig.fromEnvironment(Map.of("PORT","10000")));
        rejeitar(()->ServidorConfig.fromEnvironment(Map.of("PORT","10000","APP_ORIGIN","http://sensimap.vercel.app")));
        rejeitar(()->ServidorConfig.fromEnvironment(Map.of("PORT","10000","APP_ORIGIN","https://sensimap.vercel.app/login")));
        System.out.println(n+" verificações de JSON, parâmetros e configuração HTTP aprovadas (sem Oracle).");
    }
    private static void verificar(boolean v){if(!v)throw new AssertionError("Contrato web divergente.");n++;}
    private static void rejeitar(Runnable r){try{r.run();}catch(IllegalArgumentException e){n++;return;}throw new AssertionError("Entrada inválida aceita.");}
}
