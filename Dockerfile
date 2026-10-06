# =========================
# Build stage
# =========================
FROM maven:3.10-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml ./

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests -B

# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && \
    adduser -S spring -G spring

USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
