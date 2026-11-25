package com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.minicom.prestamos.dominio.modelo.EstadoPrestamo;
import com.minicom.prestamos.dominio.modelo.Prestamo;

public record PrestamoRespuestaDto(
        Long id,
        Long usuarioId,
        BigDecimal monto,
        int plazoMeses,
        EstadoPrestamo estado,
        Instant solicitadoEn,
        Instant decididoEn,
        Long decididoPor) {

    public static PrestamoRespuestaDto desde(Prestamo prestamo) {
        return new PrestamoRespuestaDto(
                prestamo.getId(),
                prestamo.getUsuarioId(),
                prestamo.getMonto(),
                prestamo.getPlazoMeses(),
                prestamo.getEstado(),
                prestamo.getSolicitadoEn(),
                prestamo.getDecididoEn(),
                prestamo.getDecididoPor());
    }
}
