package br.com.sensimap.service;

import br.com.sensimap.model.ClassificacaoSensorial;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class IndicadorService {
    public ClassificacaoSensorial classificar(BigDecimal media, long quantidade) {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade inválida.");
        if (quantidade < 3) return ClassificacaoSensorial.DADOS_INSUFICIENTES;
        if (media == null || media.compareTo(BigDecimal.ONE) < 0 || media.compareTo(BigDecimal.valueOf(5)) > 0)
            throw new IllegalArgumentException("Média deve estar entre 1 e 5.");
        if (media.compareTo(new BigDecimal("2.00")) <= 0) return ClassificacaoSensorial.BAIXA;
        if (media.compareTo(new BigDecimal("3.50")) <= 0) return ClassificacaoSensorial.MODERADA;
        return ClassificacaoSensorial.ALTA;
    }

    public String formatarMedia(BigDecimal media) {
        return media == null ? "—" : media.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}
