# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only the backend module (faster builds if you also have a frontend)
COPY backend/pom.xml backend/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f backend/pom.xml -q -DskipTests dependency:go-offline

# Now copy the rest and build
COPY backend ./backend
RUN --mount=type=cache,target=/root/.m2 mvn -f backend/pom.xml -q -DskipTests clean package

# ---------- Run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the fat jar built above (handles any jar name)
COPY --from=build /app/backend/target/*.jar app.jar

# Bind to Render's PORT even if you don't have application.properties
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh","-c","java ${JAVA_OPTS} -jar /app/app.jar --server.port=${PORT}"]