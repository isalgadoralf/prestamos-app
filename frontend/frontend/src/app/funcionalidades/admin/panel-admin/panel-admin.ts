import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Observable, finalize } from 'rxjs';

import { PrestamoServicio } from '../../../core/servicios/prestamo.servicio';
import { UsuarioServicio } from '../../../core/servicios/usuario.servicio';
import { NotificacionServicio } from '../../../core/servicios/notificacion.servicio';
import { EstadoPrestamo, Prestamo } from '../../../core/modelos/prestamo.modelo';
import { mensajeDeError } from '../../../core/utilidades/mensaje-error';
import { BarraSuperior } from '../../../compartido/barra-superior/barra-superior';
import { EtiquetaEstado } from '../../../compartido/etiqueta-estado/etiqueta-estado';

export type FiltroEstado = EstadoPrestamo | 'TODOS';

@Component({
  selector: 'app-panel-admin',
  imports: [CurrencyPipe, DatePipe, BarraSuperior, EtiquetaEstado],
  templateUrl: './panel-admin.html',
  styleUrl: './panel-admin.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PanelAdmin implements OnInit {
  private readonly prestamoServicio = inject(PrestamoServicio);
  private readonly usuarioServicio = inject(UsuarioServicio);
  private readonly notificaciones = inject(NotificacionServicio);

  readonly estadoCarga = this.prestamoServicio.estadoCargaTodos;
  readonly resumen = this.prestamoServicio.resumenTodos;

  private readonly correos = signal(new Map<number, string>());

  readonly filtro = signal<FiltroEstado>('PENDIENTE');
  readonly busqueda = signal('');
  readonly procesandoId = signal<number | null>(null);
  readonly confirmandoRechazoId = signal<number | null>(null);

  readonly filtros: { valor: FiltroEstado; texto: string }[] = [
    { valor: 'PENDIENTE', texto: 'Pendientes' },
    { valor: 'APROBADO', texto: 'Aprobados' },
    { valor: 'RECHAZADO', texto: 'Rechazados' },
    { valor: 'TODOS', texto: 'Todos' },
  ];

  readonly montoAprobado = computed(() =>
    this.prestamoServicio
      .todos()
      .filter((p) => p.estado === 'APROBADO')
      .reduce((total, p) => total + Number(p.monto), 0),
  );

  readonly prestamosVisibles = computed(() => {
    const filtro = this.filtro();
    const texto = this.busqueda().trim().toLowerCase();
    return this.prestamoServicio
      .todos()
      .filter((p) => filtro === 'TODOS' || p.estado === filtro)
      .filter((p) => !texto || this.correoDe(p.usuarioId).toLowerCase().includes(texto));
  });

  ngOnInit(): void {
    this.cargar();
    this.usuarioServicio.listarTodos().subscribe({
      next: (usuarios) => this.correos.set(new Map(usuarios.map((u) => [u.id, u.correo]))),
      error: () => {},
    });
  }

  cargar(): void {
    this.prestamoServicio.cargarTodos().subscribe({ error: () => {} });
  }

  conteo(filtro: FiltroEstado): number {
    const resumen = this.resumen();
    return filtro === 'TODOS' ? resumen.PENDIENTE + resumen.APROBADO + resumen.RECHAZADO : resumen[filtro];
  }

  correoDe(usuarioId: number): string {
    return this.correos().get(usuarioId) ?? `usuario #${usuarioId}`;
  }

  alBuscar(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }

  aprobar(prestamo: Prestamo): void {
    this.decidir(prestamo, (id) => this.prestamoServicio.aprobar(id), 'aprobado');
  }

  pedirRechazo(prestamo: Prestamo): void {
    this.confirmandoRechazoId.set(prestamo.id);
  }

  cancelarRechazo(): void {
    this.confirmandoRechazoId.set(null);
  }

  rechazar(prestamo: Prestamo): void {
    this.confirmandoRechazoId.set(null);
    this.decidir(prestamo, (id) => this.prestamoServicio.rechazar(id), 'rechazado');
  }

  private decidir(
    prestamo: Prestamo,
    accion: (id: number) => Observable<Prestamo>,
    resultado: string,
  ): void {
    this.procesandoId.set(prestamo.id);

    accion(prestamo.id)
      .pipe(finalize(() => this.procesandoId.set(null)))
      .subscribe({
        next: () =>
          this.notificaciones.exito(`Préstamo #${prestamo.id} de ${this.correoDe(prestamo.usuarioId)} ${resultado}.`),
        error: (error) => {
          if (error?.status === 403 || error?.status === 401) return; // ya lo notifica el interceptor
          this.notificaciones.error(mensajeDeError(error, 'No se pudo actualizar el préstamo.'));
          // Si otro admin ya lo decidió (409), refrescamos para mostrar el estado real.
          if (error?.status === 409) this.cargar();
        },
      });
  }
}
