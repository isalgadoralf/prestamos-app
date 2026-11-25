package com.minicom.prestamos.infraestructura.adaptador.entrada.web;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.minicom.prestamos.dominio.excepcion.AccesoDenegadoException;
import com.minicom.prestamos.dominio.excepcion.CorreoYaRegistradoException;
import com.minicom.prestamos.dominio.excepcion.OperacionNoPermitidaException;
import com.minicom.prestamos.dominio.excepcion.PrestamoNoEncontradoException;
import com.minicom.prestamos.dominio.excepcion.UsuarioNoEncontradoException;
import com.minicom.prestamos.dominio.excepcion.PrestamoYaDecididoException;
import com.minicom.prestamos.dominio.excepcion.SolicitudPrestamoInvalidaException;
import com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto.ErrorRespuestaDto;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(SolicitudPrestamoInvalidaException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarSolicitudInvalida(SolicitudPrestamoInvalidaException ex,
            HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), peticion);
    }

    @ExceptionHandler({ PrestamoNoEncontradoException.class, UsuarioNoEncontradoException.class })
    public ResponseEntity<ErrorRespuestaDto> manejarNoEncontrado(RuntimeException ex,
            HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage(), peticion);
    }

    @ExceptionHandler({ PrestamoYaDecididoException.class, CorreoYaRegistradoException.class,
            OperacionNoPermitidaException.class })
    public ResponseEntity<ErrorRespuestaDto> manejarConflicto(RuntimeException ex,
            HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage(), peticion);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarAccesoAlRecurso(AccesoDenegadoException ex,
            HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.FORBIDDEN, ex.getMessage(), peticion);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarValidacion(MethodArgumentNotValidException ex,
            HttpServletRequest peticion) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construirRespuesta(HttpStatus.BAD_REQUEST, mensaje, peticion);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarAutenticacion(AuthenticationException ex,
            HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", peticion);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarAccesoDenegado(AccessDeniedException ex,
            HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.FORBIDDEN, "No tienes permisos para esta acción", peticion);
    }

    private ResponseEntity<ErrorRespuestaDto> construirRespuesta(HttpStatus estado, String mensaje,
            HttpServletRequest peticion) {
        ErrorRespuestaDto cuerpo = new ErrorRespuestaDto(
                Instant.now(), estado.value(), estado.getReasonPhrase(), mensaje, peticion.getRequestURI());
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
