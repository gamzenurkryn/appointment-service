FROM eclipse-temurin:21-jre AS appointment-runtime
WORKDIR /app
COPY build/libs/appointment-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre AS gateway-runtime
WORKDIR /app
COPY api-gateway/build/libs/api-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre AS call-runtime
WORKDIR /app
COPY call-service/build/libs/call-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre AS notification-runtime
WORKDIR /app
COPY notification-service/build/libs/notification-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre AS crm-runtime
WORKDIR /app
COPY crm-service/build/libs/crm-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre AS whatsapp-runtime
WORKDIR /app
COPY whatsapp-service/build/libs/whatsapp-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
