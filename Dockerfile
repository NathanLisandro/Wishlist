# ---- Build stage ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
# Cache de dependências: copia o wrapper e os scripts primeiro.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true
# Código e build do jar (sem rodar testes — testes exigem Docker/Testcontainers).
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
