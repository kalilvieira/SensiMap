# Verificações — Etapa 4

Registro atualizado em 19/11/2026. Resultados abaixo foram observados neste ambiente; a conexão HTTP com o Oracle foi comprovada sem criar registros de teste permanentes. O CRUD Oracle completo e o endereço publicado ainda devem ser exercitados no roteiro de apresentação.

## Ambiente e resultados reais

| Verificação | Resultado |
|---|---|
| `java -version` | Oracle JDK 26 disponível; nenhum runtime JDK 17 listado por `java_home -V` |
| `mvn clean package` | Não executado: shell retornou `mvn: command not found` (saída 127) |
| `sh scripts/verificar-local.sh` | Saída 0; compilação de produção/testes com `javac --release 17` e JAR gerado |
| `VerificacaoEtapa1` | 43 verificações aprovadas: entidades, local colaborativo, entradas inválidas, configuração e descoberta do driver |
| `VerificacaoEtapa2` | 38 verificações aprovadas: n=0/1/2/3+, limites 2,00/3,50, arredondamento e dimensões independentes |
| `VerificacaoJdbcUtil` | 11 verificações aprovadas: categorias de erro sem mensagem interna e escape de busca |
| `VerificacaoWeb` | 37 verificações aprovadas: escape JSON, parâmetros URL-encoded, IDs, notas, datas, períodos, PBKDF2 e configuração HTTP local/publicada |
| `VerificacaoAuth` | 7 verificações aprovadas: cadastro de usuário, hash sem senha original, login, senha errada e duplicidade |
| `scripts/verificar-web.mjs` | 53 verificações aprovadas: classificações, agregação, validação client-side, formulário de novo local, contrato da API de autenticação, falha de rede controlada e ausência de armazenamento local |
| `vercel.json` | JSON válido; rota `/api/*` preparada para `BACKEND_URL` e arquivos estáticos preservados |
| Build Docker / serviço público | Não executado: Docker e Maven não estão instalados neste ambiente; a tentativa de escutar em interface pública também não pôde ser concluída aqui |
| `Main --ajuda` no JAR local | Saída 0; comandos de CRUD e demonstração presentes |
| `Main` com `DB_URL`, `DB_USER` e `DB_PASSWORD` ausentes | Saída 2, mensagem indicando `DB_URL`, sem stack trace |
| Inspeção dos arquivos `.class` do build local | Todos com major version 61 (bytecode Java 17) |
| Acesso Oracle pelo DBeaver | Schema e carga conferidos; perfil administrativo legado desativado, consulta retornou `0` acessos administrativos ativos e os 10 locais reais foram persistidos preservando os IDs |
| CRUD real e persistência entre processos | **Pendentes** para execução completa pela aplicação Java |
| Frontend administrativo | Página e JavaScript removidos; servidor não os inclui na lista de recursos; login oferece somente pseudônimo e senha de usuário |
| Safari após a remoção administrativa | Login exibiu somente pseudônimo/senha; acesso direto a `admin.html` retornou “Página não encontrada.” |
| Persistência do frontend | Fallback local removido; cadastro, login, catálogo e avaliações usam somente a API Java/JDBC |
| Cadastro/login no navegador | Cadastro real `KalV7`, saída e nova entrada validados no Safari; a sessão abriu Explorar e Contribuir com o mesmo usuário |
| Locais reais no frontend | Explorar exibiu os 10 nomes/bairros reais; o seletor de Contribuir exibiu os mesmos 10 registros após leitura do Oracle |
| Responsividade | CSS contém recortes para 1000 px e 800 px; layout reduzido foi inspecionado no Safari |
| `scripts/verificar-http.py` | 30 verificações aprovadas com servidor em modo Oracle: páginas/assets atuais, ausência de admin/demo, controles de origem, conexão ao banco e redirecionamento de `localhost` para `127.0.0.1` |
| Renderização/exportação dos diagramas | Concluída; UML em PDF A2 horizontal e ER em PDF A3 horizontal, ambos renderizados novamente em PNG e inspecionados sem cortes ou sobreposições |

Os 189 testes locais (136 Java e 53 JavaScript) e os 30 testes HTTP do ensaio anterior não substituem o roteiro completo de escrita e releitura. A autenticação de serviço foi verificada com DAOs em memória; o teste HTTP anterior confirmou a conexão ao Oracle real, mas permaneceu somente leitura. Não foi utilizado um substituto de Oracle para declarar persistência funcional. A compilação com `--release 17` limita as APIs e o bytecode, mas não equivale a executar em runtime JDK 17. A criação e releitura de um local colaborativo ainda devem ser confirmadas no serviço publicado e no Oracle FIAP.

O JAR desta execução está em `target/verificacao-local/sensimap.jar`. O arquivo preexistente `target/sensimap-0.1.0.jar` não foi recompilado pelo Maven nesta sessão.

## Reproduzir as verificações locais

Com o driver já obtido:

```sh
sh scripts/verificar-local.sh
java -Duser.timezone=America/Sao_Paulo -cp 'target/verificacao-local/sensimap.jar:target/dependency/*' br.com.sensimap.app.Main --ajuda
node --experimental-default-type=module scripts/verificar-web.mjs
```

Com Maven disponível, execute `mvn clean package` e os quatro verificadores Java pelo classpath descrito no README. Os verificadores são programas com `main` e não rodam automaticamente pelo Surefire.

## Roteiro Oracle de escrita e releitura

1. Configure `DB_URL`, `DB_USER` e `DB_PASSWORD` conforme o README. Confirme o schema acadêmico e a versão Oracle 19c+.
2. Sobre schema sem as tabelas SensiMap, execute `01_schema.sql`, `02_seed.sql` e `verificar_etapa1.sql`. Confira também `CONTA_ACESSO` e o gatilho. Se o schema anterior já existe, execute `04_migracao_autenticacao.sql`; quando tabela e índice já existirem, aplique `05_remover_acesso_administrativo.sql`. Não repita criação/carga.
3. Rode `Main` sem argumentos para confirmar `SELECT 1 FROM DUAL`. Essa consulta não valida tabelas.
4. Rode `Main demo-crud`. Ele exercita CRUD das quatro entidades, IDs gerados, busca literal, N:N, conflitos, proteção de exclusões, agregações e filtro por período. O comando cria nomes exclusivos e remove apenas seus próprios registros dentro da transação. Ao falhar, tenta rollback e retorna código 1. A sequência de identidade pode avançar mesmo em rollback.
5. Siga os comandos individuais do README para criar usuário/local/estímulo/avaliação. Registre os IDs retornados. Confirme a mensagem de commit antes de considerar a escrita concluída.
6. Encerre a JVM/terminal e execute `local-ver LOCAL_ID` e `avaliacao-ver AVALIACAO_ID` em novo processo após reexportar as variáveis. Compare com `SELECT * FROM local WHERE id = ...` e `SELECT * FROM avaliacao WHERE id = ...` no SQL Developer, usando os IDs reais. Este passo verifica a persistência após reinício.
7. Atualize a avaliação, consulte pelo console e SQL, exclua e confirme sua ausência nas duas consultas. Exclua os demais registros próprios conforme o README.
8. Com registros de teste, tente nota 0/6, data futura e período inválido. Tente repetir apelido e visita. Para referência inexistente, use o ID de um registro de teste que acabou de excluir; não presuma que um número arbitrário esteja livre. Confirme mensagem clara e ausência de gravação parcial.
9. Execute `03_consultas_relatorios.sql` e compare amostra, médias e período com `local-ver ID [PERIODO]`. As médias SQL para comparação podem conter mais casas; o console mostra duas.
10. Confirme que usuário/local com avaliações não podem ser excluídos e que excluir estímulo ou local sem avaliações remove apenas suas associações. Confira também os demais registros do outro lado da associação.

Resultados **esperados a partir dos INSERTs**, ainda não observados no banco:

| Local real / recorte simulado | n | Ruído / movimento / imprevisibilidade | Classes |
|---|---:|---|---|
| Biblioteca Mário de Andrade / todos | 4 | 2,50 / 3,50 / 3,50 | Moderada / Moderada / Moderada |
| Biblioteca Mário de Andrade / MANHA | 3 | 2,00 / 3,00 / 4,00 | Baixa / Moderada / Alta |
| Biblioteca Mário de Andrade / TARDE | 1 | 4,00 / 5,00 / 2,00 | Dados insuficientes nas três dimensões |
| Praça das Artes / todos | 2 | 3,50 / 2,50 / 1,50 | Dados insuficientes nas três dimensões |
| Terminal Pinheiros / todos | 0 | Médias nulas | Ainda não avaliado; dados insuficientes |

Ao executar os testes pendentes, acrescente data, ambiente, comandos e resultados observados. Não copie os resultados esperados como evidência de execução. Não registre credenciais, URLs internas completas ou informações pessoais.

## Próximos critérios de aceite

- Build Maven e execução em JDK 17.
- Scripts, restrições, CRUD, rollback e persistência comprovados no Oracle institucional.
- Navegação por teclado e viewport real de smartphone ainda devem ser conferidos manualmente em mais de um navegador.
- Escrita HTTP no Oracle: criar pelo navegador, conferir no DBeaver e após reload/reinício durante o ensaio do pitch.
- Autenticação Oracle: cadastrar, sair, entrar novamente, testar senha errada e confirmar `CONTA_ACESSO` sem senha em texto puro.
- Integrantes reais e link público do pitch preenchidos no pacote final.
