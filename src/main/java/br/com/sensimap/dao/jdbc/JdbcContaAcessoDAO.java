package br.com.sensimap.dao.jdbc;

import br.com.sensimap.dao.*;
import br.com.sensimap.model.*;
import java.sql.*;
import java.util.*;

/** A conexão pertence ao chamador; este DAO não faz commit nem fecha a conexão. */
public final class JdbcContaAcessoDAO implements ContaAcessoDAO {
    private final Connection conexao;

    public JdbcContaAcessoDAO(Connection conexao) { this.conexao=Objects.requireNonNull(conexao); }

    @Override
    public Optional<ContaAcesso> buscarPorApelido(String apelido) {
        String sql="""
                SELECT u.id, u.apelido, c.senha_hash, c.senha_salt, c.senha_iteracoes, c.perfil, c.ativo
                  FROM conta_acesso c JOIN usuario u ON u.id = c.usuario_id
                 WHERE LOWER(u.apelido) = LOWER(?)
                   AND c.perfil = 'USUARIO'
                """;
        try(PreparedStatement p=conexao.prepareStatement(sql)) {
            p.setString(1,apelido);
            try(ResultSet r=p.executeQuery()) {
                if(!r.next())return Optional.empty();
                return Optional.of(new ContaAcesso(r.getLong("id"),r.getString("apelido"),r.getString("senha_hash"),
                        r.getString("senha_salt"),r.getInt("senha_iteracoes"),PerfilAcesso.valueOf(r.getString("perfil")),r.getInt("ativo")==1));
            }
        }catch(SQLException e){throw JdbcUtil.traduzir(e);}
    }

    @Override
    public void inserir(ContaAcesso c) {
        String sql="INSERT INTO conta_acesso (usuario_id, senha_hash, senha_salt, senha_iteracoes, perfil, ativo) VALUES (?, ?, ?, ?, ?, ?)";
        try(PreparedStatement p=conexao.prepareStatement(sql)) {
            p.setLong(1,c.usuarioId());p.setString(2,c.senhaHash());p.setString(3,c.senhaSalt());
            p.setInt(4,c.senhaIteracoes());p.setString(5,c.perfil().name());p.setInt(6,c.ativa()?1:0);
            p.executeUpdate();
        }catch(SQLException e){throw JdbcUtil.traduzir(e);}
    }
}
