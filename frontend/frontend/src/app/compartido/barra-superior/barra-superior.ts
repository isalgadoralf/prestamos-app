import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { Router } from '@angular/router';

import { AutenticacionServicio } from '../../core/servicios/autenticacion.servicio';

@Component({
  selector: 'app-barra-superior',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="barra">
      <div class="marca">
        <span class="logo" aria-hidden="true">$</span>
        <span>Préstamos</span>
      </div>
      <div class="sesion">
        <div class="saludo">
          <span class="titulo">{{ saludo() }}</span>
          <span class="rol">{{ auth.esAdmin() ? 'Administrador' : 'Cliente' }}</span>
        </div>
        <span class="avatar" aria-hidden="true">{{ inicial() }}</span>
        <button type="button" class="btn btn--fantasma" (click)="cerrarSesion()">Cerrar sesión</button>
      </div>
    </header>
  `,
  styles: `
    .barra {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
      padding: 0.75rem clamp(1rem, 4vw, 2rem);
      background: var(--superficie);
      border-bottom: 1px solid var(--borde);
      position: sticky;
      top: 0;
      z-index: 10;
    }
    .marca { display: flex; align-items: center; gap: 0.5rem; font-weight: 700; }
    .logo {
      display: grid;
      place-items: center;
      width: 2rem;
      height: 2rem;
      border-radius: 8px;
      background: var(--primario);
      color: var(--primario-contraste);
    }
    .sesion { display: flex; align-items: center; gap: 0.75rem; }
    .saludo { display: flex; flex-direction: column; text-align: right; line-height: 1.2; }
    .titulo { font-weight: 600; font-size: 0.9rem; }
    .rol { font-size: 0.75rem; color: var(--texto-suave); }
    .avatar {
      display: grid;
      place-items: center;
      width: 2.1rem;
      height: 2.1rem;
      border-radius: 50%;
      background: var(--acento-fondo);
      color: var(--primario);
      font-weight: 700;
      text-transform: uppercase;
    }
    @media (max-width: 560px) {
      .saludo, .avatar { display: none; }
    }
  `,
})
export class BarraSuperior {
  protected readonly auth = inject(AutenticacionServicio);
  private readonly router = inject(Router);

  readonly prefijo = input('Hola');

  protected readonly saludo = computed(() => {
    const correo = this.auth.usuario()?.correo ?? '';
    return `${this.prefijo()}, ${correo.split('@')[0]}`;
  });
  protected readonly inicial = computed(() => this.auth.usuario()?.correo.charAt(0) ?? '?');

  cerrarSesion(): void {
    this.auth.cerrarSesion();
    this.router.navigateByUrl('/login');
  }
}
