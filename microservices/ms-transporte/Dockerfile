FROM eclipse-temurin:21-alpine
RUN mkdir -p /opt/app/ms-transporte
COPY target/ms-transporte-0.0.1-SNAPSHOT.jar /opt/app/ms-transporte/ms-transporte.jar
CMD ["java", "-jar", "/opt/app/ms-transporte/ms-transporte.jar"]