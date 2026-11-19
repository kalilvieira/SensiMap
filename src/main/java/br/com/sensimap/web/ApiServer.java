package br.com.sensimap.web;

import br.com.sensimap.config.DatabaseConfig;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

/** Servidor HTTP local ou publicado por proxy HTTPS. Não registra IPs nem expõe arquivos do projeto. */
public final class ApiServer {
    private static final Set<String> ARQUIVOS=Set.of("index.html","login.html","cadastro.html","locais.html","contribuir.html","favicon.svg",
            "css/styles.css","js/api.js","js/regras.js","js/ui.js","js/auth.js","js/login.js","js/cadastro.js","js/locais.js","js/contribuir.js");
    private ApiServer() { }
    public static void iniciar() throws IOException {
        DatabaseConfig config=DatabaseConfig.fromEnvironment();
        ServidorConfig servidorConfig=ServidorConfig.fromEnvironment();
        HttpServer server=HttpServer.create(new InetSocketAddress(servidorConfig.host(),servidorConfig.port()),0);
        var executor=Executors.newFixedThreadPool(4);
        server.setExecutor(executor);
        var api=new ApiHandler(config,servidorConfig.remoto());
        server.createContext("/",x->{
            try {
                var h=x.getResponseHeaders();
                h.set("X-Content-Type-Options","nosniff");h.set("Referrer-Policy","no-referrer");
                h.set("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'none'; frame-ancestors 'none'; form-action 'self'");
                String host=x.getRequestHeaders().getFirst("Host");
                if(servidorConfig.redirectLocalhost(host)){
                    h.set("Location","http://127.0.0.1:8080"+x.getRequestURI().toASCIIString());
                    x.sendResponseHeaders(307,-1);return;
                }
                if(!servidorConfig.hostAllowed(host)) {texto(x,403,"Host não permitido.");return;}
                String origin=x.getRequestHeaders().getFirst("Origin");
                if(!servidorConfig.originAllowed(origin)){texto(x,403,"Origem não permitida.");return;}
                String path=x.getRequestURI().getPath();
                if(path.startsWith("/api/")){api.handle(x);return;}
                if(!x.getRequestMethod().equals("GET")){texto(x,405,"Método não permitido.");return;}
                String file=path.equals("/")?"index.html":path.substring(1);
                if(!ARQUIVOS.contains(file)){texto(x,404,"Página não encontrada.");return;}
                try(var in=ApiServer.class.getResourceAsStream("/web/"+file)) {
                    if(in==null){texto(x,404,"Recursos web ausentes. Recompile usando o script de inicialização.");return;}
                    byte[] bytes=in.readAllBytes();
                    String type=file.endsWith(".css")?"text/css":file.endsWith(".js")?"text/javascript":file.endsWith(".svg")?"image/svg+xml":"text/html";
                    h.set("Content-Type",type+"; charset=utf-8");h.set("Cache-Control","no-cache");x.sendResponseHeaders(200,bytes.length);x.getResponseBody().write(bytes);
                }
            }finally{x.close();}
        });
        Runtime.getRuntime().addShutdownHook(new Thread(()->{server.stop(1);executor.shutdownNow();}));
        server.start();
        System.out.println("SensiMap disponível em " + servidorConfig.displayAddress());
        System.out.println("Modo Oracle: operações web usarão JDBC; a conexão será verificada em cada solicitação.");
        if(!servidorConfig.remoto())System.out.println("Para encerrar, pressione Ctrl+C neste terminal.");
    }
    private static void texto(HttpExchange x,int status,String msg)throws IOException{
        byte[] b=msg.getBytes(StandardCharsets.UTF_8);x.getResponseHeaders().set("Content-Type","text/plain; charset=utf-8");x.sendResponseHeaders(status,b.length);x.getResponseBody().write(b);
    }
}
