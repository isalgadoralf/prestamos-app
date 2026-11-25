import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { API_URL } from '../../../core/config/api.config';
import { PanelPrestamos } from './panel-prestamos';

describe('PanelPrestamos', () => {
  let componente: PanelPrestamos;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PanelPrestamos],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    const fixture = TestBed.createComponent(PanelPrestamos);
    componente = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(`${API_URL}/prestamos/mios`).flush([]);
  });

  afterEach(() => http.verify());

  it('el formulario vacío es inválido y no envía nada', () => {
    componente.enviar();

    expect(componente.formulario.invalid).toBe(true);
    expect(componente.monto.touched).toBe(true);
    http.expectNone(`${API_URL}/prestamos`);
  });

  it.each([
    [0, 'min'],
    [-5, 'min'],
    [2_000_000_000, 'max'],
    [10.123, 'pattern'],
  ])('rechaza el monto %s (%s)', (valor, error) => {
    componente.monto.setValue(valor);
    expect(componente.monto.hasError(error)).toBe(true);
  });

  it.each([
    [0, 'min'],
    [361, 'max'],
    [1.5, 'pattern'],
  ])('rechaza el plazo %s (%s)', (valor, error) => {
    componente.plazoMeses.setValue(valor);
    expect(componente.plazoMeses.hasError(error)).toBe(true);
  });

  it('envía una solicitud válida y limpia el formulario', () => {
    componente.formulario.setValue({ monto: 1500.5, plazoMeses: 12 });
    componente.enviar();

    const req = http.expectOne(`${API_URL}/prestamos`);
    expect(req.request.body).toEqual({ monto: 1500.5, plazoMeses: 12 });
    req.flush({
      id: 1,
      usuarioId: 1,
      monto: 1500.5,
      plazoMeses: 12,
      estado: 'PENDIENTE',
      solicitadoEn: '2026-01-01T00:00:00Z',
      decididoEn: null,
      decididoPor: null,
    });

    expect(componente.formulario.value).toEqual({ monto: null, plazoMeses: null });
    expect(componente.prestamos().length).toBe(1);
  });

  it('muestra el mensaje de error del backend', () => {
    componente.formulario.setValue({ monto: 100, plazoMeses: 12 });
    componente.enviar();

    http
      .expectOne(`${API_URL}/prestamos`)
      .flush({ estado: 400, mensaje: 'El plazo mínimo es 1 mes' }, { status: 400, statusText: 'Bad Request' });

    expect(componente.mensajeError()).toBe('El plazo mínimo es 1 mes');
    expect(componente.enviando()).toBe(false);
  });
});
