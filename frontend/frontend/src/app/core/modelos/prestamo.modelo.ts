export type EstadoPrestamo = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO';

export interface Prestamo {
  id: number;
  usuarioId: number;
  monto: number;
  plazoMeses: number;
  estado: EstadoPrestamo;
  solicitadoEn: string;
  decididoEn: string | null;
  decididoPor: number | null;
}

export interface PrestamoSolicitud {
  monto: number;
  plazoMeses: number;
}
