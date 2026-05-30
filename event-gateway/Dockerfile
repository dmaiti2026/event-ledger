FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY account-service/pom.xml account-service/
COPY event-gateway/pom.xml event-gateway/
COPY event-gateway/src event-gateway/src
RUN mvn -pl event-gateway -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/event-gateway/target/event-gateway-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
