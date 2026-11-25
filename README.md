# Sistema de Gestión de Préstamos Bancarios

Prueba técnica Makers: API REST con Spring Boot y front-end en Angular.
Respuestas de la parte teórica en [TEORIA.md](TEORIA.md).

```
backend/base-api/prestamo-api   → API REST (Spring Boot 3.3, Java 21)
frontend/frontend               → Interfaz (Angular 21)
```

## Cómo ejecutar

**Backend** (puerto `8081`)
```bash
cd backend/base-api/prestamo-api
mvn spring-boot:run
mvn test                     # ejecutar pruebas
```
Consola H2: `http://localhost:8081/h2-console` (JDBC URL `jdbc:h2:mem:prestamodb`, usuario `sa`, sin contraseña).

**Frontend** (puerto `4200`)
```bash
cd frontend/frontend
npm install
npm start
```

### Usuarios de prueba
| Rol | Email | Contraseña |
|---|---|---|
| Usuario | usuario@test.com | 123 |
| Administrador | admin@test.com | 123 |

## Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | Público | Devuelve un token JWT |
| POST | `/api/auth/registro` | Público | Registrarse (siempre con rol USUARIO) |
| POST | `/api/prestamos` | Autenticado | Solicitar préstamo (`monto`, `plazoMeses`) |
| GET | `/api/prestamos/mios` | Autenticado | Préstamos del usuario actual |
| GET | `/api/prestamos/{id}` | Dueño o ADMIN | Detalle de un préstamo |
| DELETE | `/api/prestamos/{id}` | Dueño o ADMIN | Cancelar un préstamo **pendiente** |
| GET | `/api/prestamos` | ADMIN | Todos los préstamos |
| POST | `/api/prestamos/{id}/aprobar` | ADMIN | Aprobar préstamo |
| POST | `/api/prestamos/{id}/rechazar` | ADMIN | Rechazar préstamo |
| GET | `/api/usuarios` | ADMIN | Listar usuarios |
| GET | `/api/usuarios/{id}` | ADMIN | Obtener usuario |
| POST | `/api/usuarios` | ADMIN | Crear usuario (con rol) |
| PUT | `/api/usuarios/{id}` | ADMIN | Actualizar usuario (contraseña opcional) |
| DELETE | `/api/usuarios/{id}` | ADMIN | Eliminar usuario |

Enviar el token en el header `Authorization: Bearer <token>`.

## Decisiones técnicas

**Arquitectura hexagonal.** El código se organiza en tres capas:
- `dominio`: modelos (`Prestamo`, `Usuario`), reglas de negocio, excepciones y puertos (interfaces de entrada y salida). No depende de Spring ni de JPA.
- `aplicacion`: servicios que implementan los casos de uso y coordinan los puertos.
- `infraestructura`: adaptadores de entrada (controladores REST, DTOs) y de salida (entidades JPA, repositorios, mapeadores), además de la configuración y la seguridad.

Con esto las reglas de negocio se prueban sin levantar Spring, y la base de datos o la API se pueden cambiar sin tocar el dominio.

**Reglas de negocio en el dominio.** `Prestamo` valida el monto (> 0) y el plazo (1 a 360 meses). Solo un préstamo `PENDIENTE` se puede aprobar, rechazar o cancelar; si ya fue decidido, la API responde `409 Conflict`. Un usuario solo puede ver o cancelar sus propios préstamos (`403` en caso contrario). En usuarios: el correo es único, no se puede eliminar un usuario con préstamos registrados (se conserva el historial) y un admin no puede eliminarse a sí mismo. Las contraseñas se codifican a través de un puerto (`CodificadorContrasenaPuerto`) para que el dominio no dependa de Spring Security.

**Validación en dos niveles.** Hibernate Validator (`@Valid`) en los DTOs rechaza las entradas mal formadas, y el dominio protege sus invariantes aunque el caso de uso se invoque desde otro adaptador.

**Seguridad.** Spring Security con JWT stateless (jjwt) y contraseñas con BCrypt. `@PreAuthorize("hasRole('ADMIN')")` restringe la aprobación, el rechazo y los listados globales. CSRF está deshabilitado porque la API no usa cookies de sesión: el token viaja en un header. CORS solo permite `localhost`.

**Transacciones.** Aprobar y rechazar son `@Transactional`: la lectura, el cambio de estado y el guardado ocurren en una sola unidad atómica. Las consultas usan `readOnly = true`.

**Caché.** Se usa Ehcache 3 vía JCache para `prestamoPorIdCache` y `prestamosUsuarioCache` (TTL de 30 s, 100 entradas). Las cachés se invalidan con `@CacheEvict` al solicitar, aprobar o rechazar, para que el usuario nunca vea un estado viejo. Se eligió Ehcache por ser el recomendado y porque corre embebido, sin infraestructura extra.

**WebFlux.** No se usó. JPA/JDBC es bloqueante, así que mezclarlo con WebFlux no aporta rendimiento real y agrega complejidad. Spring MVC es la opción adecuada para este volumen y modelo de datos. WebFlux tendría sentido con R2DBC o para notificaciones en tiempo real (SSE) del cambio de estado.

**Manejo de errores.** `ManejadorGlobalExcepciones` (`@RestControllerAdvice`) devuelve un JSON uniforme:
`400` validación · `401` credenciales inválidas · `403` sin permisos o recurso ajeno · `404` préstamo/usuario no encontrado · `409` préstamo ya decidido, correo repetido o usuario con préstamos.

**Base de datos.** H2 en memoria, para que el proyecto se pueda probar sin instalar nada. Cambiar a PostgreSQL solo requiere el driver y el `datasource` en `application.yml`.

**Pruebas (JUnit 5 + Spring Boot Starter Test).**
- Dominio: reglas de `Prestamo` (sin Spring).
- Servicio: casos de uso con Mockito.
- Controladores: `@WebMvcTest` con seguridad (roles, códigos HTTP).
- Repositorio: `@DataJpaTest`.
- Caché: verifica que la segunda consulta no llegue al repositorio y que la caché se invalide.
- Integración: flujo completo login → solicitar → aprobar.

**Frontend (Angular).**
- Componentes standalone con rutas lazy (`login`, `prestamos`, `admin`).
- `autenticacionGuardia` y `adminGuardia` protegen las rutas por sesión y por rol.
- Un interceptor HTTP agrega el JWT a cada petición.
- El estado se maneja con servicios (no hace falta NgRx para este alcance).
- Los formularios son Reactive Forms con validaciones.
