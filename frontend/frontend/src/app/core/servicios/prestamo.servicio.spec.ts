import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { API_URL } from '../config/api.config';
import { Prestamo } from '../modelos/prestamo.modelo';
import { PrestamoServicio } from './prestamo.servicio';

function prestamo(parcial: Partial<Prestamo>): Prestamo {
  return {
    id: 1,
    usuarioId: 1,
    monto: 1000,
    plazoMeses: 12,
    estado: 'PENDIENTE',
    solicitadoEn: '2026-01-01T10:00:00Z',
    decididoEn: null,
    decididoPor: null,
    ...parcial,
  };
}

describe('PrestamoServicio', () => {
  let servicio: PrestamoServicio;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(PrestamoServicio);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('carga mis préstamos ordenados del más reciente al más antiguo y calcula el resumen', () => {
    servicio.cargarMios().subscribe();
    expect(servicio.estadoCargaMios()).toBe('cargando');

    http.expectOne(`${API_URL}/prestamos/mios`).flush([
      prestamo({ id: 1, solicitadoEn: '2026-01-01T00:00:00Z', estado: 'APROBADO' }),
      prestamo({ id: 2, solicitadoEn: '2026-03-01T00:00:00Z' }),
    ]);

    expect(servicio.estadoCargaMios()).toBe('listo');
    expect(servicio.prestamos().map((p) => p.id)).toEqual([2, 1]);
    expect(servicio.resumenMios()).toEqual({ PENDIENTE: 1, APROBADO: 1, RECHAZADO: 0 });
  });

  it('marca error si falla la carga', () => {
    servicio.cargarMios().subscribe({ error: () => {} });
    http.expectOne(`${API_URL}/prestamos/mios`).flush(null, { status: 500, statusText: 'Error' });

    expect(servicio.estadoCargaMios()).toBe('error');
  });

  it('agrega la nueva solicitud al estado local', () => {
    servicio.solicitar({ monto: 500, plazoMeses: 6 }).subscribe();

    const req = http.expectOne(`${API_URL}/prestamos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ monto: 500, plazoMeses: 6 });
    req.flush(prestamo({ id: 9, monto: 500, plazoMeses: 6 }));

    expect(servicio.prestamos().map((p) => p.id)).toEqual([9]);
  });

  it('al aprobar reemplaza el préstamo en la lista del admin', () => {
    servicio.cargarTodos().subscribe();
    http.expectOne(`${API_URL}/prestamos`).flush([prestamo({ id: 3 })]);

    servicio.aprobar(3).subscribe();
    http.expectOne(`${API_URL}/prestamos/3/aprobar`).flush(prestamo({ id: 3, estado: 'APROBADO' }));

    expect(servicio.todos()[0].estado).toBe('APROBADO');
    expect(servicio.resumenTodos()).toEqual({ PENDIENTE: 0, APROBADO: 1, RECHAZADO: 0 });
  });

  it('al rechazar llama al endpoint correspondiente', () => {
    servicio.rechazar(4).subscribe();
    const req = http.expectOne(`${API_URL}/prestamos/4/rechazar`);
    expect(req.request.method).toBe('POST');
    req.flush(prestamo({ id: 4, estado: 'RECHAZADO' }));
  });
});
