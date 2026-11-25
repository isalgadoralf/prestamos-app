# Parte teórica
 
## 1. Optimización en sistemas financieros
 
**Concurrencia**
 
Para evitar que dos transacciones se pisen sobre el mismo saldo usamos `@Version` en JPA (bloqueo optimista). Si otra transacción modificó el registro antes, la segunda falla y no se sobrescribe el dato. Es lo que manejamos actualmente.
 
Para lo que no necesita respuesta inmediata usamos Kafka. Por ejemplo, cuando se completa una transferencia se publica un evento y a partir de eso se envía la notificación, sin frenar la operación principal.
 
**Caché**
 
Usamos Redis, sobre todo para parámetros y datos que se consultan seguido pero cambian poco, y que no son parte de una transacción. Los saldos y movimientos siempre se leen de la base de datos.
 
**Escalabilidad**
 
Usamos JWT. Además de la seguridad, lo importante para escalar es que es stateless: el servidor no guarda sesión, entonces se pueden levantar varias instancias detrás de un balanceador. Si se necesita que el contenido del token no sea legible, se puede cifrar con JWE.
 
## 2. Seguridad en APIs financieras
 
- **Inyección SQL:** JPA ya trabaja con consultas parametrizadas, y nunca se deberían armar consultas concatenando strings. Es una buena práctica básica.
- **CSRF:** como el JWT se envía en el header `Authorization` y no en una cookie, el navegador no lo manda automáticamente, así que la API no queda expuesta a CSRF. Si se usaran cookies, habría que activar la protección CSRF de Spring Security.
- **Fuerza bruta:** configurar rate limiting (en Apache o en el gateway) para limitar la cantidad de peticiones, y bloquear después de varios intentos fallidos de login.
- **Exposición de datos:** nunca devolver en la respuesta datos sensibles del cliente. Usar DTOs en lugar de las entidades y enmascarar datos como el número de cuenta.
También es importante usar siempre HTTPS y no guardar secretos en el código.
 
## 3. Transacciones en sistemas distribuidos
 
Actualmente no he trabajado con transacciones distribuidas, pero el patrón que conozco es **Saga**:
 
1. Se debita la cuenta A y la transferencia queda en `PENDIENTE`.
2. El servicio B acredita la cuenta destino.
3. Si B falla, A reversa el débito y la transferencia queda como `FALLIDA`.
Cada paso tiene que poder repetirse sin duplicar la operación (por ejemplo con un id único de transferencia), porque si hay reintentos no se debe debitar dos veces.
 
## 4. Pruebas unitarias y de integración
 
Las pruebas se hacen por etapas:
 
- **Unitarias:** JUnit 5 con Mockito y AssertJ, sin levantar Spring. Se prueba la lógica de los servicios mockeando sus dependencias.
- **Integración:** levantando Spring, para probar los endpoints HTTP (con MockMvc) y el acceso a datos con JPA.
En un sistema financiero conviene probar sí o sí los casos críticos: montos inválidos, permisos por rol y qué pasa cuando una operación falla a la mitad.
 
## 5. Front-end: estado y autenticación
 
Se usa JWT para la autenticación, y en el token viaja el rol del usuario. Con un interceptor de Angular se agrega el token a cada petición.
 
Los guards de Angular revisan que el usuario tenga el rol correspondiente para entrar a cada ruta; si no lo tiene, no lo deja ingresar. Igual, la validación real siempre la hace el backend, porque el front se puede manipular.
 
Al cerrar sesión se limpia el token y los datos guardados.
 
## 6. Spring Boot
 
### a. `@SpringBootApplication`
 
Es la combinación de tres anotaciones:
 
- `@SpringBootConfiguration`: indica que la clase es de configuración y puede definir beans.
- `@ComponentScan`: escanea las clases anotadas con `@Service`, `@Repository`, `@Controller`, etc., en el paquete de la clase principal y sus subpaquetes.
- `@EnableAutoConfiguration`: carga las configuraciones automáticas que trae Spring Boot según las dependencias del proyecto. Por ejemplo, si agrego `spring-boot-starter-data-jpa`, configura solo el datasource y los repositorios.
### b. Ciclo de vida de un bean
 
1. Spring instancia el bean con su constructor.
2. Inyecta las dependencias.
3. Ejecuta lo marcado con `@PostConstruct` para inicializarlo.
4. El bean queda listo para usarse.
5. Al cerrar el contexto, ejecuta `@PreDestroy` y el método de destrucción.
### c. Personalizar la autoconfiguración
 
Lo primero es cambiar propiedades en el `application.yml` (por ejemplo la conexión a la base de datos). Si eso no alcanza, se puede definir un bean propio, y Spring usa ese en lugar del que trae por defecto (por ejemplo un `PasswordEncoder` o la configuración de seguridad). Y con perfiles (`dev`, `prod`) se cambia la configuración según el entorno sin tocar el código.
 