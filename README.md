# Gestión de Tareas (Spring Boot Web + API REST)

Este es el proyecto correspondiente a la Entrega 3 de la Unidad 7. Consiste en una aplicación de Gestión de Tareas con una interfaz web construida en Thymeleaf y una API REST completa sobre el mismo modelo de dominio y servicio, utilizando Spring Boot 3.3.x y Java 17.

## Descripción

La aplicación permite administrar una lista de tareas en memoria. Soporta operaciones CRUD completas y filtrado por prioridad y estado de completitud. Se exponen tanto vistas web renderizadas en servidor con HTML/Thymeleaf como endpoints de la API REST que consumen y devuelven JSON. Ambas interfaces comparten el mismo conjunto de datos subyacente de forma segura para hilos.

## Prerrequisitos

- **Java 17** o superior instalado en el sistema.
- **Maven** o utilizar el wrapper `./mvnw` incluido.
- **Navegador web** para interactuar con la interfaz Thymeleaf.
- **cURL / Postman** para probar los endpoints de la API REST.

## Ejecución

1. Clonar el repositorio.
2. Navegar al directorio raíz: `cd AbrilWeb-post1-u7`
3. Ejecutar la aplicación mediante el Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Acceder a la interfaz web en: [http://localhost:8080/tareas](http://localhost:8080/tareas)

## Tabla de Endpoints de la API REST

La ruta base para todos los endpoints es: `http://localhost:8080/api/tareas`

| Método | URL | Código Éxito | Código Error | Descripción |
|---|---|---|---|---|
| `GET` | `/` o `/?prioridad=ALTA&completada=false` | 200 OK | - | Retorna la lista de todas las tareas. Permite filtrado opcional. |
| `GET` | `/{id}` | 200 OK | 404 Not Found | Retorna los detalles de una tarea específica por su ID. |
| `POST` | `/` | 201 Created | 400 Bad Request | Crea una nueva tarea. Retorna encabezado `Location` con la URI de la nueva tarea. Los errores de validación devuelven 400 JSON. |
| `PUT` | `/{id}` | 200 OK | 404 / 400 | Reemplaza completamente una tarea existente. |
| `PATCH` | `/{id}/completar` | 200 OK | 404 Not Found | Actualización parcial: marca la tarea indicada como completada. |
| `DELETE`| `/{id}` | 204 No Content | 404 Not Found | Elimina la tarea especificada. |

## Decisiones de Diseño

1. **Inyección por Constructor (sin `@Autowired`)**: 
   A partir de Spring 4.3, si una clase (`@Controller`, `@Service`, `@RestController`) tiene un solo constructor, Spring inyectará automáticamente las dependencias, por lo que la anotación `@Autowired` sobre los campos se vuelve innecesaria y se desaconseja, ya que el uso de constructores facilita el testing y asegura la inmutabilidad de la dependencia.
2. **Validación de fechas con `@FutureOrPresent`**:
   Se decidió utilizar `@FutureOrPresent` en la propiedad `fechaLimite` en lugar de `@Future`, dado que una tarea puede vencer perfectamente en el transcurso del mismo día en que se crea. Así mismo, esto impide que un usuario pueda guardar ediciones sobre tareas que tengan una fecha límite en el pasado.
3. **POST para Completar/Eliminar en Thymeleaf**:
   Los navegadores HTML5 nativamente solo soportan formularios con métodos GET y POST. Se utilizó POST porque las acciones de "Completar" o "Eliminar" modifican el estado de la aplicación. Usar GET para acciones destructivas o mutables viola el estándar de HTTP sobre métodos idempotentes y seguros.
4. **PATCH vs PUT en la API REST**:
   Se utiliza `PUT` (`PUT /{id}`) para operaciones de reemplazo total de la entidad. Se introdujo `PATCH` (`PATCH /{id}/completar`) de manera específica para la acción de completar una tarea porque es una actualización parcial, que no requiere que el cliente envíe toda la representación (título, descripción, etc.) de vuelta para un simple cambio de estado booleano.
5. **Manejo de Errores de Validación (BindingResult vs `@RestControllerAdvice`)**:
   La vista web Thymeleaf se beneficia del objeto `BindingResult` recibido directamente en los argumentos del método de controlador, permitiendo redirigir al mismo formulario HTML e inyectar los mensajes de error a los campos correspondientes. Para la API REST, este mecanismo no sirve, por lo que se separó la lógica de validación usando un `@RestControllerAdvice` que captura centralizadamente `MethodArgumentNotValidException` y devuelve un mapa JSON estructurado (`{campo: mensaje}`) cuando las peticiones REST no cumplen las validaciones.
6. **Persistencia en Memoria hasta Unidad 8**:
   A falta de una base de datos real (que se verá más adelante), el almacenamiento de las tareas se implementa de manera transitoria utilizando un `ConcurrentSkipListMap` y tipos atómicos (`AtomicLong`) dentro del patrón `@Service` de Spring. Se hizo hilo seguro (thread-safe) porque este servicio Singleton es consultado de manera asincrónica y concurrente tanto por los clientes de la API REST como por los usuarios navegando el HTML.

## Notas Técnicas (Correcciones obligatorias)

- **Fechas Dinámicas**: Se utilizaron fechas futuras dinámicas (`LocalDate.now().plusDays(X)`) en la carga de tareas de prueba y en las pruebas manuales (curl) ya que las fechas estáticas sugeridas (2026-08-xx) ya pasaron o quedarían desactualizadas rápidamente y fallarían el `@FutureOrPresent`.
- **Excepción 404 Personalizada**: Para no mostrar páginas de error 500 feos ante la falta de una tarea al intentar editarla o buscarla, se implementó `TareaNoEncontradaException` con `@ResponseStatus(HttpStatus.NOT_FOUND)`.
- **Seguridad de Hilos en Servicio**: Justificada en la decisión de diseño #6.

## Capturas de Pantalla

Todas las capturas reales de ejecución están alojadas en el directorio `/capturas`.

- `lista-tareas.png`
- `formulario-error.png`
- `formulario-editar.png`
- `postman-get-200.png`
- `postman-post-201.png`
- `postman-post-400.png`
- `postman-patch-200.png`
- `postman-delete-204.png`
- `datos-compartidos.png`
