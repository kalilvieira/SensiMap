package br.com.sensimap.model;

import br.com.sensimap.util.ValidacaoUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Objeto imutável; novos registros têm ID nulo até a persistência. */
public final class Avaliacao {
    private final Long id;
    private final Usuario usuario;
    private final Local local;
    private final LocalDate dataVisita;
    private final PeriodoDia periodo;
    private final int ruido;
    private final int movimento;
    private final int imprevisibilidade;
    private final String comentario;
    private final LocalDateTime criadoEm;

    public Avaliacao(Usuario usuario, Local local, LocalDate dataVisita, PeriodoDia periodo,
                     int ruido, int movimento, int imprevisibilidade, String comentario) {
        this(null, usuario, local, dataVisita, periodo, ruido, movimento, imprevisibilidade, comentario, null);
    }

    public Avaliacao(Long id, Usuario usuario, Local local, LocalDate dataVisita, PeriodoDia periodo, int ruido, int movimento, int imprevisibilidade, String comentario, LocalDateTime criadoEm) {
        this.id = ValidacaoUtil.id(id);
        if (usuario == null || usuario.getId() == null || local == null || local.getId() == null) {
            throw new IllegalArgumentException("Informe usuário e local com ID persistido.");
        }
        if (dataVisita == null || dataVisita.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data da visita é obrigatória e não pode ser futura.");
        }
        if (periodo == null) {
            throw new IllegalArgumentException("Período é obrigatório.");
        }
        this.usuario = usuario;
        this.local = local;
        this.dataVisita = dataVisita;
        this.periodo = periodo;
        this.ruido = ValidacaoUtil.nota(ruido, "Ruído percebido");
        this.movimento = ValidacaoUtil.nota(movimento, "Concentração de pessoas percebida");
        this.imprevisibilidade = ValidacaoUtil.nota(imprevisibilidade, "Imprevisibilidade percebida");
        this.comentario = ValidacaoUtil.texto(comentario, "Comentário", 500, false);
        this.criadoEm = criadoEm;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Local getLocal() { return local; }
    public LocalDate getDataVisita() { return dataVisita; }
    public PeriodoDia getPeriodo() { return periodo; }
    public int getRuido() { return ruido; }
    public int getMovimento() { return movimento; }
    public int getImprevisibilidade() { return imprevisibilidade; }
    public String getComentario() { return comentario; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
