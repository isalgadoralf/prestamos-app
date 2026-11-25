package com.minicom.prestamos.infraestructura.adaptador.salida.persistencia;

import com.minicom.prestamos.dominio.modelo.Usuario;

final class UsuarioMapeador {

    private UsuarioMapeador() {
    }

    static UsuarioEntidad aEntidad(Usuario usuario) {
        return UsuarioEntidad.builder()
                .id(usuario.getId())
                .nombreCompleto(usuario.getNombreCompleto())
                .correo(usuario.getCorreo())
                .hashContrasena(usuario.getHashContrasena())
                .rol(usuario.getRol())
                .build();
    }

    static Usuario aDominio(UsuarioEntidad entidad) {
        return Usuario.de(entidad.getId(), entidad.getNombreCompleto(), entidad.getCorreo(),
                entidad.getHashContrasena(), entidad.getRol());
    }
}
