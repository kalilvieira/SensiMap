package br.com.sensimap.model;

import br.com.sensimap.util.ValidacaoUtil;

/** Objeto imutável; novos registros têm ID nulo até a persistência. */
public final class Estimulo {
    private final Long id;
    private final String nome;
    private final String descricao;

    public Estimulo(String nome, String descricao) {
        this(null, nome, descricao);
    }

    public Estimulo(Long id, String nome, String descricao) {
        this.id = ValidacaoUtil.id(id);
        this.nome = ValidacaoUtil.texto(nome, "Nome", 80, true);
        this.descricao = ValidacaoUtil.texto(descricao, "Descrição", 300, false);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
}
