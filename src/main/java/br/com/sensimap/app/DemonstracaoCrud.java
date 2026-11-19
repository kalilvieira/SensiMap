package br.com.sensimap.app;

import br.com.sensimap.dao.*;
import br.com.sensimap.dao.jdbc.*;
import br.com.sensimap.model.*;
import br.com.sensimap.service.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.UUID;

/** Executa em uma transação controlada pelo Main, sem alterar a carga inicial. */
public final class DemonstracaoCrud {
    private DemonstracaoCrud() { }

    public static void executar(Connection c) {
        var ud = new JdbcUsuarioDAO(c);
        var ld = new JdbcLocalDAO(c);
        var ed = new JdbcEstimuloDAO(c);
        var ad = new JdbcAvaliacaoDAO(c);
        var usuarios = new UsuarioService(ud);
        var locais = new LocalService(ld);
        var estimulos = new EstimuloService(ed);
        var avaliacoes = new AvaliacaoService(ad, usuarios, locais);
        var associacoes = new LocalEstimuloService(new JdbcLocalEstimuloDAO(c), locais, estimulos);
        String sufixo = UUID.randomUUID().toString();
        Usuario u = usuarios.cadastrar("demo_" + sufixo);
        Local l = locais.cadastrar(new Local("Local fictício " + sufixo, null, "Bairro fictício", null, null, null, true));
        Local l2 = locais.cadastrar(new Local("Outro local fictício " + sufixo, null, "Bairro fictício", null, null, null, true));
        Estimulo e = estimulos.cadastrar("Teste " + sufixo, "Estímulo fictício");
        Estimulo e2 = estimulos.cadastrar("Outro teste " + sufixo, null);
        verificar(u.getId() != null && l.getId() != null && e.getId() != null, "IDs gerados pelo Oracle");
        verificar(usuarios.listar().stream().anyMatch(v -> v.getId().equals(u.getId())), "Listagem de usuários");
        verificar(locais.listar().stream().anyMatch(v -> v.getId().equals(l.getId())), "Listagem de locais");
        verificar(estimulos.listar().stream().anyMatch(v -> v.getId().equals(e.getId())), "Listagem de estímulos");
        conflito(() -> usuarios.cadastrar(u.getApelido()));
        conflito(() -> estimulos.cadastrar(e.getNome(), null));
        usuarios.atualizar(u.getId(), "editado_" + sufixo);
        verificar(usuarios.buscar(u.getId()).getApelido().equals("editado_" + sufixo), "Atualização de usuário");
        estimulos.atualizar(e.getId(), e.getNome(), "Descrição alterada");
        verificar(estimulos.buscar(e.getId()).getDescricao().equals("Descrição alterada"), "Atualização de estímulo");
        locais.atualizar(new Local(l.getId(), "Editado " + sufixo, null, l.getBairro(), "Descrição alterada",
                null, null, true, l.getCriadoEm()));
        verificar(locais.buscar(l.getId()).getDescricao().equals("Descrição alterada"), "Atualização de local");
        verificar(locais.pesquisar(sufixo, "fictício").size() == 2, "Busca por nome e bairro");
        verificar(locais.pesquisar("' OR 1=1 -- " + sufixo, "").isEmpty(), "Busca trata entrada como texto");
        verificar(locais.pesquisar("%" + sufixo, "").isEmpty(), "Curinga é tratado literalmente");
        associacoes.associar(l.getId(), e.getId());
        associacoes.associar(l.getId(), e2.getId());
        associacoes.associar(l2.getId(), e.getId());
        verificar(associacoes.listarPorLocal(l.getId()).size() == 2
                && associacoes.listarPorLocal(l2.getId()).size() == 1, "Relacionamento N:N");
        conflito(() -> associacoes.associar(l.getId(), e.getId()));
        verificar(avaliacoes.indicadores(l.getId(), null).quantidade() == 0, "Local sem avaliações");
        LocalDate dia = LocalDate.now().minusDays(1);
        Avaliacao a1 = avaliacoes.cadastrar(u.getId(), l.getId(), dia, PeriodoDia.MANHA, 1, 3, 5, "Relato fictício");
        Avaliacao a2 = avaliacoes.cadastrar(u.getId(), l.getId(), dia, PeriodoDia.TARDE, 2, 3, 4, null);
        verificar(!avaliacoes.indicadores(l.getId(), null).dadosSuficientes(), "Duas avaliações: dados insuficientes");
        Avaliacao a3 = avaliacoes.cadastrar(u.getId(), l.getId(), dia, PeriodoDia.NOITE, 3, 3, 3, null);
        verificar(avaliacoes.listar().stream().anyMatch(v -> v.getId().equals(a1.getId())), "Listagem de avaliações");
        verificar(avaliacoes.listarPorLocal(l.getId()).size() == 3, "Listagem por local");
        conflito(() -> avaliacoes.cadastrar(u.getId(), l.getId(), dia, PeriodoDia.MANHA, 1, 1, 1, null));
        conflito(() -> usuarios.excluir(u.getId()));
        conflito(() -> locais.excluir(l.getId()));
        IndicadoresSensoriais i = avaliacoes.indicadores(l.getId(), null);
        verificar(i.quantidade() == 3 && i.ruido().compareTo(new BigDecimal("2")) == 0
                && i.movimento().compareTo(new BigDecimal("3")) == 0
                && i.imprevisibilidade().compareTo(new BigDecimal("4")) == 0, "AVG e COUNT dos três relatos conhecidos");
        var classificador = new IndicadorService();
        verificar(classificador.classificar(i.ruido(), i.quantidade()) == ClassificacaoSensorial.BAIXA
                && classificador.classificar(i.movimento(), i.quantidade()) == ClassificacaoSensorial.MODERADA
                && classificador.classificar(i.imprevisibilidade(), i.quantidade()) == ClassificacaoSensorial.ALTA,
                "Classificações independentes");
        verificar(avaliacoes.indicadores(l.getId(), PeriodoDia.MANHA).quantidade() == 1, "Filtro por período");
        avaliacoes.atualizar(a1.getId(), u.getId(), l.getId(), dia, PeriodoDia.MANHA, 5, 1, 2, "Editado");
        verificar(avaliacoes.buscar(a1.getId()).getRuido() == 5, "Atualização de avaliação");
        verificar(avaliacoes.indicadores(l.getId(), PeriodoDia.MANHA).ruido().compareTo(new BigDecimal("5")) == 0,
                "Indicadores refletem atualização");
        avaliacoes.excluir(a1.getId());
        avaliacoes.excluir(a2.getId());
        avaliacoes.excluir(a3.getId());
        verificar(ad.buscarPorId(a1.getId()).isEmpty() && avaliacoes.listarPorLocal(l.getId()).isEmpty(), "Exclusão de avaliações");
        associacoes.desassociar(l.getId(), e2.getId());
        verificar(associacoes.listarPorLocal(l.getId()).size() == 1, "Desassociação N:N");
        locais.excluir(l2.getId());
        estimulos.excluir(e.getId());
        verificar(associacoes.listarPorLocal(l.getId()).isEmpty(), "Cascade remove associações, preservando local");
        locais.excluir(l.getId());
        estimulos.excluir(e2.getId());
        usuarios.excluir(u.getId());
        verificar(ld.buscarPorId(l.getId()).isEmpty() && ld.buscarPorId(l2.getId()).isEmpty()
                && ed.buscarPorId(e.getId()).isEmpty() && ed.buscarPorId(e2.getId()).isEmpty()
                && ud.buscarPorId(u.getId()).isEmpty(), "Registros próprios removidos");
        System.out.println("Demonstração JDBC verificada; aguardando commit. Persistência após reinício exige os comandos individuais.");
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) throw new IllegalStateException("Falha na demonstração: " + descricao);
        System.out.println("Verificado: " + descricao);
    }
    private static void conflito(Runnable acao) {
        try { acao.run(); }
        catch (PersistenciaException e) {
            verificar(e.getCodigo().equals("CONFLITO"), "Conflito esperado tratado");
            return;
        }
        throw new IllegalStateException("Conflito esperado não foi recusado.");
    }
}
