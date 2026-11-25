import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';

import { crearJwtFalso } from '../../../testing/jwt-falso';
import { API_URL } from '../config/api.config';
import { AutenticacionServicio } from '../servicios/autenticacion.servicio';
import { NotificacionServicio } from '../servicios/notificacion.servicio';
import { autenticacionInterceptor } from './autenticacion.interceptor';

describe('autenticacionInterceptor', () => {
  let http: HttpClient;
  let controlador: HttpTestingController;
  let router: Router;
  const token = crearJwtFalso({ rol: 'USUARIO' });

  beforeEach(() => {
    localStorage.setItem('prestamos_token', token);
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([autenticacionInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    controlador = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
  });

  afterEach(() => {
    controlador.verify();
    localStorage.clear();
  });

  it('agrega el Bearer token a las peticiones de la API', () => {
    http.get(`${API_URL}/prestamos/mios`).subscribe();
    const req = controlador.expectOne(`${API_URL}/prestamos/mios`);
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${token}`);
    req.flush([]);
  });

  it('no envía el token a dominios externos', () => {
    http.get('https://externo.com/datos').subscribe();
    const req = controlador.expectOne('https://externo.com/datos');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('no envía el token al endpoint de login', () => {
    http.post(`${API_URL}/auth/login`, {}).subscribe({ error: () => {} });
    const req = controlador.expectOne(`${API_URL}/auth/login`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('ante un 401 cierra la sesión y redirige al login', () => {
    const auth = TestBed.inject(AutenticacionServicio);

    http.get(`${API_URL}/prestamos/mios`).subscribe({ error: () => {} });
    controlador
      .expectOne(`${API_URL}/prestamos/mios`)
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(auth.estaAutenticado()).toBe(false);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });

  it('ante un 403 notifica y devuelve al usuario a su panel', () => {
    const notificaciones = TestBed.inject(NotificacionServicio);

    http.post(`${API_URL}/prestamos/1/aprobar`, {}).subscribe({ error: () => {} });
    controlador
      .expectOne(`${API_URL}/prestamos/1/aprobar`)
      .flush(null, { status: 403, statusText: 'Forbidden' });

    expect(notificaciones.notificaciones()[0].tipo).toBe('error');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/prestamos');
  });
});
