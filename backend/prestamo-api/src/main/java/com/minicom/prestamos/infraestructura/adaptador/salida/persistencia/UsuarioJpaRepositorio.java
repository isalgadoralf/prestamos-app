package com.minicom.prestamos.infraestructura.adaptador.salida.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepositorio extends JpaRepository<UsuarioEntidad, Long> {

    Optional<UsuarioEntidad> findByCorreo(String correo);
}
