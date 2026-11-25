package com.minicom.prestamos.dominio.puerto.salida;

import java.util.List;
import java.util.Optional;

import com.minicom.prestamos.dominio.modelo.Usuario;

public interface UsuarioRepositorioPuerto {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorCorreo(String correo);

    List<Usuario> buscarTodos();

    void eliminar(Long id);
}
