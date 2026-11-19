package br.com.sensimap.util;

import java.util.Map;

/** Serialização de saída apenas; a API recebe formulários URL-encoded. */
public final class JsonUtil {
    private JsonUtil() { }
    public static String escrever(Object valor) {
        if (valor == null) return "null";
        if (valor instanceof String || valor instanceof Enum<?>) return aspas(valor.toString());
        if (valor instanceof Boolean) return valor.toString();
        if (valor instanceof Number n) {
            if (n instanceof Double && !Double.isFinite(n.doubleValue()) || n instanceof Float && !Float.isFinite(n.floatValue()))
                throw new IllegalArgumentException("Número inválido para JSON.");
            return n.toString();
        }
        if (valor instanceof Map<?, ?> mapa) {
            var partes = new java.util.ArrayList<String>();
            for (var e : mapa.entrySet()) partes.add(aspas(e.getKey().toString()) + ":" + escrever(e.getValue()));
            return "{" + String.join(",", partes) + "}";
        }
        if (valor instanceof Iterable<?> lista) {
            var partes = new java.util.ArrayList<String>();
            for (var item : lista) partes.add(escrever(item));
            return "[" + String.join(",", partes) + "]";
        }
        throw new IllegalArgumentException("Tipo não serializável.");
    }
    private static String aspas(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> { if (c < 32) b.append(String.format("\\u%04x", (int)c)); else b.append(c); }
            }
        }
        return b.append('"').toString();
    }
}
