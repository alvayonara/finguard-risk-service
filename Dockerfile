FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -Dpit.skip=true

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/finguard-app/target/finguard-app-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]