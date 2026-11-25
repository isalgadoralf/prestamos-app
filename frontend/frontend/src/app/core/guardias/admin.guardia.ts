import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AutenticacionServicio } from '../servicios/autenticacion.servicio';

export const adminGuardia: CanActivateFn = () => {
  const auth = inject(AutenticacionServicio);
  const router = inject(Router);

  return auth.esAdmin() ? true : router.parseUrl('/prestamos');
};
