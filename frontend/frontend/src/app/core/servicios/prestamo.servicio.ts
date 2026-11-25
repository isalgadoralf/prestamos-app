import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { API_URL } from '../config/api.config';
import { EstadoPrestamo, Prestamo, PrestamoSolicitud } from '../modelos/prestamo.modelo';

export type EstadoCarga = 'inactivo' | 'cargando' | 'listo' | 'error';

type ConteoPorEstado = Record<EstadoPrestamo, number>;

function contar(prestamos: Prestamo[]): ConteoPorEstado {
  const conteo: ConteoPorEstado = { PENDIENTE: 0, APROBADO: 0, RECHAZADO: 0 };
  for (const prestamo of prestamos) conteo[prestamo.estado]++;
  return conteo;
}

function masRecientesPrimero(a: Prestamo, b: Prestamo): number {
  return b.solicitadoEn.localeCompare(a.solicitadoEn) || b.id - a.id;
}

/**
 * Store de préstamos basado en signals. Los componentes solo leen señales de solo lectura;
 * las mutaciones pasan siempre por los métodos del servicio, que sincronizan con la API.
 */
@Injectable({ providedIn: 'root' })
export class PrestamoServicio {
  private readonly http = inject(HttpClient);

  // ---- Préstamos del usuario autenticado ----
  private readonly misPrestamos = signal<Prestamo[]>([]);
  private readonly cargaMios = signal<EstadoCarga>('inactivo');

  readonly prestamos = computed(() => [...this.misPrestamos()].sort(masRecientesPrimero));
  readonly estadoCargaMios = this.cargaMios.asReadonly();
  readonly resumenMios = computed(() => contar(this.misPrestamos()));

  // ---- Todos los préstamos (admin) ----
  private readonly todosPrestamos = signal<Prestamo[]>([]);
  private readonly cargaTodos = signal<EstadoCarga>('inactivo');

  readonly todos = computed(() => [...this.todosPrestamos()].sort(masRecientesPrimero));
  readonly estadoCargaTodos = this.cargaTodos.asReadonly();
  readonly resumenTodos = computed(() => contar(this.todosPrestamos()));

  cargarMios(): Observable<Prestamo[]> {
    this.cargaMios.set('cargando');
    return this.http.get<Prestamo[]>(`${API_URL}/prestamos/mios`).pipe(
      tap({
        next: (prestamos) => {
          this.misPrestamos.set(prestamos);
          this.cargaMios.set('listo');
        },
        error: () => this.cargaMios.set('error'),
      }),
    );
  }

  solicitar(solicitud: PrestamoSolicitud): Observable<Prestamo> {
    return this.http
      .post<Prestamo>(`${API_URL}/prestamos`, solicitud)
      .pipe(tap((prestamo) => this.misPrestamos.update((prestamos) => [prestamo, ...prestamos])));
  }

  cargarTodos(): Observable<Prestamo[]> {
    this.cargaTodos.set('cargando');
    return this.http.get<Prestamo[]>(`${API_URL}/prestamos`).pipe(
      tap({
        next: (prestamos) => {
          this.todosPrestamos.set(prestamos);
          this.cargaTodos.set('listo');
        },
        error: () => this.cargaTodos.set('error'),
      }),
    );
  }

  aprobar(id: number): Observable<Prestamo> {
    return this.http
      .post<Prestamo>(`${API_URL}/prestamos/${id}/aprobar`, {})
      .pipe(tap((prestamo) => this.reemplazarEnTodos(prestamo)));
  }

  rechazar(id: number): Observable<Prestamo> {
    return this.http
      .post<Prestamo>(`${API_URL}/prestamos/${id}/rechazar`, {})
      .pipe(tap((prestamo) => this.reemplazarEnTodos(prestamo)));
  }

  /** Limpia el estado en memoria (p. ej. al cerrar sesión) para no filtrar datos entre usuarios. */
  limpiar(): void {
    this.misPrestamos.set([]);
    this.todosPrestamos.set([]);
    this.cargaMios.set('inactivo');
    this.cargaTodos.set('inactivo');
  }

  private reemplazarEnTodos(actualizado: Prestamo): void {
    this.todosPrestamos.update((prestamos) =>
      prestamos.map((prestamo) => (prestamo.id === actualizado.id ? actualizado : prestamo)),
    );
  }
}
