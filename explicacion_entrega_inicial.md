# Explicación para la Entrega Inicial

## 📑 Índice

- [Presentación del Enunciado y Motivación](#1-presentación-del-enunciado-y-motivación)
- [DER Completo y Modelos de Datos](#2-der-completo-y-modelos-de-datos)
- [Diseño a Nivel de Contenedor y Microservicios](#3-diseño-a-nivel-de-contenedor-y-microservicios)
- [Recursos, Endpoints, Roles y Datos de Entrada/Respuesta](#4-recursos-endpoints-roles-y-datos-de-entradarespuesta)

---

## 1. Presentación del Enunciado y Motivación
**Responsable:** Hernan

**Qué decir:**
- Presentar el enunciado del proyecto: “El objetivo es desarrollar un sistema de gestión logística de transporte, permitiendo la administración de solicitudes, clientes, contenedores y el seguimiento del estado de los envíos.”
- Explicar la motivación y el alcance: “Buscamos digitalizar y optimizar la logística, mejorando la trazabilidad y la eficiencia operativa.”
- Mencionar la estructura general: “El sistema está basado en microservicios, cada uno con una responsabilidad clara y definida.”

---


## 2. DER Completo y Modelos de Datos
**Responsables:** Hernan y Conti

**Qué decir:**

**Hernan:**
- Introducir el propósito del DER: “El Diagrama de Entidad-Relación es fundamental para definir cómo se estructuran y relacionan los datos en el sistema.”
- Explicar la importancia de identificar correctamente las entidades principales: “En nuestro caso, las entidades clave son Cliente, Solicitud, Contenedor y Estado, cada una representando un aspecto esencial del negocio logístico.”
- Mencionar la necesidad de definir claves primarias y foráneas: “Cada entidad tiene una clave primaria única que la identifica, y las relaciones entre entidades se establecen mediante claves foráneas, lo que garantiza la integridad de los datos.”
- Resaltar cómo el DER permite visualizar el flujo de información y las dependencias entre los distintos microservicios.

**Conti:**
- Presentar el DER definitivo: “En el DER se pueden observar todas las entidades, sus atributos principales y las relaciones entre ellas.”
- Detallar las relaciones más importantes:
	- “Por ejemplo, una Solicitud está asociada a un Cliente y a un Contenedor, y tiene un Estado que indica el progreso del envío.”
	- “El Contenedor puede estar vinculado a varias Solicitudes a lo largo del tiempo, pero cada Solicitud tiene un único Contenedor asignado.”
- Explicar la decisión sobre bases de datos: “Cada microservicio cuenta con su propia base de datos independiente, siguiendo el patrón ‘Database per Service’. Utilizamos PostgreSQL como DBMS en todos los casos.”
- Mencionar cómo las relaciones y claves foráneas permiten consultas eficientes y evitan inconsistencias: “Las claves foráneas aseguran que, por ejemplo, no se pueda crear una Solicitud para un Cliente inexistente.”
- Concluir con la importancia del diseño lógico definitivo: “Este diseño lógico nos permite escalar el sistema, mantener la integridad referencial y adaptar fácilmente los modelos de datos si el negocio evoluciona.”

---

## 3. Diseño a Nivel de Contenedor y Microservicios
**Responsable:** Fran

**Qué decir:**
- Presentar el diseño de contenedores: “En el diagrama de arquitectura se visualizan todos los microservicios y sus relaciones. Cada microservicio corre en su propio contenedor Docker, lo que facilita la orquestación y el despliegue.”
- Enumerar los microservicios: “Tenemos microservicios para gestión de clientes, solicitudes, contenedores y el API Gateway, que centraliza el acceso y la autenticación.”
- Explicar las relaciones: “Los microservicios se comunican entre sí mediante APIs REST y el Gateway gestiona la autenticación con Keycloak.”

---

## 4. Recursos, Endpoints, Roles y Datos de Entrada/Respuesta
**Responsable:** Ale

**Qué decir:**
- Presentar los recursos y endpoints: “Cada microservicio expone endpoints específicos para sus recursos principales. Por ejemplo, el microservicio de clientes permite registrar y consultar clientes, el de solicitudes gestiona la creación y consulta de solicitudes, y el de contenedores administra los contenedores.”
- Explicar los roles y accesos: “El acceso a los endpoints está controlado por roles: cliente, operador y administrador. Cada rol tiene permisos específicos sobre los recursos.”
- Dar ejemplos de datos de entrada y respuesta: “Por ejemplo, para crear una solicitud se envía un JSON con los datos del cliente y el destino, y la respuesta incluye el estado actual del envío.”

---