package com.minicom.prestamos.infraestructura.seguridad;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFiltroAutenticacion extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtServicio jwtServicio;
    private final DetallesUsuarioServicio detallesUsuarioServicio;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest peticion, @NonNull HttpServletResponse respuesta,
            @NonNull FilterChain cadena) throws ServletException, IOException {

        String cabecera = peticion.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith(PREFIJO_BEARER)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String token = cabecera.substring(PREFIJO_BEARER.length());
                UserDetails usuario = detallesUsuarioServicio.loadUserByUsername(jwtServicio.extraerCorreo(token));

                if (jwtServicio.esTokenValido(token, usuario.getUsername())) {
                    var autenticacion = new UsernamePasswordAuthenticationToken(
                            usuario, null, usuario.getAuthorities());
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(peticion));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }

        cadena.doFilter(peticion, respuesta);
    }
}
