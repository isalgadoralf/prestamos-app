import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { API_URL } from '../config/api.config';
import { AutenticacionServicio } from '../servicios/autenticacion.servicio';
import { NotificacionServicio } from '../servicios/notificacion.servicio';

export const autenticacionInterceptor: HttpInterceptorFn = (solicitud, siguiente) => {
  // El token solo viaja a nuestra API, nunca a dominios de terceros.
  if (!solicitud.url.startsWith(API_URL)) {
    return siguiente(solicitud);
  }

  const auth = inject(AutenticacionServicio);
  const router = inject(Router);
  const notificaciones = inject(NotificacionServicio);

  const esLogin = solicitud.url.startsWith(`${API_URL}/auth/`);
  const token = esLogin ? null : auth.obtenerToken();
  const conToken = token
    ? solicitud.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : solicitud;

  return siguiente(conToken).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !esLogin) {
        auth.cerrarSesion();
        notificaciones.info('Tu sesión expiró. Vuelve a iniciar sesión.');
        router.navigateByUrl('/login');
      } else if (error.status === 403) {
        notificaciones.error('No tienes permisos para realizar esta acción.');
        router.navigateByUrl(auth.rutaInicio());
      }
      return throwError(() => error);
    }),
  );
};
