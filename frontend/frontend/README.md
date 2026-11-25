# Préstamos · Frontend (Angular 21)

Interfaz web del sistema de gestión de préstamos bancarios. Consume la API REST de Spring Boot
(`backend/base-api/prestamo-api`) y permite:

- **Usuarios:** solicitar un préstamo (monto y plazo) y consultar el estado de sus préstamos.
- **Administradores:** ver todas las solicitudes, filtrarlas y aprobarlas o rechazarlas.

## Requisitos

- Node.js 20.19+ / 22+ y npm
- Backend corriendo en `http://localhost:8081` (ver README del backend)

## Puesta en marcha

```bash
npm install
npm start            # http://localhost:4200
npm test             # pruebas unitarias (Vitest)
npm run build        # build de producción en dist/
```

La URL de la API se configura en `src/environments/environment*.ts`
(`environment.development.ts` se usa con `ng serve`, `environment.ts` en el build de producción).

### Credenciales de prueba

| Rol           | Correo             | Contraseña |
| ------------- | ------------------ | ---------- |
| Usuario       | `usuario@test.com` | `123`      |
| Administrador | `admin@test.com`   | `123`      |

En la pantalla de login hay botones para autocompletar cada cuenta.

## Estructura

```
src/
├── environments/                 # URL de la API por entorno
├── testing/                      # utilidades de prueba (JWT falso)
└── app/
    ├── core/                     # singletons: lógica transversal, sin UI
    │   ├── config/               # API_URL
    │   ├── guardias/             # autenticacion, admin, usuario, invitado
    │   ├── interceptores/        # JWT + manejo global de 401/403
    │   ├── modelos/              # contratos con la API (DTOs)
    │   ├── servicios/            # stores de estado (signals) + acceso HTTP
    │   └── utilidades/           # decodificación JWT, mensajes de error
    ├── compartido/               # componentes de UI reutilizables
    │   ├── barra-superior/
    │   ├── etiqueta-estado/
    │   └── notificaciones/       # toasts globales
    └── funcionalidades/          # pantallas (lazy loading por ruta)
        ├── autenticacion/login/
        ├── usuario/panel-prestamos/
        └── admin/panel-admin/
```

## Decisiones técnicas

### Estado: servicios + Signals (en lugar de NgRx)

El dominio es pequeño (sesión + dos listas de préstamos), así que NgRx agregaría mucho
código repetitivo (actions/reducers/effects) sin beneficio real. Se usa el patrón
**service-as-store** con Signals de Angular:

- Cada servicio guarda su estado en `signal()` **privadas** y expone solo vistas de lectura
  (`asReadonly()` / `computed()`), así ningún componente puede modificar el estado directamente.
- Las mutaciones pasan por métodos del servicio que primero llaman a la API y después actualizan
  el estado con la respuesta del servidor, que es la fuente de verdad (p. ej. aprobar reemplaza el
  préstamo en la lista con el que devuelve el backend).
- Los datos derivados (ordenamiento, conteos por estado, monto aprobado, filtros) son `computed`,
  así que siempre son coherentes con el estado base.
- Cada lista lleva su estado de carga (`inactivo | cargando | listo | error`) para mostrar
  skeletons, estados vacíos y reintentos sin mensajes engañosos.
- Al cerrar sesión se limpia el estado en memoria para no mostrar datos de otro usuario.

Si la app creciera (más módulos, caché compartida, devtools/time-travel), migrar a
`@ngrx/signals` (SignalStore) sería directo, porque la forma de los stores ya es la misma.

### Autenticación y autorización con JWT

- **Solo se guarda el token** (`localStorage`). El correo, el id y el **rol se derivan de los
  claims del JWT** (`sub`, `usuarioId`, `rol`, `exp`), no de un objeto aparte que el usuario
  podría editar para "ascender" a ADMIN.
- **Se valida la expiración (`exp`)**: un token vencido o malformado se descarta al iniciar la app,
  en cada guard y antes de cada petición; además se programa un cierre de sesión automático al
  vencer.
- El front **no valida la firma** (no debe conocer el secreto). Los guards son una medida de
  UX; **la autorización real la hace Spring Security** en el backend.
- `localStorage` vs cookie `HttpOnly`: la API emite el JWT en el cuerpo de la respuesta, así que se
  guarda en `localStorage` y el riesgo de XSS se mitiga porque Angular escapa todo el contenido
  interpolado y no se usa `innerHTML`. En producción lo ideal sería una cookie
  `HttpOnly; Secure; SameSite=Strict` emitida por el backend junto con un token CSRF.

**Guards funcionales (`CanActivateFn`):**

| Guard                  | Ruta                     | Regla                                                |
| ---------------------- | ------------------------ | ---------------------------------------------------- |
| `autenticacionGuardia` | `/prestamos`, `/admin`   | Requiere un token vigente; si no, lleva a `/login`.  |
| `adminGuardia`         | `/admin`                 | Solo `ADMIN`; un usuario se redirige a `/prestamos`. |
| `usuarioGuardia`       | `/prestamos`             | Un admin se redirige a su panel.                     |
| `invitadoGuardia`      | `/login`                 | Con sesión activa, lleva directo a tu panel.         |

**Interceptor funcional (`HttpInterceptorFn`):**

- Agrega `Authorization: Bearer …` **solo a las URLs de nuestra API** (nunca filtra el token a
  terceros) y nunca al endpoint de login.
- `401`: cierra la sesión, avisa que expiró y redirige al login.
- `403`: avisa que no tienes permisos y te lleva a tu panel.

### Formularios reactivos y validaciones

- Login: correo obligatorio y con formato válido, contraseña obligatoria y longitudes máximas.
- Solicitud: monto obligatorio, mayor a 0, con tope máximo y como mucho 2 decimales; plazo
  entero entre 1 y 360 meses (las mismas reglas que `PrestamoSolicitudDto` en el backend).
- Los errores se muestran por campo al tocarlo o al intentar enviar; los errores de negocio del
  backend (`ErrorRespuestaDto.mensaje`) se muestran tal cual en una alerta.
- Los botones se deshabilitan mientras se envía, para evitar solicitudes duplicadas.

### UI / UX

- Sistema de diseño propio con **design tokens** en CSS (colores, radios, sombras) y
  **modo oscuro automático** (`prefers-color-scheme`).
- Diseño responsive (grid adaptable, panel de marca en el login solo en escritorio).
- Métricas por estado, etiquetas de estado con color, montos con `CurrencyPipe` (locale `es-CO`) y
  fechas con `DatePipe`.
- Panel admin: pestañas por estado con contadores, búsqueda por correo, **confirmación en línea**
  para rechazar (sin `confirm()` nativo) y refresco automático si otro admin ya decidió (409).
- Skeletons de carga, estados vacíos, reintento ante errores y notificaciones (toasts) globales.
- Accesibilidad: `label` asociados, `aria-invalid`, `role="alert"`, `aria-live` en los toasts,
  foco visible y `prefers-reduced-motion`.
- Rendimiento: componentes standalone con `OnPush`, rutas con lazy loading y nuevo control flow
  (`@if`, `@for`).

## Pruebas

`npm test` ejecuta 38 pruebas unitarias con Vitest + `HttpTestingController`:

- `jwt.spec`: decodificación de claims, tokens malformados y expiración.
- `autenticacion.servicio.spec`: login, rol derivado del token, restauración, token
  vencido/manipulado y cierre de sesión.
- `guardias.spec`: los cuatro guards con y sin sesión, por rol.
- `autenticacion.interceptor.spec`: header Bearer, sin token a terceros ni al login, 401 y 403.
- `prestamo.servicio.spec`: carga, orden, resumen, error, solicitar, aprobar y rechazar.
- `panel-prestamos.spec`: validaciones del formulario reactivo, envío y error del backend.
