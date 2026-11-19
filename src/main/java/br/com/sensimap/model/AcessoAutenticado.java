package br.com.sensimap.model;

public record AcessoAutenticado(long usuarioId, String apelido, PerfilAcesso perfil) {
    public AcessoAutenticado(ContaAcesso conta) {
        this(conta.usuarioId(), conta.apelido(), conta.perfil());
    }
}
