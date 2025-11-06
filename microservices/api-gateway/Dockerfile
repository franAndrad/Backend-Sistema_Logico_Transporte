FROM eclipse-temurin:21-jdk-alpine
RUN mkdir -p /opt/app/api-gateway
RUN keytool -genkeypair \
    -alias gateway-ssl \
    -keyalg RSA -keysize 2048 \
    -storetype PKCS12 \
    -keystore /opt/app/api-gateway/gateway.p12 \
    -storepass changeit -keypass changeit \
    -dname "CN=localhost, OU=Dev, O=Logistica, L=Cordoba, S=Cordoba, C=AR" \
    -validity 3650 \
    -ext "SAN=dns:localhost"
COPY target/api-gateway-0.0.1-SNAPSHOT.jar /opt/app/api-gateway/api-gateway.jar
WORKDIR /opt/app/api-gateway
EXPOSE 8443
CMD ["java", "-jar", "/opt/app/api-gateway/api-gateway.jar"]
