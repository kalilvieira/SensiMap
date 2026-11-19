#!/bin/sh
# Alternativa local quando Maven não está instalado. Não baixa dependências.
set -eu
cd "$(dirname "$0")/.."
build_dir="target/verificacao-local"
driver_jar="${JDBC_JAR:-target/dependency/ojdbc11-23.26.4.0.0.jar}"
if [ ! -f "$driver_jar" ]; then
    echo "Driver ausente. Execute mvn package ou defina JDBC_JAR com o caminho do ojdbc11." >&2
    exit 2
fi
rm -rf "$build_dir/main" "$build_dir/test"
mkdir -p "$build_dir/main" "$build_dir/test"
find src/main/java -name '*.java' -print > "$build_dir/main.txt"
find src/test/java -name '*.java' -print > "$build_dir/test.txt"
javac --release 17 -encoding UTF-8 -d "$build_dir/main" @"$build_dir/main.txt"
javac --release 17 -encoding UTF-8 -cp "$build_dir/main" -d "$build_dir/test" @"$build_dir/test.txt"
classpath="$build_dir/main:$build_dir/test:$driver_jar"
java -Duser.timezone=America/Sao_Paulo -cp "$classpath" br.com.sensimap.VerificacaoEtapa1
java -Duser.timezone=America/Sao_Paulo -cp "$classpath" br.com.sensimap.VerificacaoEtapa2
java -Duser.timezone=America/Sao_Paulo -cp "$classpath" br.com.sensimap.dao.jdbc.VerificacaoJdbcUtil
java -Duser.timezone=America/Sao_Paulo -cp "$classpath" br.com.sensimap.web.VerificacaoWeb
java -Duser.timezone=America/Sao_Paulo -cp "$classpath" br.com.sensimap.service.VerificacaoAuth
cp -R src/main/resources/. "$build_dir/main/"
jar --create --file "$build_dir/sensimap.jar" -C "$build_dir/main" .
echo "Build local concluído em $build_dir/sensimap.jar. Oracle não foi acessado."
