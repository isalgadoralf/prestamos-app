package com.minicom.prestamos.infraestructura.seguridad;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto.ErrorRespuestaDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PuntoEntradaAutenticacion implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta,
            AuthenticationException excepcion) throws IOException {
        respuesta.setStatus(HttpStatus.UNAUTHORIZED.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorRespuestaDto cuerpo = new ErrorRespuestaDto(
                Instant.now(), HttpStatus.UNAUTHORIZED.value(), "Unauthorized",
                "Debes iniciar sesión para acceder a este recurso", peticion.getRequestURI());

        respuesta.getWriter().write(objectMapper.writeValueAsString(cuerpo));
    }
}
