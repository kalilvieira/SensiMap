package br.com.sensimap.app;

import br.com.sensimap.config.DatabaseConfig;
import br.com.sensimap.dao.PersistenciaException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        System.out.println("SensiMap — Web e console");
        System.out.println("Dados simulados para demonstração. Relatos subjetivos, sem medições clínicas.");
        if (args.length == 1 && args[0].equals("--web")) {
            try { br.com.sensimap.web.ApiServer.iniciar(); }
            catch (IllegalArgumentException e) { System.err.println("Configuração inválida: " + e.getMessage()); System.exit(2); }
            catch (java.io.IOException e) { System.err.println("Não foi possível iniciar o servidor HTTP. Confira o host, a porta e se outro processo já está usando o endereço."); System.exit(1); }
            return;
        }
        if (args.length == 1 && args[0].equals("--ajuda")) {
            ConsoleCrud.ajuda();
            return;
        }
        try {
            DatabaseConfig config = DatabaseConfig.fromEnvironment();
            try (Connection conexao = config.abrirConexao()) {
                if (args.length == 0) {
                    try (PreparedStatement comando = conexao.prepareStatement("SELECT 1 FROM DUAL");
                         ResultSet resultado = comando.executeQuery()) {
                        if (!resultado.next() || resultado.getInt(1) != 1) throw new SQLException("Diagnóstico inesperado.");
                        System.out.println("Conexão real com Oracle confirmada: SELECT 1 FROM DUAL retornou 1.");
                        System.out.println("Este diagnóstico não verifica tabelas nem executa CRUD. Consulte --ajuda.");
                    }
                    return;
                }
                conexao.setAutoCommit(false);
                try {
                    if (args.length == 1 && args[0].equals("demo-crud")) DemonstracaoCrud.executar(conexao);
                    else new ConsoleCrud(conexao).executar(args);
                    conexao.commit();
                    System.out.println("Operação concluída; transação confirmada no Oracle.");
                } catch (RuntimeException | SQLException e) {
                    try { conexao.rollback(); }
                    catch (SQLException falhaRollback) {
                        System.err.println("Não foi possível confirmar o rollback. Confira o estado do banco antes de repetir a operação.");
                    }
                    throw e;
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Entrada ou configuração inválida: " + e.getMessage());
            System.exit(2);
        } catch (PersistenciaException e) {
            System.err.println(e.getCodigo() + ": " + e.getMessage());
            System.exit(1);
        } catch (IllegalStateException e) {
            System.err.println("A demonstração não passou em uma verificação. Nenhum sucesso foi confirmado.");
            System.exit(1);
        } catch (SQLException e) {
            int codigo = e.getErrorCode();
            if (codigo == 1017 || codigo == 28000 || codigo == 28001) {
                System.err.println("Acesso recusado: confira DB_USER, DB_PASSWORD e a autenticação do Oracle.");
            } else if (codigo == 12514 || codigo == 12505) {
                System.err.println("Serviço Oracle não encontrado: confira service name ou SID em DB_URL.");
            } else {
                System.err.println("Falha JDBC: confira servidor, configuração e driver. Se houve tentativa de commit, confira o estado no banco antes de repetir.");
            }
            // Não imprimir a exceção: ela pode conter URL, usuário ou detalhes internos.
            System.exit(1);
        }
    }
}
