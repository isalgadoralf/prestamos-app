package com.minicom.prestamos.dominio.puerto.entrada;

import java.util.List;

import com.minicom.prestamos.dominio.modelo.Rol;
import com.minicom.prestamos.dominio.modelo.Usuario;

public interface GestionarUsuariosCasoUso {

    List<Usuario> listarTodos();

    Usuario obtenerPorId(Long usuarioId);

    Usuario crear(String nombreCompleto, String correo, String contrasena, Rol rol);

    /** La contraseña es opcional: si viene vacía se conserva la actual. */
    Usuario actualizar(Long usuarioId, String nombreCompleto, String correo, String contrasena, Rol rol);

    void eliminar(Long usuarioId, Long solicitanteId);
}
