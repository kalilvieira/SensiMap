package br.com.sensimap.web;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Configuração HTTP local ou pública, sem misturar credenciais do banco. */
record ServidorConfig(String host, int port, Set<String> origensPermitidas, boolean remoto) {
    static ServidorConfig fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    static ServidorConfig fromEnvironment(Map<String, String> ambiente) {
        String portValue = ambiente.get("PORT");
        if (portValue == null || portValue.isBlank()) {
            return new ServidorConfig("127.0.0.1", 8080, Set.of("http://127.0.0.1:8080"), false);
        }
        int port;
        try { port = Integer.parseInt(portValue); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("PORT deve ser um número entre 1 e 65535."); }
        if (port < 1 || port > 65535) throw new IllegalArgumentException("PORT deve ser um número entre 1 e 65535.");

        String configured = ambiente.get("APP_ORIGIN");
        if (configured == null || configured.isBlank()) {
            throw new IllegalArgumentException("Defina APP_ORIGIN com o endereço HTTPS do frontend publicado.");
        }
        Set<String> origins = new LinkedHashSet<>();
        for (String value : configured.split(",")) origins.add(validOrigin(value));
        return new ServidorConfig("0.0.0.0", port, Set.copyOf(origins), true);
    }

    private static String validOrigin(String value) {
        String origin = value.strip();
        if (origin.endsWith("/")) origin = origin.substring(0, origin.length() - 1);
        URI uri;
        try { uri = URI.create(origin); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("APP_ORIGIN deve conter endereços HTTPS válidos."); }
        boolean pathValid = uri.getPath() == null || uri.getPath().isEmpty();
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || !pathValid
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("APP_ORIGIN deve conter somente origens HTTPS, sem caminhos ou credenciais.");
        }
        return origin;
    }

    boolean redirectLocalhost(String requestHost) {
        return !remoto && "localhost:8080".equals(requestHost);
    }

    boolean hostAllowed(String requestHost) {
        return remoto || "127.0.0.1:8080".equals(requestHost);
    }

    boolean originAllowed(String origin) {
        return origin == null || origensPermitidas.contains(origin);
    }

    String displayAddress() {
        return remoto ? "porta pública " + port : "http://127.0.0.1:8080";
    }
}
