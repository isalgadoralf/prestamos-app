import { crearJwtFalso } from '../../../testing/jwt-falso';
import { decodificarJwt, tokenExpirado } from './jwt';

describe('jwt', () => {
  it('decodifica los claims del token', () => {
    const claims = decodificarJwt(crearJwtFalso({ correo: 'admin@test.com', rol: 'ADMIN', usuarioId: 7 }));
    expect(claims).toMatchObject({ sub: 'admin@test.com', rol: 'ADMIN', usuarioId: 7 });
  });

  it('devuelve null con un token mal formado', () => {
    expect(decodificarJwt('no-es-un-jwt')).toBeNull();
    expect(decodificarJwt('a.@@@.c')).toBeNull();
  });

  it('detecta tokens expirados', () => {
    const vigente = decodificarJwt(crearJwtFalso({ expiraEnSegundos: 60 }))!;
    const vencido = decodificarJwt(crearJwtFalso({ expiraEnSegundos: -1 }))!;
    expect(tokenExpirado(vigente)).toBe(false);
    expect(tokenExpirado(vencido)).toBe(true);
  });
});
