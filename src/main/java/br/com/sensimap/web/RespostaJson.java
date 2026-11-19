package br.com.sensimap.web;

import br.com.sensimap.model.*;
import br.com.sensimap.service.IndicadorService;
import java.util.*;

final class RespostaJson {
    private RespostaJson() { }
    static Map<String,Object> objeto(Object... pares) {
        var m = new LinkedHashMap<String,Object>();
        for (int i=0; i<pares.length; i+=2) m.put((String)pares[i], pares[i+1]);
        return m;
    }
    static Object converter(Object v) {
        if (v instanceof List<?> itens) return itens.stream().map(RespostaJson::converter).toList();
        if (v instanceof Usuario u) return objeto("id",u.getId(),"apelido",u.getApelido());
        if (v instanceof Estimulo e) return objeto("id",e.getId(),"nome",e.getNome(),"descricao",e.getDescricao());
        if (v instanceof Local l) return objeto("id",l.getId(),"nome",l.getNome(),"bairro",l.getBairro(),"endereco",l.getEndereco(),
                "descricao",l.getDescricao(),"latitude",l.getLatitude(),"longitude",l.getLongitude(),"dados_demonstrativos",l.isDadosDemonstrativos());
        if (v instanceof Avaliacao a) return objeto("id",a.getId(),"usuario_id",a.getUsuario().getId(),"local_id",a.getLocal().getId(),
                "data_visita",a.getDataVisita().toString(),"periodo",a.getPeriodo().name(),"ruido",a.getRuido(),"movimento",a.getMovimento(),
                "imprevisibilidade",a.getImprevisibilidade(),"comentario",a.getComentario());
        if (v instanceof IndicadoresSensoriais i) {
            var s = new IndicadorService();
            return objeto("quantidade",i.quantidade(),"periodo",i.periodo(),"ultima_visita",i.ultimaVisita()==null?null:i.ultimaVisita().toString(),
                    "ruido",i.ruido(),"movimento",i.movimento(),"imprevisibilidade",i.imprevisibilidade(),
                    "classificacoes",objeto("ruido",s.classificar(i.ruido(),i.quantidade()).name(),
                            "movimento",s.classificar(i.movimento(),i.quantidade()).name(),
                            "imprevisibilidade",s.classificar(i.imprevisibilidade(),i.quantidade()).name()));
        }
        return v;
    }
}
