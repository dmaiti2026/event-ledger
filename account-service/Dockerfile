FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY account-service/pom.xml account-service/
COPY event-gateway/pom.xml event-gateway/
COPY account-service/src account-service/src
RUN mvn -pl account-service -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/account-service/target/account-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
