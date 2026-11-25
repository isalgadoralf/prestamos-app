package com.minicom.prestamos.infraestructura.seguridad;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.minicom.prestamos.dominio.puerto.salida.UsuarioRepositorioPuerto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DetallesUsuarioServicio implements UserDetailsService {

    private final UsuarioRepositorioPuerto usuarioRepositorio;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        return usuarioRepositorio.buscarPorCorreo(correo)
                .map(UsuarioAutenticado::new)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con correo " + correo));
    }
}
