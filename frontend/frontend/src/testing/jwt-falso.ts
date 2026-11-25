import { Rol } from '../app/core/modelos/autenticacion.modelo';

function base64Url(objeto: object): string {
  return btoa(JSON.stringify(objeto)).replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_');
}

/** Genera un JWT sin firma válida, suficiente para probar la lectura de claims en el front. */
export function crearJwtFalso(
  opciones: { correo?: string; rol?: Rol; usuarioId?: number; expiraEnSegundos?: number } = {},
): string {
  const ahora = Math.floor(Date.now() / 1000);
  const payload = {
    sub: opciones.correo ?? 'usuario@test.com',
    usuarioId: opciones.usuarioId ?? 1,
    rol: opciones.rol ?? 'USUARIO',
    iat: ahora,
    exp: ahora + (opciones.expiraEnSegundos ?? 3600),
  };
  return `${base64Url({ alg: 'HS256', typ: 'JWT' })}.${base64Url(payload)}.firma`;
}
