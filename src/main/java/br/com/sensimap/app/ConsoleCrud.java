package br.com.sensimap.app;

import br.com.sensimap.dao.jdbc.*;
import br.com.sensimap.model.*;
import br.com.sensimap.service.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Locale;

/** Comandos explícitos; cada execução bem-sucedida é confirmada pelo Main. */
public final class ConsoleCrud {
    private final UsuarioService usuarios;
    private final LocalService locais;
    private final EstimuloService estimulos;
    private final AvaliacaoService avaliacoes;
    private final LocalEstimuloService associacoes;
    private final IndicadorService indicadores = new IndicadorService();

    public ConsoleCrud(Connection conexao) {
        usuarios = new UsuarioService(new JdbcUsuarioDAO(conexao));
        locais = new LocalService(new JdbcLocalDAO(conexao));
        estimulos = new EstimuloService(new JdbcEstimuloDAO(conexao));
        avaliacoes = new AvaliacaoService(new JdbcAvaliacaoDAO(conexao), usuarios, locais);
        associacoes = new LocalEstimuloService(new JdbcLocalEstimuloDAO(conexao), locais, estimulos);
    }

    public static void ajuda() {
        System.out.println("""
                Uso: Main [comando argumentos] (sem comando: diagnóstico JDBC)
                  --web (interface em http://127.0.0.1:8080)
                  usuarios | usuario-ver ID | usuario-criar APELIDO
                  usuario-editar ID APELIDO | usuario-excluir ID
                  locais [NOME BAIRRO] | local-ver ID [MANHA|TARDE|NOITE]
                  local-criar NOME BAIRRO DESCRICAO | local-editar ID NOME BAIRRO DESCRICAO
                  local-excluir ID
                  estimulos | estimulo-ver ID | estimulo-criar NOME DESCRICAO
                  estimulo-editar ID NOME DESCRICAO | estimulo-excluir ID
                  associar LOCAL_ID ESTIMULO_ID | desassociar LOCAL_ID ESTIMULO_ID
                  avaliacoes | avaliacao-ver ID | avaliacao-excluir ID
                  avaliacao-criar USUARIO_ID LOCAL_ID DATA PERIODO RUIDO MOVIMENTO IMPREVISIBILIDADE COMENTARIO
                  avaliacao-editar ID USUARIO_ID LOCAL_ID DATA PERIODO RUIDO MOVIMENTO IMPREVISIBILIDADE COMENTARIO
                  demo-crud (cria, verifica e remove somente os próprios registros)
                DATA: AAAA-MM-DD; notas: 1 a 5; textos com espaços entre aspas.
                Exclusões são explícitas e imediatas após commit; usuário/local com avaliações é bloqueado.
                """);
    }

    public void executar(String[] a) {
        switch (a[0]) {
            case "usuarios" -> { tamanho(a, 1); usuarios.listar().forEach(this::usuario); }
            case "usuario-ver" -> { tamanho(a, 2); usuario(usuarios.buscar(id(a[1]))); }
            case "usuario-criar" -> { tamanho(a, 2); usuario(usuarios.cadastrar(a[1])); }
            case "usuario-editar" -> { tamanho(a, 3); usuarios.atualizar(id(a[1]), a[2]); }
            case "usuario-excluir" -> { tamanho(a, 2); usuarios.excluir(id(a[1])); }
            case "locais" -> {
                if (a.length != 1 && a.length != 3) throw new IllegalArgumentException("Use locais ou locais NOME BAIRRO.");
                var encontrados = a.length == 1 ? locais.listar() : locais.pesquisar(a[1], a[2]);
                if (encontrados.isEmpty()) System.out.println("Nenhum local encontrado.");
                encontrados.forEach(this::local);
            }
            case "local-ver" -> {
                if (a.length != 2 && a.length != 3) throw new IllegalArgumentException("Use local-ver ID [PERIODO].");
                long localId = id(a[1]);
                local(locais.buscar(localId));
                mostrarIndicadores(avaliacoes.indicadores(localId, a.length == 3 ? periodo(a[2]) : null));
                associacoes.listarPorLocal(localId).forEach(this::estimulo);
                // Relatos devem corresponder ao mesmo período apresentado nos indicadores.
                avaliacoes.listarPorLocal(localId).stream()
                        .filter(v -> a.length == 2 || v.getPeriodo() == periodo(a[2])).forEach(this::avaliacao);
            }
            case "local-criar" -> {
                tamanho(a, 4);
                local(locais.cadastrar(new Local(a[1], null, a[2], a[3], null, null, true)));
            }
            case "local-editar" -> {
                tamanho(a, 5);
                Local v = locais.buscar(id(a[1]));
                locais.atualizar(new Local(v.getId(), a[2], v.getEndereco(), a[3], a[4],
                        v.getLatitude(), v.getLongitude(), v.isDadosDemonstrativos(), v.getCriadoEm()));
            }
            case "local-excluir" -> { tamanho(a, 2); locais.excluir(id(a[1])); }
            case "estimulos" -> { tamanho(a, 1); estimulos.listar().forEach(this::estimulo); }
            case "estimulo-ver" -> { tamanho(a, 2); estimulo(estimulos.buscar(id(a[1]))); }
            case "estimulo-criar" -> { tamanho(a, 3); estimulo(estimulos.cadastrar(a[1], a[2])); }
            case "estimulo-editar" -> { tamanho(a, 4); estimulos.atualizar(id(a[1]), a[2], a[3]); }
            case "estimulo-excluir" -> { tamanho(a, 2); estimulos.excluir(id(a[1])); }
            case "associar" -> { tamanho(a, 3); associacoes.associar(id(a[1]), id(a[2])); }
            case "desassociar" -> { tamanho(a, 3); associacoes.desassociar(id(a[1]), id(a[2])); }
            case "avaliacoes" -> { tamanho(a, 1); avaliacoes.listar().forEach(this::avaliacao); }
            case "avaliacao-ver" -> { tamanho(a, 2); avaliacao(avaliacoes.buscar(id(a[1]))); }
            case "avaliacao-excluir" -> { tamanho(a, 2); avaliacoes.excluir(id(a[1])); }
            case "avaliacao-criar", "avaliacao-editar" -> {
                boolean editar = a[0].equals("avaliacao-editar");
                tamanho(a, editar ? 10 : 9);
                int p = editar ? 2 : 1;
                long usuarioId = id(a[p]), localId = id(a[p + 1]);
                LocalDate data = data(a[p + 2]);
                PeriodoDia periodo = periodo(a[p + 3]);
                int ruido = nota(a[p + 4]), movimento = nota(a[p + 5]), imprevisibilidade = nota(a[p + 6]);
                if (editar) avaliacoes.atualizar(id(a[1]), usuarioId, localId, data, periodo,
                        ruido, movimento, imprevisibilidade, a[p + 7]);
                else avaliacao(avaliacoes.cadastrar(usuarioId, localId, data, periodo,
                        ruido, movimento, imprevisibilidade, a[p + 7]));
            }
            default -> throw new IllegalArgumentException("Comando desconhecido. Consulte --ajuda.");
        }
    }

    private void mostrarIndicadores(IndicadoresSensoriais i) {
        System.out.println("Período: " + (i.periodo() == null ? "Todos" : i.periodo())
                + " | n=" + i.quantidade() + " | última visita: " + (i.ultimaVisita() == null ? "—" : i.ultimaVisita()));
        if (i.quantidade() == 0) System.out.println("Ainda não avaliado. Falta de registros não significa ausência de estímulos.");
        System.out.println("Ruído percebido: " + indicadores.classificar(i.ruido(), i.quantidade()).getDescricao()
                + " | média: " + indicadores.formatarMedia(i.ruido()));
        System.out.println("Concentração de pessoas percebida: " + indicadores.classificar(i.movimento(), i.quantidade()).getDescricao()
                + " | média: " + indicadores.formatarMedia(i.movimento()));
        System.out.println("Imprevisibilidade percebida: " + indicadores.classificar(i.imprevisibilidade(), i.quantidade()).getDescricao()
                + " | média: " + indicadores.formatarMedia(i.imprevisibilidade()));
    }

    private void usuario(Usuario v) { System.out.println("Usuário " + v.getId() + ": " + v.getApelido()); }
    private void local(Local v) { System.out.println("Local " + v.getId() + ": " + v.getNome() + " | " + v.getBairro()); }
    private void estimulo(Estimulo v) { System.out.println("Estímulo " + v.getId() + ": " + v.getNome()); }
    private void avaliacao(Avaliacao v) {
        System.out.println("Avaliação " + v.getId() + " | usuário=" + v.getUsuario().getId() + " | local=" + v.getLocal().getId()
                + " | " + v.getDataVisita() + " | " + v.getPeriodo() + " | notas=" + v.getRuido() + "/" + v.getMovimento()
                + "/" + v.getImprevisibilidade() + " | " + (v.getComentario() == null ? "" : v.getComentario()));
    }
    private static void tamanho(String[] a, int esperado) {
        if (a.length != esperado) throw new IllegalArgumentException("Quantidade de argumentos inválida. Consulte --ajuda.");
    }
    private static long id(String valor) {
        try { long id = Long.parseLong(valor); if (id > 0) return id; }
        catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("ID deve ser um inteiro positivo.");
    }
    private static int nota(String valor) {
        try { return br.com.sensimap.util.ValidacaoUtil.nota(Integer.parseInt(valor), "Nota"); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Nota deve ser um inteiro de 1 a 5."); }
    }
    private static PeriodoDia periodo(String valor) {
        try { return PeriodoDia.valueOf(valor.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Período deve ser MANHA, TARDE ou NOITE."); }
    }
    private static LocalDate data(String valor) {
        try { return LocalDate.parse(valor); }
        catch (java.time.format.DateTimeParseException e) { throw new IllegalArgumentException("Data deve ser válida, no formato AAAA-MM-DD."); }
    }
}
