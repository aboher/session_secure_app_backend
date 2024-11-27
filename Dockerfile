FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre-alpine AS final
WORKDIR /app
COPY --from=build /app/target/session-secure-app-0.0.1.jar session-secure-app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "session-secure-app.jar"]