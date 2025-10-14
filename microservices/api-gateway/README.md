# Microservicio API Gateway

Este microservicio funciona como la puerta de entrada principal al Sistema Logístico de Transporte de Contenedores, enrutando las peticiones a los diferentes microservicios y proporcionando seguridad centralizada.

## Funcionalidades principales

- Enrutamiento de peticiones a microservicios internos
- Control de acceso y autenticación centralizada
- Balanceo de carga
- Limitación de tasas (rate limiting)
- Monitoreo de tráfico

## Vista general del sistema

```
sistema-logistico/
├── microservices/              # Microservicios del sistema
│   ├── api-gateway/            # API Gateway para enrutamiento y seguridad
│   ├── ms-cliente/             # Microservicio de gestión de clientes
│   ├── ms-transporte/          # Microservicio de gestión de transporte
│   └── ms-seguimiento/         # Microservicio de seguimiento y notificaciones
├── docker/                     # Archivos de configuración Docker
├── docs/                       # Documentación del proyecto
└── README.md                   # Documentación principal
```

## Estructura del microservicio (simplificada)

```
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/               # Código fuente Java
│   │   │   └── com/logistica/gateway/
│   │   │       ├── config/     # Configuraciones del gateway
│   │   │       ├── filter/     # Filtros de peticiones
│   │   │       ├── security/   # Configuración de seguridad
│   │   │       └── GatewayApplication.java
│   │   └── resources/          # Archivos de configuración
│   │       ├── application.yml # Configuración principal
│   │       └── bootstrap.yml   # Configuración de arranque
│   └── test/                   # Pruebas
├── Dockerfile                  # Configuración para Docker
└── pom.xml                     # Dependencias del proyecto
```

## Componentes principales

### Configuración (Config)
Define las rutas y reglas de enrutamiento a los diferentes microservicios.

```java
// Ejemplo simplificado de RouteConfig.java
@Configuration
public class RouteConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Ruta para el microservicio de clientes
            .route("cliente-service", r -> r
                .path("/api/clientes/**")
                .uri("lb://ms-cliente"))
                
            // Ruta para el microservicio de transporte
            .route("transporte-service", r -> r
                .path("/api/transporte/**")
                .uri("lb://ms-transporte"))
                
            // Ruta para el microservicio de seguimiento
            .route("seguimiento-service", r -> r
                .path("/api/seguimiento/**")
                .uri("lb://ms-seguimiento"))
                
            .build();
    }
}
```

### Filtros (Filter)
Permiten modificar las peticiones entrantes y salientes.

```java
// Ejemplo simplificado de LoggingFilter.java
@Component
public class LoggingFilter implements GlobalFilter {
    
    private Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Registra información de la petición
        logger.info("Petición recibida: {} {}", 
            exchange.getRequest().getMethod(), 
            exchange.getRequest().getPath());
            
        // Continúa con la cadena de filtros
        return chain.filter(exchange);
    }
}
```

### Seguridad (Security)
Gestiona la autenticación y autorización de usuarios.

```java
// Ejemplo simplificado de SecurityConfig.java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf().disable()
            .authorizeExchange()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/auth/**").permitAll()
                .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
                .jwt()
            .and().and().build();
    }
}
```

## Configuración típica

El archivo application.yml configura las rutas y servicios:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: cliente-service
          uri: lb://ms-cliente
          predicates:
            - Path=/api/clientes/**
          
        - id: transporte-service
          uri: lb://ms-transporte
          predicates:
            - Path=/api/transporte/**
            
        - id: seguimiento-service
          uri: lb://ms-seguimiento
          predicates:
            - Path=/api/seguimiento/**
```

## Tecnologías utilizadas

- **Java**: Lenguaje de programación
- **Spring Cloud Gateway**: Framework para API Gateway
- **Spring Security**: Para autenticación y autorización
- **Spring Cloud LoadBalancer**: Para balanceo de carga
- **Docker**: Para contenerización y despliegue simplificado

## Cómo ejecutar el microservicio

1. **Compilar el proyecto**:
   ```bash
   mvn clean package
   ```

2. **Ejecutar en desarrollo**:
   ```bash
   mvn spring-boot:run
   ```

3. **Ejecutar con Docker**:
   ```bash
   docker build -t api-gateway .
   docker run -p 8080:8080 api-gateway
   ```

## Documentación

El API Gateway expone un endpoint de Actuator para monitoreo:
```
http://localhost:8080/actuator
```