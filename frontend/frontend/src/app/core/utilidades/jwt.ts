import { Rol } from '../modelos/autenticacion.modelo';

export interface ClaimsJwt {
  sub: string;
  usuarioId: number;
  rol: Rol;
  iat: number;
  exp: number;
}

/**
 * Decodifica el payload de un JWT. No valida la firma (eso es responsabilidad del backend):
 * se usa solo para leer el rol y la expiración sin confiar en datos editables del localStorage.
 */
export function decodificarJwt(token: string): ClaimsJwt | null {
  try {
    const payload = token.split('.')[1];
    if (!payload) return null;
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const bytes = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));
    const claims = JSON.parse(new TextDecoder().decode(bytes)) as ClaimsJwt;
    return claims.sub && claims.rol && claims.exp ? claims : null;
  } catch {
    return null;
  }
}

export function tokenExpirado(claims: ClaimsJwt, ahoraMs = Date.now()): boolean {
  return claims.exp * 1000 <= ahoraMs;
}
