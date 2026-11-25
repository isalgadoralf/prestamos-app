import { Routes } from '@angular/router';

import { autenticacionGuardia } from './core/guardias/autenticacion.guardia';
import { adminGuardia } from './core/guardias/admin.guardia';
import { invitadoGuardia } from './core/guardias/invitado.guardia';
import { usuarioGuardia } from './core/guardias/usuario.guardia';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    title: 'Iniciar sesión · Préstamos',
    canActivate: [invitadoGuardia],
    loadComponent: () => import('./funcionalidades/autenticacion/login/login').then((m) => m.Login),
  },
  {
    path: 'prestamos',
    title: 'Mis préstamos · Préstamos',
    canActivate: [autenticacionGuardia, usuarioGuardia],
    loadComponent: () =>
      import('./funcionalidades/usuario/panel-prestamos/panel-prestamos').then((m) => m.PanelPrestamos),
  },
  {
    path: 'admin',
    title: 'Gestión de préstamos · Préstamos',
    canActivate: [autenticacionGuardia, adminGuardia],
    loadComponent: () =>
      import('./funcionalidades/admin/panel-admin/panel-admin').then((m) => m.PanelAdmin),
  },
  { path: '**', redirectTo: 'login' },
];
