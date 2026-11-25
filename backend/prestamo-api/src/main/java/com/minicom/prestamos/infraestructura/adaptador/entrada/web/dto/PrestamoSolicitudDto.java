package com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PrestamoSolicitudDto(

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero")
        BigDecimal monto,

        @NotNull(message = "El plazo en meses es obligatorio")
        @Min(value = 1, message = "El plazo mínimo es 1 mes")
        @Max(value = 360, message = "El plazo máximo es 360 meses")
        Integer plazoMeses) {
}
