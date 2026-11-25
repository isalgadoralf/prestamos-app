package com.minicom.prestamos.dominio.puerto.entrada;

import java.util.List;

import com.minicom.prestamos.dominio.modelo.Prestamo;

public interface ConsultarPrestamoCasoUso {
    Prestamo obtenerPorId(Long prestamoId);
    List<Prestamo> listarPorUsuario(Long usuarioId);
    List<Prestamo> listarTodos();
}
