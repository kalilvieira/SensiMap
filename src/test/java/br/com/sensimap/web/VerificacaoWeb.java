package br.com.sensimap.web;

import br.com.sensimap.util.JsonUtil;
import br.com.sensimap.util.SenhaUtil;
import java.util.List;

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
        System.out.println(n+" verificações de JSON/parâmetros HTTP aprovadas (sem Oracle).");
    }
    private static void verificar(boolean v){if(!v)throw new AssertionError("Contrato web divergente.");n++;}
    private static void rejeitar(Runnable r){try{r.run();}catch(IllegalArgumentException e){n++;return;}throw new AssertionError("Entrada inválida aceita.");}
}
