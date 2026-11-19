package br.com.sensimap.model;

public enum ClassificacaoSensorial {
    BAIXA("Baixa"), MODERADA("Moderada"), ALTA("Alta"),
    DADOS_INSUFICIENTES("Dados insuficientes");

    private final String descricao;
    ClassificacaoSensorial(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
