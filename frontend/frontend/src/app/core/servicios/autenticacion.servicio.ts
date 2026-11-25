import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { API_URL } from '../config/api.config';
import { LoginRespuesta, LoginSolicitud, UsuarioAutenticado } from '../modelos/autenticacion.modelo';
import { decodificarJwt, tokenExpirado } from '../utilidades/jwt';
import { PrestamoServicio } from './prestamo.servicio';

const CLAVE_TOKEN = 'prestamos_token';

/**
 * Fuente única de verdad de la sesión. Solo se persiste el token: el usuario y el rol se
 * derivan de sus claims, así que editar el localStorage no permite "ascender" a ADMIN
 * (y un token alterado lo rechaza el backend al verificar la firma).
 */
@Injectable({ providedIn: 'root' })
export class AutenticacionServicio {
  private readonly http = inject(HttpClient);
  private readonly prestamos = inject(PrestamoServicio);

  private readonly token = signal<string | null>(null);
  private temporizadorExpiracion: ReturnType<typeof setTimeout> | null = null;

  readonly usuario = computed<UsuarioAutenticado | null>(() => {
    const token = this.token();
    const claims = token ? decodificarJwt(token) : null;
    return claims ? { id: claims.usuarioId, correo: claims.sub, rol: claims.rol } : null;
  });
  readonly esAdmin = computed(() => this.usuario()?.rol === 'ADMIN');

  constructor() {
    this.restaurarSesion();
  }

  login(credenciales: LoginSolicitud): Observable<LoginRespuesta> {
    return this.http
      .post<LoginRespuesta>(`${API_URL}/auth/login`, credenciales)
      .pipe(tap((respuesta) => this.iniciarSesion(respuesta.token)));
  }

  cerrarSesion(): void {
    this.prestamos.limpiar();
    this.token.set(null);
    this.limpiarTemporizador();
    localStorage.removeItem(CLAVE_TOKEN);
  }

  /** Comprueba presencia y vigencia del token; si expiró, cierra la sesión. */
  estaAutenticado(): boolean {
    const token = this.token();
    const claims = token ? decodificarJwt(token) : null;
    if (!claims || tokenExpirado(claims)) {
      if (token) this.cerrarSesion();
      return false;
    }
    return true;
  }

  obtenerToken(): string | null {
    return this.estaAutenticado() ? this.token() : null;
  }

  rutaInicio(): string {
    return this.esAdmin() ? '/admin' : '/prestamos';
  }

  private iniciarSesion(token: string): void {
    const claims = decodificarJwt(token);
    if (!claims || tokenExpirado(claims)) {
      this.cerrarSesion();
      return;
    }
    this.token.set(token);
    localStorage.setItem(CLAVE_TOKEN, token);
    this.programarExpiracion(claims.exp * 1000 - Date.now());
  }

  private restaurarSesion(): void {
    const guardado = localStorage.getItem(CLAVE_TOKEN);
    if (guardado) this.iniciarSesion(guardado);
  }

  private programarExpiracion(ms: number): void {
    this.limpiarTemporizador();
    this.temporizadorExpiracion = setTimeout(() => this.cerrarSesion(), ms);
  }

  private limpiarTemporizador(): void {
    if (this.temporizadorExpiracion) clearTimeout(this.temporizadorExpiracion);
    this.temporizadorExpiracion = null;
  }
}
