import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { crearJwtFalso } from '../../../testing/jwt-falso';
import { API_URL } from '../config/api.config';
import { AutenticacionServicio } from './autenticacion.servicio';

describe('AutenticacionServicio', () => {
  let http: HttpTestingController;

  function crearServicio(): AutenticacionServicio {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    return TestBed.inject(AutenticacionServicio);
  }

  beforeEach(() => localStorage.clear());
  afterEach(() => http?.verify());

  it('inicia sesión, persiste solo el token y deriva el rol de los claims', () => {
    const servicio = crearServicio();
    const token = crearJwtFalso({ correo: 'admin@test.com', rol: 'ADMIN', usuarioId: 2 });

    servicio.login({ correo: 'admin@test.com', contrasena: '123' }).subscribe();
    const req = http.expectOne(`${API_URL}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ token, correo: 'admin@test.com', rol: 'ADMIN' });

    expect(servicio.estaAutenticado()).toBe(true);
    expect(servicio.esAdmin()).toBe(true);
    expect(servicio.usuario()).toEqual({ id: 2, correo: 'admin@test.com', rol: 'ADMIN' });
    expect(servicio.rutaInicio()).toBe('/admin');
    expect(localStorage.getItem('prestamos_token')).toBe(token);
  });

  it('restaura una sesión vigente desde localStorage', () => {
    localStorage.setItem('prestamos_token', crearJwtFalso({ rol: 'USUARIO' }));
    const servicio = crearServicio();

    expect(servicio.estaAutenticado()).toBe(true);
    expect(servicio.rutaInicio()).toBe('/prestamos');
  });

  it('descarta un token expirado guardado', () => {
    localStorage.setItem('prestamos_token', crearJwtFalso({ expiraEnSegundos: -10 }));
    const servicio = crearServicio();

    expect(servicio.estaAutenticado()).toBe(false);
    expect(servicio.usuario()).toBeNull();
    expect(localStorage.getItem('prestamos_token')).toBeNull();
  });

  it('descarta un token manipulado que no se puede decodificar', () => {
    localStorage.setItem('prestamos_token', 'basura');
    const servicio = crearServicio();

    expect(servicio.estaAutenticado()).toBe(false);
    expect(servicio.obtenerToken()).toBeNull();
  });

  it('cierra sesión limpiando el estado', () => {
    localStorage.setItem('prestamos_token', crearJwtFalso());
    const servicio = crearServicio();

    servicio.cerrarSesion();

    expect(servicio.estaAutenticado()).toBe(false);
    expect(servicio.usuario()).toBeNull();
    expect(localStorage.getItem('prestamos_token')).toBeNull();
  });
});
