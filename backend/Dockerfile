# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-22 AS build
WORKDIR /app

# Copy pom.xml and fetch dependencies
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests clean package

# ---------- Run stage ----------
FROM eclipse-temurin:22-jre
WORKDIR /app

# Copy the jar built above
COPY --from=build /app/target/*.jar app.jar

# Render automatically sets PORT, so pass it to Spring Boot
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh","-c","java ${JAVA_OPTS} -jar /app/app.jar --server.port=${PORT}"]