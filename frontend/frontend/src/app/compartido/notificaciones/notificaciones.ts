import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { NotificacionServicio } from '../../core/servicios/notificacion.servicio';

@Component({
  selector: 'app-notificaciones',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pila" aria-live="polite">
      @for (n of servicio.notificaciones(); track n.id) {
        <div class="toast" [class]="'toast toast--' + n.tipo" role="status">
          <span class="icono" aria-hidden="true">
            {{ n.tipo === 'exito' ? '✓' : n.tipo === 'error' ? '!' : 'i' }}
          </span>
          <span class="texto">{{ n.mensaje }}</span>
          <button type="button" class="cerrar" aria-label="Cerrar" (click)="servicio.descartar(n.id)">
            ×
          </button>
        </div>
      }
    </div>
  `,
  styles: `
    .pila {
      position: fixed;
      right: 1rem;
      bottom: 1rem;
      left: 1rem;
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 0.5rem;
      z-index: 100;
      pointer-events: none;
    }
    .toast {
      pointer-events: auto;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      max-width: 380px;
      width: 100%;
      padding: 0.75rem 0.9rem;
      border-radius: var(--radio);
      background: var(--superficie);
      border: 1px solid var(--borde);
      box-shadow: var(--sombra-lg);
      font-size: 0.9rem;
      animation: entrar 0.25s ease-out;
    }
    .icono {
      flex: none;
      display: grid;
      place-items: center;
      width: 1.5rem;
      height: 1.5rem;
      border-radius: 50%;
      font-weight: 700;
      font-size: 0.8rem;
      color: #fff;
    }
    .toast--exito .icono { background: var(--exito); }
    .toast--error .icono { background: var(--peligro); }
    .toast--info .icono { background: var(--primario); }
    .texto { flex: 1; }
    .cerrar {
      border: none;
      background: none;
      font-size: 1.2rem;
      line-height: 1;
      color: var(--texto-suave);
      cursor: pointer;
    }
    @keyframes entrar {
      from { opacity: 0; transform: translateY(8px); }
      to { opacity: 1; transform: none; }
    }
  `,
})
export class Notificaciones {
  protected readonly servicio = inject(NotificacionServicio);
}
