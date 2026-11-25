package com.minicom.prestamos.dominio.puerto.entrada;

import com.minicom.prestamos.dominio.modelo.Prestamo;

public interface CancelarPrestamoCasoUso {

    /** Elimina un préstamo PENDIENTE. Solo su dueño o un administrador pueden hacerlo. */
    Prestamo cancelar(Long prestamoId, Long solicitanteId, boolean esAdmin);
}
