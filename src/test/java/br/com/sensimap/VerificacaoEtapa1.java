package br.com.sensimap;

import br.com.sensimap.config.DatabaseConfig;
import br.com.sensimap.model.*;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** Verificações sem framework; executar explicitamente pelo comando do README. */
public final class VerificacaoEtapa1 {
    private static int verificacoes;
    private static final Usuario USUARIO = new Usuario(1L, "demo_teste", LocalDateTime.now());
    private static final Local LOCAL = new Local(1L, "Local fictício", null, "Bairro fictício",
            null, null, null, true, LocalDateTime.now());

    public static void main(String[] args) throws Exception {
        verificar(new Usuario(" demo ").getApelido().equals("demo"), "Normalização do apelido");
        verificar(new Usuario("demo").getId() == null, "ID ainda não persistido");
        verificar(new Estimulo("Obras", " ").getDescricao() == null, "Texto opcional vazio");
        verificar(new Usuario("a".repeat(60)).getApelido().length() == 60, "Apelido no limite");
        rejeitar(() -> new Usuario(null));
        rejeitar(() -> new Usuario(" "));
        rejeitar(() -> new Usuario("a".repeat(61)));
        rejeitar(() -> new Usuario(0L, "demo", null));
        rejeitar(() -> new Estimulo("Teste", "a".repeat(301)));
        rejeitar(() -> local(null, BigDecimal.ZERO));
        rejeitar(() -> local(BigDecimal.ZERO, null));
        rejeitar(() -> local(new BigDecimal("90.000001"), BigDecimal.ZERO));
        rejeitar(() -> local(new BigDecimal("-90.000001"), BigDecimal.ZERO));
        rejeitar(() -> local(BigDecimal.ZERO, new BigDecimal("180.000001")));
        rejeitar(() -> local(BigDecimal.ZERO, new BigDecimal("-180.000001")));
        rejeitar(() -> local(new BigDecimal("0.0000001"), BigDecimal.ZERO));
        // Coordenadas abaixo são somente limites matemáticos de teste, não locais reais.
        verificar(local(new BigDecimal("-90"), new BigDecimal("180")).isDadosDemonstrativos(), "Limites geográficos");
        verificar(local(null, null).getLatitude() == null, "Coordenadas ausentes");
        verificar(!new Local("Local colaborativo", null, "Bairro", null, null, null, false).isDadosDemonstrativos(), "Local colaborativo identificado");
        verificar(avaliacao(1, 5, 3).getMovimento() == 5, "Notas válidas independentes");
        for (int invalida : new int[] {0, 6}) {
            rejeitar(() -> avaliacao(invalida, 3, 3));
            rejeitar(() -> avaliacao(3, invalida, 3));
            rejeitar(() -> avaliacao(3, 3, invalida));
        }
        rejeitar(() -> new Avaliacao(USUARIO, LOCAL, LocalDate.now().plusDays(1), PeriodoDia.MANHA, 1, 1, 1, null));
        rejeitar(() -> new Avaliacao(USUARIO, LOCAL, null, PeriodoDia.MANHA, 1, 1, 1, null));
        rejeitar(() -> new Avaliacao(USUARIO, LOCAL, LocalDate.now(), null, 1, 1, 1, null));
        rejeitar(() -> PeriodoDia.valueOf("MADRUGADA"));
        rejeitar(() -> new Avaliacao(new Usuario("novo"), LOCAL, LocalDate.now(), PeriodoDia.MANHA, 1, 1, 1, null));
        rejeitar(() -> new Avaliacao(USUARIO, LOCAL, LocalDate.now(), PeriodoDia.MANHA, 1, 1, 1, "a".repeat(501)));
        Estimulo estimulo = new Estimulo(1L, "Obras", null);
        verificar(new LocalEstimulo(LOCAL, estimulo).getEstimulo() == estimulo, "Associação entre objetos");
        rejeitar(() -> new LocalEstimulo(LOCAL, new Estimulo("novo", null)));

        Map<String, String> ambiente = Map.of("DB_URL", "jdbc:oracle:thin:@//localhost:1521/teste",
                "DB_USER", "usuario_teste", "DB_PASSWORD", "valor_apenas_de_teste");
        verificar(DatabaseConfig.fromEnvironment(ambiente) != null, "Configuração por service name");
        Map<String, String> sid = new HashMap<>(ambiente);
        sid.put("DB_URL", "jdbc:oracle:thin:@localhost:1521:TESTE");
        verificar(DatabaseConfig.fromEnvironment(sid) != null, "Configuração por SID");
        for (String variavel : ambiente.keySet()) {
            Map<String, String> incompleto = new HashMap<>(ambiente);
            incompleto.remove(variavel);
            rejeitar(() -> DatabaseConfig.fromEnvironment(incompleto));
        }
        for (String url : new String[] {"jdbc:postgresql://localhost/teste",
                "jdbc:oracle:thin:usuario/senha@localhost:1521:teste",
                "jdbc:oracle:thin:@//localhost:1521/teste?password=teste"}) {
            Map<String, String> invalido = new HashMap<>(ambiente);
            invalido.put("DB_URL", url);
            rejeitar(() -> DatabaseConfig.fromEnvironment(invalido));
        }
        verificar(DriverManager.getDriver(ambiente.get("DB_URL")).getClass().getName().startsWith("oracle.jdbc."),
                "Driver Oracle disponível; este teste não abre conexão");
        System.out.println(verificacoes + " verificações aprovadas (domínio, configuração e carregamento do driver; sem conexão ao banco).");
    }

    private static Local local(BigDecimal latitude, BigDecimal longitude) {
        return new Local("Local fictício", null, "Bairro fictício", null, latitude, longitude, true);
    }

    private static Avaliacao avaliacao(int ruido, int movimento, int imprevisibilidade) {
        return new Avaliacao(USUARIO, LOCAL, LocalDate.now(), PeriodoDia.MANHA, ruido, movimento, imprevisibilidade, null);
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
        verificacoes++;
    }

    private static void rejeitar(Runnable acao) {
        try {
            acao.run();
        } catch (IllegalArgumentException e) {
            verificacoes++;
            return;
        }
        throw new AssertionError("A entrada inválida deveria ser recusada.");
    }
}
