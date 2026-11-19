package br.com.sensimap.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Agregação bruta do banco. Arredondar somente ao apresentar, não ao classificar. */
public record IndicadoresSensoriais(long quantidade, BigDecimal ruido, BigDecimal movimento,
        BigDecimal imprevisibilidade, PeriodoDia periodo, LocalDate ultimaVisita) {
    public IndicadoresSensoriais {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade inválida.");
        if (quantidade == 0) {
            if (ruido != null || movimento != null || imprevisibilidade != null || ultimaVisita != null)
                throw new IllegalArgumentException("Sem avaliações, médias e data devem ser nulas.");
        } else {
            validarMedia(ruido);
            validarMedia(movimento);
            validarMedia(imprevisibilidade);
            if (ultimaVisita == null) throw new IllegalArgumentException("Informe a última visita.");
        }
    }

    private static void validarMedia(BigDecimal media) {
        if (media == null || media.compareTo(BigDecimal.ONE) < 0 || media.compareTo(BigDecimal.valueOf(5)) > 0)
            throw new IllegalArgumentException("Média deve estar entre 1 e 5.");
    }

    public boolean dadosSuficientes() { return quantidade >= 3; }
}
