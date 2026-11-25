package com.minicom.prestamos.dominio.puerto.salida;

import java.util.List;
import java.util.Optional;

import com.minicom.prestamos.dominio.modelo.Prestamo;

public interface PrestamoRepositorioPuerto {

    Prestamo guardar(Prestamo prestamo);

    Optional<Prestamo> buscarPorId(Long id);

    List<Prestamo> buscarPorUsuarioId(Long usuarioId);

    List<Prestamo> buscarTodos();

    boolean existePorUsuarioId(Long usuarioId);

    void eliminar(Long id);
}
