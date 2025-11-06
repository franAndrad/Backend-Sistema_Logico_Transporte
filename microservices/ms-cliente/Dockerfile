FROM eclipse-temurin:21-alpine
RUN mkdir -p /opt/app/ms-cliente
COPY target/ms-cliente-0.0.1-SNAPSHOT.jar /opt/app/ms-cliente/ms-cliente.jar
CMD ["java", "-jar", "/opt/app/ms-cliente/ms-cliente.jar"]