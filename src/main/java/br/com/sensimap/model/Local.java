package br.com.sensimap.model;

import br.com.sensimap.util.ValidacaoUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/** Objeto imutável; novos registros têm ID nulo até a persistência. */
public final class Local {
    private final Long id;
    private final String nome;
    private final String endereco;
    private final String bairro;
    private final String descricao;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final boolean dadosDemonstrativos;
    private final LocalDateTime criadoEm;

    public Local(String nome, String endereco, String bairro, String descricao,
                 BigDecimal latitude, BigDecimal longitude, boolean dadosDemonstrativos) {
        this(null, nome, endereco, bairro, descricao, latitude, longitude, dadosDemonstrativos, null);
    }

    public Local(Long id, String nome, String endereco, String bairro, String descricao, BigDecimal latitude, BigDecimal longitude, boolean dadosDemonstrativos, LocalDateTime criadoEm) {
        this.id = ValidacaoUtil.id(id);
        this.nome = ValidacaoUtil.texto(nome, "Nome", 120, true);
        this.endereco = ValidacaoUtil.texto(endereco, "Endereço", 180, false);
        this.bairro = ValidacaoUtil.texto(bairro, "Bairro", 80, true);
        this.descricao = ValidacaoUtil.texto(descricao, "Descrição", 500, false);
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("Preencha latitude e longitude juntas ou deixe ambas vazias.");
        }
        this.latitude = coordenada(latitude, 90, "Latitude");
        this.longitude = coordenada(longitude, 180, "Longitude");
        this.dadosDemonstrativos = dadosDemonstrativos;
        this.criadoEm = criadoEm;
    }

    private static BigDecimal coordenada(BigDecimal valor, int limite, String campo) {
        if (valor == null) {
            return null;
        }
        if (valor.abs().compareTo(BigDecimal.valueOf(limite)) > 0) {
            throw new IllegalArgumentException(campo + " fora da faixa permitida.");
        }
        try {
            return valor.setScale(6, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(campo + " deve ter no máximo 6 casas decimais.");
        }
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getBairro() { return bairro; }
    public String getDescricao() { return descricao; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public boolean isDadosDemonstrativos() { return dadosDemonstrativos; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
