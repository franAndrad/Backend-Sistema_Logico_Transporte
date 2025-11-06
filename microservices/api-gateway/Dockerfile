FROM eclipse-temurin:21-alpine
RUN mkdir -p /opt/app/api-gateway
COPY target/api-gateway-0.0.1-SNAPSHOT.jar /opt/app/api-gateway/api-gateway.jar
CMD ["java", "-jar", "/opt/app/api-gateway/api-gateway.jar"]