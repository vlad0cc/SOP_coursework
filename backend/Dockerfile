FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY documents-api-contract documents-api-contract
COPY documents-events-contract documents-events-contract
COPY documents-grpc-contract documents-grpc-contract
COPY document-rest document-rest
COPY audit-service audit-service
COPY grpc-analytics-server grpc-analytics-server
COPY grpc-enrichment-client grpc-enrichment-client
COPY notification-service notification-service

ARG MODULE

RUN chmod +x mvnw && ./mvnw -pl "${MODULE}" -am -DskipTests package

FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

ARG MODULE

COPY --from=build /workspace/${MODULE}/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
