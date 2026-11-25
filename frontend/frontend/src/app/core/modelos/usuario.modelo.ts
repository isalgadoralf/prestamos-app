import { Rol } from './autenticacion.modelo';

export interface Usuario {
  id: number;
  nombreCompleto: string;
  correo: string;
  rol: Rol;
}
