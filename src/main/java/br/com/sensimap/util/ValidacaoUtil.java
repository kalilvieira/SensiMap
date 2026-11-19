package br.com.sensimap.util;

public final class ValidacaoUtil {
    private ValidacaoUtil() { }

    public static Long id(Long valor) {
        if (valor != null && valor <= 0) {
            throw new IllegalArgumentException("O ID deve ser positivo.");
        }
        return valor;
    }

    public static String texto(String valor, String campo, int limite, boolean obrigatorio) {
        if (valor == null || valor.isBlank()) {
            if (obrigatorio) {
                throw new IllegalArgumentException(campo + " é obrigatório.");
            }
            return null;
        }
        String texto = valor.strip();
        if (texto.codePointCount(0, texto.length()) > limite) {
            throw new IllegalArgumentException(campo + " deve ter no máximo " + limite + " caracteres.");
        }
        return texto;
    }

    public static int nota(int valor, String campo) {
        if (valor < 1 || valor > 5) {
            throw new IllegalArgumentException(campo + " deve estar entre 1 e 5.");
        }
        return valor;
    }
}
