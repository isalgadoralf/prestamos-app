import { HttpErrorResponse } from '@angular/common/http';

import { ErrorApi } from '../modelos/error-api.modelo';

/** Traduce un error HTTP a un mensaje legible, priorizando el mensaje del backend. */
export function mensajeDeError(error: unknown, porDefecto: string): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) return 'No se pudo conectar con el servidor.';
    const cuerpo = error.error as Partial<ErrorApi> | null;
    if (cuerpo && typeof cuerpo.mensaje === 'string' && cuerpo.mensaje) return cuerpo.mensaje;
  }
  return porDefecto;
}
