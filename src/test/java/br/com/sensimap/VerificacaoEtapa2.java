package br.com.sensimap;

import br.com.sensimap.model.*;
import br.com.sensimap.service.IndicadorService;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Regras de indicadores; não simula nem comprova o funcionamento do Oracle. */
public final class VerificacaoEtapa2 {
    private static int verificacoes;
    private static final IndicadorService SERVICE = new IndicadorService();

    public static void main(String[] args) {
        for (long n : new long[] {0, 1, 2}) {
            verificar(SERVICE.classificar(null, n) == ClassificacaoSensorial.DADOS_INSUFICIENTES);
            verificar(SERVICE.classificar(new BigDecimal("5"), n) == ClassificacaoSensorial.DADOS_INSUFICIENTES);
        }
        for (long n : new long[] {3, 20}) {
            classificar("1", n, ClassificacaoSensorial.BAIXA);
            classificar("2.00", n, ClassificacaoSensorial.BAIXA);
            classificar("2.000001", n, ClassificacaoSensorial.MODERADA);
            classificar("3.50", n, ClassificacaoSensorial.MODERADA);
            classificar("3.500001", n, ClassificacaoSensorial.ALTA);
            classificar("5", n, ClassificacaoSensorial.ALTA);
        }
        verificar(SERVICE.formatarMedia(new BigDecimal("2.005")).equals("2,01"));
        verificar(SERVICE.formatarMedia(new BigDecimal("3.500001")).equals("3,50"));
        verificar(SERVICE.formatarMedia(new BigDecimal("2")).equals("2,00"));
        verificar(SERVICE.formatarMedia(null).equals("—"));
        rejeitar(() -> SERVICE.classificar(BigDecimal.ZERO, 3));
        rejeitar(() -> SERVICE.classificar(new BigDecimal("5.01"), 3));
        rejeitar(() -> SERVICE.classificar(null, 3));
        rejeitar(() -> SERVICE.classificar(BigDecimal.ONE, -1));
        var vazio = new IndicadoresSensoriais(0, null, null, null, PeriodoDia.NOITE, null);
        verificar(!vazio.dadosSuficientes() && vazio.periodo() == PeriodoDia.NOITE);
        var dois = new IndicadoresSensoriais(2, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, null, LocalDate.now());
        verificar(!dois.dadosSuficientes());
        var tres = new IndicadoresSensoriais(3, new BigDecimal("2"), new BigDecimal("3.5"), new BigDecimal("4"), null, LocalDate.now());
        verificar(tres.dadosSuficientes());
        verificar(SERVICE.classificar(tres.ruido(), tres.quantidade()) == ClassificacaoSensorial.BAIXA);
        verificar(SERVICE.classificar(tres.movimento(), tres.quantidade()) == ClassificacaoSensorial.MODERADA);
        verificar(SERVICE.classificar(tres.imprevisibilidade(), tres.quantidade()) == ClassificacaoSensorial.ALTA);
        rejeitar(() -> new IndicadoresSensoriais(-1, null, null, null, null, null));
        rejeitar(() -> new IndicadoresSensoriais(0, BigDecimal.ONE, null, null, null, null));
        rejeitar(() -> new IndicadoresSensoriais(1, null, BigDecimal.ONE, BigDecimal.ONE, null, LocalDate.now()));
        rejeitar(() -> new IndicadoresSensoriais(1, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, null, LocalDate.now()));
        rejeitar(() -> new IndicadoresSensoriais(1, BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("6"), null, LocalDate.now()));
        rejeitar(() -> new IndicadoresSensoriais(1, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, null, null));
        System.out.println(verificacoes + " verificações de indicadores aprovadas (sem banco).");
    }
    private static void classificar(String media, long n, ClassificacaoSensorial esperado) {
        verificar(SERVICE.classificar(new BigDecimal(media), n) == esperado);
    }
    private static void verificar(boolean condicao) {
        if (!condicao) throw new AssertionError("Regra de indicador divergente.");
        verificacoes++;
    }
    private static void rejeitar(Runnable acao) {
        try { acao.run(); }
        catch (IllegalArgumentException e) { verificacoes++; return; }
        throw new AssertionError("Entrada inválida não recusada.");
    }
}
