package br.com.sensimap.dao;

/** Mensagem pública controlada; nunca inclui SQL, conexão ou texto do driver. */
public final class PersistenciaException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String codigo;

    public PersistenciaException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() { return codigo; }
}
