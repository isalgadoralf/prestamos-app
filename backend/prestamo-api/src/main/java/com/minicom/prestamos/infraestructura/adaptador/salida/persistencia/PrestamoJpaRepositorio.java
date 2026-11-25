package com.minicom.prestamos.infraestructura.adaptador.salida.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestamoJpaRepositorio extends JpaRepository<PrestamoEntidad, Long> {

    List<PrestamoEntidad> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
