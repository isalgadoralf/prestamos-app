import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';

import { crearJwtFalso } from '../../../testing/jwt-falso';
import { Rol } from '../modelos/autenticacion.modelo';
import { adminGuardia } from './admin.guardia';
import { autenticacionGuardia } from './autenticacion.guardia';
import { invitadoGuardia } from './invitado.guardia';
import { usuarioGuardia } from './usuario.guardia';

function ejecutar(guardia: CanActivateFn): boolean | string {
  const resultado = TestBed.runInInjectionContext(() =>
    guardia({} as ActivatedRouteSnapshot, { url: '/x' } as RouterStateSnapshot),
  );
  return resultado instanceof UrlTree ? TestBed.inject(Router).serializeUrl(resultado) : (resultado as boolean);
}

function conSesion(rol: Rol | null): void {
  localStorage.clear();
  if (rol) localStorage.setItem('prestamos_token', crearJwtFalso({ rol }));
  TestBed.configureTestingModule({
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
}

describe('Guardias de rutas', () => {
  afterEach(() => localStorage.clear());

  describe('autenticacionGuardia', () => {
    it('redirige a /login sin sesión', () => {
      conSesion(null);
      expect(ejecutar(autenticacionGuardia)).toBe('/login');
    });

    it('permite el acceso con sesión vigente', () => {
      conSesion('USUARIO');
      expect(ejecutar(autenticacionGuardia)).toBe(true);
    });
  });

  describe('adminGuardia', () => {
    it('bloquea a un usuario normal y lo envía a /prestamos', () => {
      conSesion('USUARIO');
      expect(ejecutar(adminGuardia)).toBe('/prestamos');
    });

    it('permite el acceso a un administrador', () => {
      conSesion('ADMIN');
      expect(ejecutar(adminGuardia)).toBe(true);
    });
  });

  describe('usuarioGuardia', () => {
    it('envía al admin a su panel', () => {
      conSesion('ADMIN');
      expect(ejecutar(usuarioGuardia)).toBe('/admin');
    });

    it('permite el acceso a un usuario', () => {
      conSesion('USUARIO');
      expect(ejecutar(usuarioGuardia)).toBe(true);
    });
  });

  describe('invitadoGuardia', () => {
    it('permite ver el login sin sesión', () => {
      conSesion(null);
      expect(ejecutar(invitadoGuardia)).toBe(true);
    });

    it('redirige a quien ya tiene sesión a su panel', () => {
      conSesion('ADMIN');
      expect(ejecutar(invitadoGuardia)).toBe('/admin');
    });
  });
});
