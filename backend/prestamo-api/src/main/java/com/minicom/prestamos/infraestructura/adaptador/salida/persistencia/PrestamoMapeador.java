package com.minicom.prestamos.infraestructura.adaptador.salida.persistencia;

import com.minicom.prestamos.dominio.modelo.Prestamo;

final class PrestamoMapeador {

    private PrestamoMapeador() {
    }

    static PrestamoEntidad aEntidad(Prestamo prestamo) {
        return PrestamoEntidad.builder()
                .id(prestamo.getId())
                .usuarioId(prestamo.getUsuarioId())
                .monto(prestamo.getMonto())
                .plazoMeses(prestamo.getPlazoMeses())
                .estado(prestamo.getEstado())
                .solicitadoEn(prestamo.getSolicitadoEn())
                .decididoEn(prestamo.getDecididoEn())
                .decididoPor(prestamo.getDecididoPor())
                .build();
    }

    static Prestamo aDominio(PrestamoEntidad entidad) {
        return Prestamo.reconstruir(entidad.getId(), entidad.getUsuarioId(), entidad.getMonto(),
                entidad.getPlazoMeses(), entidad.getEstado(), entidad.getSolicitadoEn(),
                entidad.getDecididoEn(), entidad.getDecididoPor());
    }
}
