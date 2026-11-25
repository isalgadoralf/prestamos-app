import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AutenticacionServicio } from '../servicios/autenticacion.servicio';

/** Evita que un usuario con sesión activa vuelva a ver el login. */
export const invitadoGuardia: CanActivateFn = () => {
  const auth = inject(AutenticacionServicio);
  const router = inject(Router);

  return auth.estaAutenticado() ? router.parseUrl(auth.rutaInicio()) : true;
};
