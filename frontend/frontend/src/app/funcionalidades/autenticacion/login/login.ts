import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { AutenticacionServicio } from '../../../core/servicios/autenticacion.servicio';
import { mensajeDeError } from '../../../core/utilidades/mensaje-error';

interface CuentaDemo {
  etiqueta: string;
  correo: string;
  contrasena: string;
}

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AutenticacionServicio);
  private readonly router = inject(Router);

  readonly cargando = signal(false);
  readonly mensajeError = signal<string | null>(null);
  readonly mostrarContrasena = signal(false);

  readonly cuentasDemo: CuentaDemo[] = [
    { etiqueta: 'Usuario', correo: 'usuario@test.com', contrasena: '123' },
    { etiqueta: 'Administrador', correo: 'admin@test.com', contrasena: '123' },
  ];

  readonly formulario = this.fb.nonNullable.group({
    correo: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    contrasena: ['', [Validators.required, Validators.maxLength(100)]],
  });

  get correo() {
    return this.formulario.controls.correo;
  }

  get contrasena() {
    return this.formulario.controls.contrasena;
  }

  usarCuenta(cuenta: CuentaDemo): void {
    this.formulario.setValue({ correo: cuenta.correo, contrasena: cuenta.contrasena });
    this.mensajeError.set(null);
  }

  enviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.cargando.set(true);
    this.mensajeError.set(null);

    const { correo, contrasena } = this.formulario.getRawValue();
    this.auth.login({ correo: correo.trim().toLowerCase(), contrasena }).subscribe({
      next: () => this.router.navigateByUrl(this.auth.rutaInicio()),
      error: (error: HttpErrorResponse) => {
        this.cargando.set(false);
        this.mensajeError.set(
          error.status === 401
            ? 'Correo o contraseña incorrectos.'
            : mensajeDeError(error, 'No se pudo iniciar sesión.'),
        );
      },
    });
  }
}
