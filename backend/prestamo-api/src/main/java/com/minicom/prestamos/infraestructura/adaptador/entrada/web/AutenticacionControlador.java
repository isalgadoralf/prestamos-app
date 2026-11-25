package com.minicom.prestamos.infraestructura.adaptador.entrada.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.minicom.prestamos.dominio.modelo.Rol;
import com.minicom.prestamos.dominio.puerto.entrada.GestionarUsuariosCasoUso;
import com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto.LoginRespuestaDto;
import com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto.LoginSolicitudDto;
import com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto.RegistroSolicitudDto;
import com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto.UsuarioRespuestaDto;
import com.minicom.prestamos.infraestructura.seguridad.JwtServicio;
import com.minicom.prestamos.infraestructura.seguridad.UsuarioAutenticado;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacionControlador {

    private final AuthenticationManager gestorAutenticacion;
    private final JwtServicio jwtServicio;
    private final GestionarUsuariosCasoUso usuarios;

    @PostMapping("/login")
    public ResponseEntity<LoginRespuestaDto> login(@Valid @RequestBody LoginSolicitudDto solicitud) {
        Authentication autenticacion = gestorAutenticacion.authenticate(
                new UsernamePasswordAuthenticationToken(solicitud.correo(), solicitud.contrasena()));

        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();

        return ResponseEntity.ok(new LoginRespuestaDto(
                jwtServicio.generarToken(usuario), usuario.getUsername(), usuario.getUsuario().getRol().name()));
    }

    /** Registro público: siempre crea usuarios con rol USUARIO. */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioRespuestaDto registrar(@Valid @RequestBody RegistroSolicitudDto solicitud) {
        return UsuarioRespuestaDto.desde(usuarios.crear(
                solicitud.nombreCompleto(), solicitud.correo(), solicitud.contrasena(), Rol.USUARIO));
    }
}
