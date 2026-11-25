package com.minicom.prestamos.dominio.puerto.entrada;

import com.minicom.prestamos.dominio.modelo.Prestamo;

public interface DecidirPrestamoCasoUso {

    Prestamo aprobar(Long prestamoId, Long adminId);
    Prestamo rechazar(Long prestamoId, Long adminId);

}
