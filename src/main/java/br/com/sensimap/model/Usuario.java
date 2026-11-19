package br.com.sensimap.model;

import br.com.sensimap.util.ValidacaoUtil;
import java.time.LocalDateTime;

/** Objeto imutável; novos registros têm ID nulo até a persistência. */
public final class Usuario {
    private final Long id;
    private final String apelido;
    private final LocalDateTime criadoEm;

    public Usuario(String apelido) {
        this(null, apelido, null);
    }

    public Usuario(Long id, String apelido, LocalDateTime criadoEm) {
        this.id = ValidacaoUtil.id(id);
        this.apelido = ValidacaoUtil.texto(apelido, "Apelido", 60, true);
        this.criadoEm = criadoEm;
    }

    public Long getId() { return id; }
    public String getApelido() { return apelido; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
