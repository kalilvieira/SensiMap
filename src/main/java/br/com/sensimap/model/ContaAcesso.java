package br.com.sensimap.model;

import java.util.Objects;

/** Credencial reconstituída do banco; a senha original nunca é mantida neste objeto. */
public record ContaAcesso(long usuarioId, String apelido, String senhaHash, String senhaSalt,
                          int senhaIteracoes, PerfilAcesso perfil, boolean ativa) {
    public ContaAcesso {
        if (usuarioId <= 0) throw new IllegalArgumentException("Usuário da conta deve estar persistido.");
        apelido = Objects.requireNonNull(apelido).strip();
        senhaHash = Objects.requireNonNull(senhaHash);
        senhaSalt = Objects.requireNonNull(senhaSalt);
        if (senhaIteracoes < 100_000) throw new IllegalArgumentException("Quantidade de iterações de senha inválida.");
        perfil = Objects.requireNonNull(perfil);
    }
}
