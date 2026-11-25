package com.minicom.prestamos.dominio.puerto.entrada;

import java.math.BigDecimal;

import com.minicom.prestamos.dominio.modelo.Prestamo;

public interface SolicitarPrestamoCasoUso {

    Prestamo solicitarPrestamo(Long usuarioId, BigDecimal monto, int plazoMeses);
}
