import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { PrestamoServicio } from '../../../core/servicios/prestamo.servicio';
import { NotificacionServicio } from '../../../core/servicios/notificacion.servicio';
import { mensajeDeError } from '../../../core/utilidades/mensaje-error';
import { BarraSuperior } from '../../../compartido/barra-superior/barra-superior';
import { EtiquetaEstado } from '../../../compartido/etiqueta-estado/etiqueta-estado';

export const MONTO_MAXIMO = 1_000_000_000;
export const PLAZO_MAXIMO = 360;

@Component({
  selector: 'app-panel-prestamos',
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe, BarraSuperior, EtiquetaEstado],
  templateUrl: './panel-prestamos.html',
  styleUrl: './panel-prestamos.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PanelPrestamos implements OnInit {
  private readonly prestamoServicio = inject(PrestamoServicio);
  private readonly notificaciones = inject(NotificacionServicio);
  private readonly fb = inject(FormBuilder);

  readonly prestamos = this.prestamoServicio.prestamos;
  readonly estadoCarga = this.prestamoServicio.estadoCargaMios;
  readonly resumen = this.prestamoServicio.resumenMios;

  readonly enviando = signal(false);
  readonly mensajeError = signal<string | null>(null);

  readonly plazosSugeridos = [6, 12, 24, 36, 60];
  readonly montoMaximo = MONTO_MAXIMO;
  readonly plazoMaximo = PLAZO_MAXIMO;

  readonly formulario = this.fb.group({
    monto: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(MONTO_MAXIMO),
      Validators.pattern(/^\d+(\.\d{1,2})?$/),
    ]),
    plazoMeses: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(PLAZO_MAXIMO),
      Validators.pattern(/^\d+$/),
    ]),
  });

  get monto() {
    return this.formulario.controls.monto;
  }

  get plazoMeses() {
    return this.formulario.controls.plazoMeses;
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.prestamoServicio.cargarMios().subscribe({ error: () => {} });
  }

  elegirPlazo(meses: number): void {
    this.plazoMeses.setValue(meses);
    this.plazoMeses.markAsTouched();
  }

  enviar(): void {
    if (this.formulario.invalid || this.enviando()) {
      this.formulario.markAllAsTouched();
      return;
    }

    const { monto, plazoMeses } = this.formulario.getRawValue();
    this.enviando.set(true);
    this.mensajeError.set(null);

    this.prestamoServicio.solicitar({ monto: monto!, plazoMeses: plazoMeses! }).subscribe({
      next: () => {
        this.enviando.set(false);
        this.formulario.reset();
        this.notificaciones.exito('Solicitud enviada. Te avisaremos cuando sea revisada.');
      },
      error: (error) => {
        this.enviando.set(false);
        this.mensajeError.set(mensajeDeError(error, 'No se pudo enviar la solicitud.'));
      },
    });
  }
}
