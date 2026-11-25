package com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto;

import com.minicom.prestamos.dominio.modelo.Rol;
import com.minicom.prestamos.dominio.modelo.Usuario;

public record UsuarioRespuestaDto(Long id, String nombreCompleto, String correo, Rol rol) {

    public static UsuarioRespuestaDto desde(Usuario usuario) {
        return new UsuarioRespuestaDto(usuario.getId(), usuario.getNombreCompleto(), usuario.getCorreo(),
                usuario.getRol());
    }
}
