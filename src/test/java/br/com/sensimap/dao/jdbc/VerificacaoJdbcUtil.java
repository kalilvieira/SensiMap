package br.com.sensimap.dao.jdbc;

import java.sql.SQLException;

/** Garante que falhas SQL não vazem mensagens internas pelo contrato público. */
public final class VerificacaoJdbcUtil {
    public static void main(String[] args) {
        int verificacoes = 0;
        int[] codigos = {1, 2292, 2291, 1400, 2290, 12899, 1438, 20001, 1017, 99999};
        String[] esperados = {"CONFLITO", "CONFLITO", "VALIDACAO", "VALIDACAO", "VALIDACAO", "VALIDACAO", "VALIDACAO", "VALIDACAO", "PERSISTENCIA", "PERSISTENCIA"};
        for (int i = 0; i < codigos.length; i++) {
            var erro = JdbcUtil.traduzir(new SQLException("segredo_de_teste SQL interno", "teste", codigos[i]));
            if (!erro.getCodigo().equals(esperados[i]) || erro.getMessage().contains("segredo_de_teste") || erro.getCause() != null)
                throw new AssertionError("Erro de persistência inadequado.");
            verificacoes++;
        }
        if (!JdbcUtil.buscaLiteral("a!%_b").equals("%a!!!%!_b%")) throw new AssertionError("Escape de busca incorreto.");
        verificacoes++;
        System.out.println(verificacoes + " verificações de tratamento JDBC aprovadas (sem conexão).");
    }
}
