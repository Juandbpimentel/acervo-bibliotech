FROM gradle:9.1-jdk21 AS build

# Diretório de trabalho no container
WORKDIR /app

# Copiar todo o projeto (inclui gradlew e gradle/wrapper)
COPY . .

# Garantir permissões do wrapper (caso não seja executável por default)
RUN chmod +x ./gradlew || true

# Build usando Gradle Wrapper (gera bootJar em build/libs)
RUN ./gradlew clean bootJar -x test --no-daemon

# Preparar artefato
RUN mkdir -p /app/dist \
    && cp build/libs/*.jar /app/dist/app.jar

# Runtime leve
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/dist/app.jar ./app.jar

# Expôr a porta em que o Spring Boot estará rodando
EXPOSE 8090

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
