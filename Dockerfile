# ── Stage 1: build ──────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S aspgroup && adduser -S aspuser -G aspgroup
USER aspuser

COPY --from=builder /app/target/asp-pago-integration-*.jar app.jar

# Puertos
EXPOSE 8080

# Perfil por defecto del contenedor. Secretos, credenciales y URLs externas
# deben inyectarse en runtime por variables de ambiente.
ENV SPRING_PROFILES_ACTIVE=prod

# JVM tuning para contenedores
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
