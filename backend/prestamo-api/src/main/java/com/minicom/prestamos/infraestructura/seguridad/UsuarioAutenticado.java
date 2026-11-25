package com.minicom.prestamos.infraestructura.seguridad;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.minicom.prestamos.dominio.modelo.Usuario;

import lombok.Getter;

@Getter
public class UsuarioAutenticado implements UserDetails {

    private final Usuario usuario;

    public UsuarioAutenticado(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getUsuarioId() {
        return usuario.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getHashContrasena();
    }

    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }
}
