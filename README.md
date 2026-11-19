# SensiMap — Global Solution 2026/2

MVP acadêmico da FIAP Global Solution 2026/2 para consulta de percepções sensoriais urbanas. Esta entrega implementa domínio, scripts Oracle, CRUD JDBC, associações N:N, serviços, indicadores, comandos de console e uma interface web responsiva com áreas pública e de usuário. O servidor HTTP usa apenas a JDK. Compilação e verificações locais estão registradas em `docs/testes.md`.

**Dados simulados para demonstração.** Os nomes e endereços dos dez locais da carga são reais e foram conferidos em fontes oficiais. As avaliações e associações de estímulos continuam fictícias e subjetivas: não são medições de decibéis, contagens de pessoas, avaliações clínicas ou garantias de segurança. Não há geolocalização do usuário ou dados médicos. Senhas de acesso são armazenadas somente como PBKDF2 com salt, nunca em texto puro.

## Banco de dados: Oracle da FIAP

A entrega utiliza **Oracle no servidor da FIAP**, acessado pelo Java via JDBC. Oracle é uma das opções de banco relacional aceitas pelo enunciado da Global Solution.

- Java 17 e JDBC continuam obrigatórios; Maven gerencia somente build e driver.
- Driver: `com.oracle.database.jdbc:ojdbc11:23.26.4.0.0`, disponível no [catálogo oficial Oracle JDBC](https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html).
- SQL destinado a Oracle 19c ou superior, sem dependência de recursos exclusivos do 23ai. A versão do servidor FIAP ainda precisa ser confirmada.
- `BIGINT` foi mapeado para `NUMBER(19)`, textos para `VARCHAR2(... CHAR)` e booleano para `NUMBER(1)` com valores 0/1. Java conserva `Long`, `String` e `boolean`.
- IDs usam `GENERATED ALWAYS AS IDENTITY`. Não preencher IDs manualmente na carga.
- O diagnóstico usa `SELECT 1 FROM DUAL`.
- Como [Oracle não permite SYSDATE em CHECK](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/SYSDATE.html), um gatilho recusa datas futuras; a entidade Java também valida a data. `DATE` é limitado a datas sem horário pelo schema.

Não é necessário instalar Oracle localmente ou criar um banco na FIAP. Utilize o usuário/schema acadêmico e os parâmetros de conexão fornecidos pela instituição. Não foram presumidos endereço, SID, service name ou credenciais reais.

## Estrutura implementada

```text
pom.xml                            build Java 17 e driver Oracle
.env.example                       modelo sem senha real
database/01_schema.sql              seis tabelas, índices e gatilho
database/02_seed.sql                locais reais e relatos simulados
database/03_consultas_relatorios.sql JOIN, AVG/COUNT e locais sem avaliações
database/04_migracao_autenticacao.sql atualização segura de schema anterior
database/05_remover_acesso_administrativo.sql desativa credenciais administrativas legadas
database/06_migracao_locais_reais.sql atualiza a carga anterior preservando IDs e relatos
database/verificar_etapa1.sql       verificação da carga e restrições
src/main/java/br/com/sensimap/
  app/                             Main, ConsoleCrud, DemonstracaoCrud
  config/DatabaseConfig.java        ambiente, conexão e timeouts
  model/                           entidades, enumerações e IndicadoresSensoriais
  dao/                             contratos CRUD/N:N e PersistenciaException
  dao/jdbc/                        DAOs Oracle, mapeamento e erros controlados
  service/                         operações de negócio e classificação
  util/                            validação e serialização JSON de saída
  web/                             servidor HTTP, rotas e parâmetros de formulário
src/main/resources/web/            HTML, CSS e JavaScript vanilla
src/test/java/br/com/sensimap/      verificadores executáveis sem framework
scripts/iniciar-web.sh             compila e inicia em localhost:8080
scripts/verificar-local.sh         build/verificações sem Maven (driver já obtido)
scripts/verificar-web.mjs          regras JavaScript em armazenamento isolado
scripts/verificar-http.py          smoke test HTTP somente de leitura
docs/diagrama_er.mmd                modelo relacional Oracle
docs/diagrama_classes.mmd           entidades, DAOs, serviços e console
Diagrama_ER_SensiMap.pdf            diagrama ER final em A3 horizontal
Diagrama_Classes_SensiMap.pdf       diagrama UML final em A2 horizontal
docs/testes.md                     resultados reais e roteiro de integração
integrantes.txt                    campos a preencher pelo grupo
```

Fluxo de console: `Main → ConsoleCrud → Service → DAO JDBC → Oracle`. Fluxo web com Oracle configurado: `HTML/JS → ApiServer → ApiHandler → Service → DAO JDBC → Oracle`. Cada requisição abre sua própria conexão, controla commit/rollback e a fecha ao terminar. As entidades são imutáveis e validam os campos no construtor. Os serviços consultam a existência das referências; FKs e restrições protegem também contra alterações concorrentes.

A interface `CrudDAO<T>` define listar, buscar por ID, inserir, atualizar e excluir. Inserções devolvem o objeto reconstituído com ID e data gerados pelo banco. A recuperação pede explicitamente a coluna `ID`, pois o sinalizador genérico de chaves no Oracle retorna `ROWID` ([documentação Oracle JDBC](https://docs.oracle.com/en/database/oracle/oracle-database/19/jjdbc/JDBC-standards-support.html)). Buscas e valores usam parâmetros; nenhum texto do usuário é concatenado ao SQL. `%`, `_` e `!` são escapados na busca por nome/bairro, que trata a entrada como texto literal e ignora maiúsculas/minúsculas.

## Preparação do ambiente

1. Instale um **JDK 17** e **Maven 3.9.x**, por exemplo pelas distribuições [Eclipse Temurin](https://adoptium.net/temurin/releases/?version=17) e [Apache Maven](https://maven.apache.org/install.html). Configure `JAVA_HOME` para o JDK 17 e inclua seus executáveis no `PATH`.
2. Tenha Oracle SQL Developer, SQLcl ou SQL*Plus para aplicar os scripts no schema da FIAP. Use os dados de acesso da instituição e a VPN/rede acadêmica caso ela seja exigida.
3. Confira no terminal, a partir da raiz do projeto:

```sh
java -version
javac -version
mvn -version
```

Os comandos devem indicar Java 17. O `pom.xml` usa `release=17` para limitar linguagem, APIs e bytecode à versão exigida, conforme a [documentação Maven](https://maven.apache.org/guides/mini/guide-java-target.html). A primeira compilação precisa de acesso ao Maven Central.

## Configurar o acesso à FIAP

Copie o modelo e edite **localmente** o arquivo `.env`:

```sh
cp .env.example .env
```

| Variável | Como preencher |
|---|---|
| `DB_URL` | Service name: `jdbc:oracle:thin:@//HOST_FIAP:1521/SERVICE_NAME_FIAP` |
| `DB_URL` (alternativa) | SID: `jdbc:oracle:thin:@HOST_FIAP:1521:SID_FIAP` |
| `DB_USER` | Usuário acadêmico informado pela FIAP |
| `DB_PASSWORD` | Senha desse usuário, preenchida somente no ambiente local |

`HOST_FIAP`, `SERVICE_NAME_FIAP`, `SID_FIAP` e `USUARIO_FIAP` são marcadores, não dados de acesso. Use a porta informada pela FIAP caso seja diferente de 1521. Não coloque usuário/senha dentro da URL. A configuração atual aceita a conexão Thin convencional por service name ou SID, sem parâmetros adicionais na URL; se a instituição exigir wallet/TLS específico, será necessário configurar esse acesso antes de validar a conexão.

No macOS/Linux, exporte as variáveis do arquivo preenchido (sintaxe de shell, com valores entre aspas):

```sh
set -a
. ./.env
set +a
```

Java não carrega `.env` automaticamente. Esse arquivo está no `.gitignore`. Não inclua senhas, arquivos de wallet ou dados reais de acesso no repositório nem em capturas de tela. No PowerShell, configure as três variáveis com `$env:DB_URL`, `$env:DB_USER` e `$env:DB_PASSWORD` na sessão local, sem registrar a senha em scripts versionados.

## Aplicar as tabelas e a carga

Conecte-se ao **seu schema acadêmico** no DBeaver, SQL Developer ou SQLcl usando os mesmos host, porta e service name/SID. Confirme que as tabelas `USUARIO`, `CONTA_ACESSO`, `LOCAL`, `AVALIACAO`, `ESTIMULO` e `LOCAL_ESTIMULO` não existem nesse schema; não substitua tabelas de outro trabalho.

Abra e execute como script (F5) nesta ordem:

1. `database/01_schema.sql`;
2. `database/02_seed.sql`;
3. `database/verificar_etapa1.sql`;
4. `database/03_consultas_relatorios.sql` (consultas de leitura).

Alternativamente, em SQLcl/SQL*Plus já conectado, a partir da raiz do projeto:

```sql
@database/01_schema.sql
@database/02_seed.sql
@database/verificar_etapa1.sql
@database/03_consultas_relatorios.sql
```

O usuário precisa de privilégios para criar tabelas e gatilhos e de quota no tablespace. Não tente criar usuários, conceder privilégios ou iniciar/parar o servidor institucional. Em caso de falta de permissão, solicite orientação à FIAP.

Os scripts não apagam dados existentes e **não são idempotentes**. Schema e carga devem ser aplicados uma vez. DDL no Oracle faz commit implícito: uma falha pode deixar tabelas parcialmente criadas. A carga faz commit somente no final e usa `WHENEVER SQLERROR ... ROLLBACK`; verifique sempre a saída do cliente. O gatilho não deve apresentar erros de compilação. A verificação SQL exige a carga original e reverte suas tentativas de escrita.

Se `01_schema.sql` e `02_seed.sql` já foram aplicados antes da implementação de login, não os execute novamente. Execute `database/04_migracao_autenticacao.sql`; ele cria `CONTA_ACESSO`, o índice de pseudônimo sem distinção de maiúsculas/minúsculas e desativa perfis administrativos legados. Em uma base que já possua a tabela e o índice, `database/05_remover_acesso_administrativo.sql` aplica somente a desativação. Para substituir a carga antiga de “Local demonstrativo” pelos dez locais reais sem alterar IDs, avaliações ou associações, execute uma única vez `database/06_migracao_locais_reais.sql`.

### Locais reais da carga

- Biblioteca Mário de Andrade — Rua da Consolação, 94 ([Prefeitura de São Paulo](https://prefeitura.sp.gov.br/web/cultura/w/servicos/541));
- Praça das Artes — Avenida São João, 281 ([Fundação Theatro Municipal](https://prefeitura.sp.gov.br/web/fundacao_theatro_municipal/w/praca_das_artes/28445));
- Centro Cultural São Paulo — Rua Vergueiro, 1000 ([Prefeitura de São Paulo](https://prefeitura.sp.gov.br/web/cultura/w/servicos/541));
- Mercado Municipal Paulistano — Rua da Cantareira, 306 ([Prefeitura de São Paulo](https://prefeitura.sp.gov.br/web/subprefeituras/w/noticias/275457));
- Terminal Pinheiros — Rua Gilberto Sabino, 130 ([SPTrans](https://www.sptrans.com.br/terminais/pinheiros/));
- Parque Ibirapuera — Avenida Pedro Álvares Cabral, s/n ([Prefeitura de São Paulo](https://www2.prefeitura.sp.gov.br/web/meio_ambiente/w/parques/regiao_sul/14062));
- Museu da Língua Portuguesa — Estação da Luz, acesso pelo Portão A ([site oficial](https://www.museudalinguaportuguesa.org.br/visite/como-chegar/));
- Pinacoteca de São Paulo — Pina Luz, Praça da Luz, 2 ([site oficial](https://pinacoteca.org.br/visita/como-chegar/));
- MASP — Avenida Paulista, 1578 ([site oficial](https://www.masp.org.br/visite));
- Jardim Botânico de São Paulo — Avenida Miguel Estéfano, 3031 ([site oficial](https://www.jardimbotanico.com.br/como-chegar/)).

Os locais e endereços são reais, mas toda avaliação que veio em `02_seed.sql` permanece identificada como **simulada para demonstração**.

Para **reiniciar somente as tabelas deste projeto**, após confirmar que deseja perder os dados SensiMap do seu schema, execute os comandos abaixo e reaplique os scripts. Se algum objeto não tiver sido criado, omita apenas seu `DROP`:

```sql
DROP TABLE local_estimulo;
DROP TABLE avaliacao;
DROP TABLE estimulo;
DROP TABLE local;
DROP TABLE conta_acesso;
DROP TABLE usuario;
```

O gatilho é removido junto com `avaliacao`. Não há reset automático no Java.

## Compilar, verificar e executar

```sh
mvn clean package
java -cp 'target/classes:target/test-classes:target/dependency/*' br.com.sensimap.VerificacaoEtapa1
java -cp 'target/classes:target/test-classes:target/dependency/*' br.com.sensimap.VerificacaoEtapa2
java -cp 'target/classes:target/test-classes:target/dependency/*' br.com.sensimap.dao.jdbc.VerificacaoJdbcUtil
java -cp 'target/classes:target/test-classes:target/dependency/*' br.com.sensimap.web.VerificacaoWeb
java -Duser.timezone=America/Sao_Paulo -cp 'target/classes:target/dependency/*' br.com.sensimap.app.Main
```

No Windows, substitua `:` por `;` nos classpaths. O build gera `target/sensimap-0.1.0.jar` e copia o driver para `target/dependency`. Também é possível usar esse JAR no lugar de `target/classes` no classpath. Ele não é um JAR executável via `java -jar`.

`mvn package` compila os verificadores, mas **não os executa automaticamente**: eles têm `main`, sem JUnit ou dependências adicionais. Execute os quatro comandos de verificação explicitamente. Eles verificam domínio, configuração, descoberta do driver, classificação, JSON, parâmetros HTTP e mensagens controladas, sem acessar o banco.

O `Main` abre uma conexão real com as variáveis configuradas e usa `PreparedStatement` e `try-with-resources`. Somente após obter o resultado do Oracle imprime:

```text
Conexão real com Oracle confirmada: SELECT 1 FROM DUAL retornou 1.
```

Essa mensagem é a saída esperada, **não uma conexão já comprovada com a FIAP**. Códigos de saída: `0` operação concluída/ajuda, `1` falha de persistência ou demonstração, `2` entrada/configuração inválida. Falhas não imprimem senha, URL ou stack trace. Confira usuário/senha, conta bloqueada/expirada, service name/SID e rede institucional conforme a mensagem. O diagnóstico não cria tabelas, não aplica carga e não verifica CRUD.

## Comandos de CRUD e indicadores

Após compilar e exportar as variáveis, defina uma função de conveniência no macOS/Linux:

```sh
sensimap() {
  java -Duser.timezone=America/Sao_Paulo -cp 'target/classes:target/dependency/*' br.com.sensimap.app.Main "$@"
}
sensimap --ajuda
sensimap usuarios
sensimap locais
sensimap locais "Biblioteca" ""
sensimap estimulos
sensimap avaliacoes
sensimap demo-crud
```

`demo-crud` cria dados fictícios com nomes únicos, exercita CRUD das quatro entidades, N:N, duplicidades, exclusões bloqueadas e indicadores de 0/2/3 avaliações, depois remove apenas seus próprios registros na mesma transação. Qualquer falha interrompe a demonstração e tenta rollback. IDs consumidos pelo Oracle podem deixar lacunas. Esse comando **não comprova persistência entre processos**, pois remove os registros antes do commit; utilize o roteiro abaixo para isso. Nenhum script SQL é aplicado automaticamente pelo Java.

Para criar e consultar uma avaliação manualmente (substitua os IDs pelos números realmente retornados; não digite os marcadores):

```text
sensimap usuario-criar "demo_banca"
sensimap local-criar "Local fictício da banca" "Bairro fictício" "Dados simulados para demonstração"
sensimap estimulo-criar "Estímulo fictício da banca" "Exemplo acadêmico"
sensimap associar LOCAL_ID ESTIMULO_ID
sensimap avaliacao-criar USUARIO_ID LOCAL_ID 2026-11-01 MANHA 2 3 4 "Relato fictício"
sensimap local-ver LOCAL_ID
sensimap local-ver LOCAL_ID MANHA
sensimap avaliacao-editar AVALIACAO_ID USUARIO_ID LOCAL_ID 2026-11-01 MANHA 1 2 3 "Relato fictício editado"
sensimap avaliacao-ver AVALIACAO_ID
```

Cada invocação inicia outra JVM e abre outra conexão. Após o cadastro retornar “transação confirmada”, execute `local-ver LOCAL_ID` em outro processo, ou feche o terminal, reexporte as variáveis e consulte novamente. Confirme também com um SELECT no SQL Developer usando o ID retornado. A data fictícia do exemplo deve ser passada ou presente no momento da execução.

Para testar edição de usuário/local/estímulo e limpar somente os registros desse exemplo:

```text
sensimap usuario-editar USUARIO_ID "demo_banca_editado"
sensimap local-editar LOCAL_ID "Local fictício editado" "Bairro fictício" "Descrição editada"
sensimap estimulo-editar ESTIMULO_ID "Estímulo fictício editado" "Descrição editada"
sensimap avaliacao-excluir AVALIACAO_ID
sensimap desassociar LOCAL_ID ESTIMULO_ID
sensimap local-excluir LOCAL_ID
sensimap estimulo-excluir ESTIMULO_ID
sensimap usuario-excluir USUARIO_ID
```

A ajuda lista também os comandos de consulta por ID. As exclusões só são realizadas por comandos explícitos. O console não pede confirmação adicional; a interface web pede confirmação antes de excluir. Os comandos de local criam dados demonstrativos sem coordenadas e editam nome, bairro e descrição; o DAO aceita todos os atributos válidos do modelo. Endereço e coordenadas permanecem preservados ao editar pelo console.

## Abrir a interface web

No macOS, clique duas vezes em `Iniciar-SensiMap.command` e informe URL JDBC, usuário/schema e senha quando o terminal solicitar. A senha fica oculta, existe apenas na memória do processo e não é salva em arquivo. Pelo terminal, exporte antes as três variáveis descritas na seção de configuração e use:

```sh
sh scripts/iniciar-web.sh
```

Quando aparecer `SensiMap disponível em http://127.0.0.1:8080`, abra:

```text
http://127.0.0.1:8080
```

Mantenha o terminal aberto enquanto utiliza o site. Para encerrar, pressione `Ctrl+C`. O servidor aceita conexões somente em `127.0.0.1:8080`; não foi projetado para publicação aberta.

O site exige `DB_URL`, `DB_USER` e `DB_PASSWORD`. Sem as três variáveis, o servidor recusa a inicialização com uma mensagem controlada. Não existe modo de persistência no navegador: cadastro, login, catálogo e formulários usam somente a API Java/JDBC e o Oracle. Cada cadastro cria `USUARIO` e `CONTA_ACESSO` na mesma transação.

Áreas e páginas disponíveis:

- **Área pública:** Início, Entrar e Cadastrar. O menu inicial não expõe as funções internas;
- **Área do usuário:** Explorar e Contribuir, acessíveis depois do cadastro ou entrada por pseudônimo;
- **Saída:** encerra a sessão e retorna à página inicial.

Não existe acesso administrativo na interface ou na autenticação. Em modo Oracle, o cadastro de usuário grava pseudônimo e credencial com perfil `USUARIO`; PBKDF2-HMAC-SHA256 usa salt aleatório e 120 mil iterações. O servidor mantém um token opaco por oito horas em cookie `HttpOnly` e `SameSite=Strict` e limita avaliações ao usuário da sessão. Nenhum cadastro, senha ou percepção é salvo no navegador. Reiniciar o Java encerra todas as sessões. Como o MVP roda em HTTP apenas no endereço local, este fluxo não substitui autenticação de produção com HTTPS, recuperação de senha, bloqueio por tentativas e gestão de segredos.

O frontend cria conteúdo dinâmico com `textContent`; comentários não são interpretados como HTML. As três dimensões permanecem independentes, o recorte por período é exibido e locais sem relatos explicam que ausência de registros não significa ausência de estímulos. A API exige sessão de usuário para os dados internos; somente configuração e rotas de autenticação são públicas. O CRUD administrativo permanece demonstrável pelo console Java/JDBC, sem uma área web dedicada.

## Verificação local sem Maven

Se o driver já estiver em `target/dependency`, execute:

```sh
sh scripts/verificar-local.sh
java -Duser.timezone=America/Sao_Paulo -cp 'target/verificacao-local/sensimap.jar:target/dependency/*' br.com.sensimap.app.Main --ajuda
```

Se estiver em outro local, passe `JDBC_JAR=/caminho/ojdbc11.jar sh scripts/verificar-local.sh`. Ajuste também o classpath ao executar o `Main`. O script compila com `--release 17`, executa os cinco verificadores Java e gera um JAR com os recursos web em `target/verificacao-local`. Não instala ferramentas nem baixa arquivos. Para rodar os comandos de CRUD com esse build, substitua `target/classes` por `target/verificacao-local/sensimap.jar` na função acima.

As regras e a ausência de persistência no navegador podem ser verificadas separadamente com:

```sh
node --experimental-default-type=module scripts/verificar-web.mjs
```

Em 18/11/2026, esse caminho foi verificado com JDK 26 e compatibilidade de linguagem/API/bytecode 17. Foram aprovadas 120 verificações Java, 43 verificações JavaScript e 30 verificações HTTP com acesso somente de leitura ao Oracle da FIAP. Executar em um **runtime JDK 17**, fazer o build Maven e ensaiar uma gravação/releitura pelo navegador continuam pendentes neste ambiente. Não use o JAR antigo em `target/sensimap-0.1.0.jar` como evidência desta etapa; ele só será atualizado por um novo build Maven.

## Modelo, exclusões e dados de demonstração

São seis tabelas, incluindo `CONTA_ACESSO` e a N:N entre local e estímulo. A carga tem 3 pseudônimos de relatos, nenhuma credencial pré-cadastrada, 10 locais reais de São Paulo, 5 estímulos, 10 associações simuladas e 10 avaliações simuladas: Biblioteca Mário de Andrade com 4, Praça das Artes com 2, Centro Cultural São Paulo com 1, Mercado Municipal Paulistano com 3 e os outros 6 locais sem avaliações. As contas são criadas somente pelo cadastro da aplicação. As datas fictícias são `2026-11-01`.

Excluir usuário ou local com avaliações é impedido pela FK. Avaliações podem ser excluídas pelo comando `avaliacao-excluir ID`. Excluir um local sem avaliações ou um estímulo remove automaticamente suas associações N:N, sem apagar a outra entidade. A duplicação da mesma visita (usuário, local, data e período) é impedida por `UNIQUE`.

O schema e a carga inicial foram aplicados e conferidos no Oracle da FIAP pelo DBeaver. Indicadores consultam `AVG`, `COUNT` e `MAX(data_visita)` das avaliações persistidas. Cada dimensão é classificada independentemente: n<3 → “Dados insuficientes”; média ≤2 → “Baixa”; >2 e ≤3,50 → “Moderada”; >3,50 → “Alta”. A média bruta define a classificação; a apresentação arredonda a duas casas. Com n=0, o console também mostra “Ainda não avaliado” e explica a ausência de registros. A data mostrada é a última **visita** do recorte, não a data de digitação do relato.

As telas foram abertas no Safari. O servidor web não oferece fallback local: sem Oracle disponível, as operações retornam erro e nenhuma mensagem afirma que houve gravação.

## Resultados e próximos passos

Consulte [docs/testes.md](docs/testes.md) para evidências e limitações. Para reproduzir a conexão real com a FIAP e executar os scripts, são necessários `DB_URL`, `DB_USER`, `DB_PASSWORD`, rede e privilégios. Os diagramas finais estão na raiz em `Diagrama_ER_SensiMap.pdf` e `Diagrama_Classes_SensiMap.pdf`; as fontes editáveis permanecem em `docs/*.mmd`. Preencha `integrantes.txt` com os dados reais do grupo e o link público do pitch.

Próxima etapa: ensaiar cadastro e avaliação pelo navegador com conferência no DBeaver, revisar o pacote final, preencher as identificações reais do grupo e acrescentar o link público do pitch.

## Uso de IA

OpenAI Codex foi utilizado como apoio para estruturar o projeto, escrever código Java, scripts SQL, verificações e documentação. Cabe ao grupo revisar, compreender, validar no Oracle institucional e explicar o material na banca. O produto não usa APIs de IA, serviços pagos ou frameworks de aplicação.
