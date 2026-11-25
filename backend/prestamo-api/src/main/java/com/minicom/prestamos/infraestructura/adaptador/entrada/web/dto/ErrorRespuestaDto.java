package com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto;

import java.time.Instant;

public record ErrorRespuestaDto(
        Instant fecha,
        int estado,
        String error,
        String mensaje,
        String ruta) {
}
