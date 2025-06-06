FROM maven:3.8.8-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ ./src/
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN useradd -ms /bin/bash springuser && chown -R springuser:springuser /app
USER springuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
