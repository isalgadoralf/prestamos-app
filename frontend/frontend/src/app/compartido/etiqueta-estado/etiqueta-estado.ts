import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

import { EstadoPrestamo } from '../../core/modelos/prestamo.modelo';

const TEXTOS: Record<EstadoPrestamo, string> = {
  PENDIENTE: 'Pendiente',
  APROBADO: 'Aprobado',
  RECHAZADO: 'Rechazado',
};

@Component({
  selector: 'app-etiqueta-estado',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="etiqueta" [class]="'etiqueta etiqueta--' + estado().toLowerCase()">{{ texto() }}</span>`,
  styles: `
    .etiqueta {
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
      padding: 0.2rem 0.6rem;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 600;
      white-space: nowrap;
    }
    .etiqueta::before {
      content: '';
      width: 0.45rem;
      height: 0.45rem;
      border-radius: 50%;
      background: currentColor;
    }
    .etiqueta--pendiente { background: var(--aviso-fondo); color: var(--aviso); }
    .etiqueta--aprobado { background: var(--exito-fondo); color: var(--exito); }
    .etiqueta--rechazado { background: var(--peligro-fondo); color: var(--peligro); }
  `,
})
export class EtiquetaEstado {
  readonly estado = input.required<EstadoPrestamo>();
  protected readonly texto = computed(() => TEXTOS[this.estado()]);
}
