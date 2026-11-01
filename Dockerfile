# Stage 1: build the jar with Maven and Java 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# Stage 2: small runtime image with only the JRE and the jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/campus-connect-api.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
