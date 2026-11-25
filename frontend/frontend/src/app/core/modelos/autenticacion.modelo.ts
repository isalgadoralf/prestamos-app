export type Rol = 'USUARIO' | 'ADMIN';

export interface LoginSolicitud {
  correo: string;
  contrasena: string;
}

export interface LoginRespuesta {
  token: string;
  correo: string;
  rol: Rol;
}

export interface UsuarioAutenticado {
  id: number;
  correo: string;
  rol: Rol;
}
