#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
build_dir="target/web-local"
rm -rf "$build_dir/classes"
mkdir -p "$build_dir/classes"
find src/main/java -name '*.java' -print > "$build_dir/fontes.txt"
javac --release 17 -encoding UTF-8 -d "$build_dir/classes" @"$build_dir/fontes.txt"
cp -R src/main/resources/. "$build_dir/classes/"
exec java -Duser.timezone=America/Sao_Paulo -cp "$build_dir/classes:target/dependency/*${JDBC_JAR:+:$JDBC_JAR}" br.com.sensimap.app.Main --web
