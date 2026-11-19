package br.com.sensimap.web;

import br.com.sensimap.model.PeriodoDia;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;

final class Parametros {
    private final Map<String,String> valores;
    Parametros(String texto) {
        valores = new HashMap<>();
        if (texto == null || texto.isEmpty()) return;
        for (String par : texto.split("&", -1)) {
            String[] kv = par.split("=", 2);
            String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String v = kv.length==2?URLDecoder.decode(kv[1], StandardCharsets.UTF_8):"";
            if (valores.putIfAbsent(k,v)!=null) throw new IllegalArgumentException("Parâmetro repetido.");
        }
    }
    String texto(String nome) { return valores.get(nome); }
    long id(String nome) { return idValor(texto(nome)); }
    static long idValor(String valor) {
        try { long n=Long.parseLong(valor); if(n>0)return n; }
        catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("ID deve ser um inteiro positivo.");
    }
    int nota(String nome) {
        try { return br.com.sensimap.util.ValidacaoUtil.nota(Integer.parseInt(texto(nome)), nome); }
        catch(NumberFormatException e) { throw new IllegalArgumentException("Informe notas inteiras de 1 a 5."); }
    }
    LocalDate data() {
        try { return LocalDate.parse(texto("data_visita")); }
        catch (RuntimeException e) { throw new IllegalArgumentException("Informe uma data válida no formato AAAA-MM-DD."); }
    }
    PeriodoDia periodo(boolean opcional) {
        String p=texto("periodo");
        if(opcional&&(p==null||p.isBlank()))return null;
        try {return PeriodoDia.valueOf(p);}
        catch(RuntimeException e){throw new IllegalArgumentException("Período deve ser MANHA, TARDE ou NOITE.");}
    }
    BigDecimal decimal(String nome) {
        String s=texto(nome);if(s==null||s.isBlank())return null;
        try{return new BigDecimal(s);}catch(NumberFormatException e){throw new IllegalArgumentException("Coordenada inválida.");}
    }
}
