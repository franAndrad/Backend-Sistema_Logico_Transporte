# Microservicio de Cliente

Este microservicio forma parte del Sistema Logístico de Transporte de Contenedores y gestiona los clientes, contenedores y solicitudes de transporte.

## Funcionalidades principales

- Gestión de clientes y sus datos personales
- Gestión de contenedores asociados a clientes
- Registro y seguimiento de solicitudes de transporte
- Consultas sobre el estado de las solicitudes

## Vista general del sistema

```
sistema-logistico/
├── microservices/              # Microservicios del sistema
│   ├── api-gateway/            # API Gateway para enrutamiento y seguridad
│   ├── ms-cliente/             # Microservicio de gestión de clientes
│   └── ms-transporte/          # Microservicio de gestión de transporte (rutas, camiones, depósitos, seguimiento por estados)
├── docker/                     # Archivos de configuración Docker
├── docs/                       # Documentación del proyecto
└── README.md                   # Documentación principal
```

## Estructura del microservicio (simplificada)

```
ms-cliente/
├── src/
│   ├── main/
│   │   ├── java/               # Código fuente Java
│   │   │   └── com/logistica/cliente/
│   │   │       ├── controller/ # Controladores REST (endpoints API)
│   │   │       ├── model/      # Entidades de datos
│   │   │       ├── repository/ # Acceso a la base de datos
│   │   │       ├── service/    # Lógica de negocio
│   │   └── resources/          # Archivos de configuración
│   └── test/                   # Pruebas
├── Dockerfile                  # Configuración para Docker
└── pom.xml                     # Dependencias del proyecto

## Componentes principales

### Controladores (Controller)
Manejan las peticiones HTTP y definen los endpoints de la API.

```java
// Ejemplo simplificado de ClienteController.java
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    
    @Autowired
    private ClienteService clienteService;
    
    @GetMapping                             // GET /api/clientes
    public List<Cliente> obtenerTodos() {   // Lista todos los clientes
        return clienteService.buscarTodos();
    }
    
    @GetMapping("/{id}")                    // GET /api/clientes/1
    public Cliente obtenerPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }
    
    @PostMapping                            // POST /api/clientes
    public Cliente crear(@RequestBody Cliente cliente) {
        return clienteService.guardar(cliente);
    }
}
```

### Modelos (Model)
Definen las entidades de datos que se mapean a tablas en la base de datos.

```java
// Ejemplo simplificado de Cliente.java
@Entity                           // Indica que es una entidad JPA
@Table(name = "clientes")         // Mapea a la tabla "clientes" en la BD
public class Cliente {
    
    @Id                           // Clave primaria
    @GeneratedValue               // Auto-incremento
    private Long id;
    
    private String nombre;
    private String email;
    private String telefono;
    
    // Getters y setters
}
```

### Repositorios (Repository)
Interfaces para acceder a la base de datos sin escribir SQL manualmente.

```java
// Ejemplo simplificado de ClienteRepository.java
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    // Métodos predefinidos: findAll(), findById(), save(), delete()...
    
    // Métodos personalizados:
    List<Cliente> findByNombre(String nombre);
}
```

### Servicios (Service)
Implementan la lógica de negocio entre los controladores y los repositorios.

```java
// Ejemplo simplificado de ClienteService.java
@Service
public class ClienteService {
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    // Lógica de negocio
    public List<Cliente> buscarTodos() {
        return clienteRepository.findAll();
    }
    
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }
}
```

## Endpoints de la API

| Método HTTP | Endpoint | Descripción |
|-------------|----------|-------------|
| GET | /api/clientes | Obtener todos los clientes |
| GET | /api/clientes/{id} | Obtener un cliente por su ID |
| POST | /api/clientes | Crear un nuevo cliente |
| PUT | /api/clientes/{id} | Actualizar un cliente existente |
| DELETE | /api/clientes/{id} | Eliminar un cliente |

## Tecnologías utilizadas

- **Java**: Lenguaje de programación
- **Spring Boot**: Framework para crear aplicaciones Java
- **Spring Data JPA**: Para acceso a datos con mínimo código
- **PostgreSQL**: Base de datos relacional
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
   docker build -t ms-cliente .
   docker run -p 8080:8080 ms-cliente
   ```

## Documentación de la API

Cuando el servicio está en ejecución, puedes acceder a la documentación en:
```
http://localhost:8080/swagger-ui.html
```
