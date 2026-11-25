import { Injectable, signal } from '@angular/core';

export type TipoNotificacion = 'exito' | 'error' | 'info';

export interface Notificacion {
  id: number;
  tipo: TipoNotificacion;
  mensaje: string;
}

@Injectable({ providedIn: 'root' })
export class NotificacionServicio {
  private secuencia = 0;
  private readonly lista = signal<Notificacion[]>([]);
  readonly notificaciones = this.lista.asReadonly();

  exito(mensaje: string): void {
    this.mostrar('exito', mensaje);
  }

  error(mensaje: string): void {
    this.mostrar('error', mensaje);
  }

  info(mensaje: string): void {
    this.mostrar('info', mensaje);
  }

  descartar(id: number): void {
    this.lista.update((lista) => lista.filter((n) => n.id !== id));
  }

  private mostrar(tipo: TipoNotificacion, mensaje: string, duracionMs = 4000): void {
    const id = ++this.secuencia;
    this.lista.update((lista) => [...lista, { id, tipo, mensaje }]);
    setTimeout(() => this.descartar(id), duracionMs);
  }
}
