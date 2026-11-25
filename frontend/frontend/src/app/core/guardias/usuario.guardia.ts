import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AutenticacionServicio } from '../servicios/autenticacion.servicio';

/** El panel de solicitud es solo para clientes: un admin se redirige a su panel. */
export const usuarioGuardia: CanActivateFn = () => {
  const auth = inject(AutenticacionServicio);
  const router = inject(Router);

  return auth.esAdmin() ? router.parseUrl('/admin') : true;
};
