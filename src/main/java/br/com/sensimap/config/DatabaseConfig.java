package br.com.sensimap.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public final class DatabaseConfig {
    private final String url;
    private final String usuario;
    private final String senha;

    private DatabaseConfig(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    public static DatabaseConfig fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    // Recebe um mapa para permitir verificar a configuração sem alterar o ambiente.
    public static DatabaseConfig fromEnvironment(Map<String, String> ambiente) {
        String url = obrigatoria(ambiente, "DB_URL");
        String usuario = obrigatoria(ambiente, "DB_USER");
        String senha = obrigatoria(ambiente, "DB_PASSWORD");
        if (!url.startsWith("jdbc:oracle:thin:@")) {
            throw new IllegalArgumentException("DB_URL deve usar jdbc:oracle:thin:@//host:porta/service_name ou host:porta:SID.");
        }
        // Credenciais devem estar apenas nas variáveis próprias, nunca na URL.
        if (url.contains("?") || url.contains("#")) {
            throw new IllegalArgumentException("DB_URL deve conter apenas o endereço Oracle, sem usuário/senha, sem parâmetros ou credenciais.");
        }
        return new DatabaseConfig(url, usuario, senha);
    }

    private static String obrigatoria(Map<String, String> ambiente, String nome) {
        String valor = ambiente.get(nome);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Defina a variável de ambiente " + nome + ".");
        }
        return valor;
    }

    public Connection abrirConexao() throws SQLException {
        Properties propriedades = new Properties();
        propriedades.setProperty("user", usuario);
        propriedades.setProperty("password", senha);
        propriedades.setProperty("oracle.net.CONNECT_TIMEOUT", "5000");
        propriedades.setProperty("oracle.net.OUTBOUND_CONNECT_TIMEOUT", "5000");
        propriedades.setProperty("oracle.jdbc.ReadTimeout", "10000");
        return DriverManager.getConnection(url, propriedades);
    }
}
