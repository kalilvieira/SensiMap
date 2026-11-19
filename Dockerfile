FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/classes ./classes
COPY --from=build /app/target/dependency ./dependency
USER 10001:10001
EXPOSE 10000
CMD ["java", "-Duser.timezone=America/Sao_Paulo", "-cp", "classes:dependency/*", "br.com.sensimap.app.Main", "--web"]
