package com.minicom.prestamos.infraestructura.adaptador.entrada.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.minicom.prestamos.dominio.modelo.Prestamo;
import com.minicom.prestamos.dominio.puerto.entrada.CancelarPrestamoCasoUso;
import com.minicom.prestamos.dominio.puerto.entrada.ConsultarPrestamoCasoUso;
import com.minicom.prestamos.dominio.puerto.entrada.DecidirPrestamoCasoUso;
import com.minicom.prestamos.dominio.puerto.entrada.SolicitarPrestamoCasoUso;
import com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto.PrestamoRespuestaDto;
import com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto.PrestamoSolicitudDto;
import com.minicom.prestamos.infraestructura.seguridad.UsuarioAutenticado;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
public class PrestamoControlador {

    private final SolicitarPrestamoCasoUso solicitar;
    private final DecidirPrestamoCasoUso decidir;
    private final ConsultarPrestamoCasoUso consultar;
    private final CancelarPrestamoCasoUso cancelar;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrestamoRespuestaDto solicitar(@Valid @RequestBody PrestamoSolicitudDto solicitud,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return PrestamoRespuestaDto.desde(solicitar.solicitarPrestamo(
                usuario.getUsuarioId(), solicitud.monto(), solicitud.plazoMeses()));
    }

    @GetMapping("/mios")
    public List<PrestamoRespuestaDto> listarMios(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return consultar.listarPorUsuario(usuario.getUsuarioId()).stream()
                .map(PrestamoRespuestaDto::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public PrestamoRespuestaDto obtenerPorId(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        Prestamo prestamo = consultar.obtenerPorId(id);
        prestamo.verificarAcceso(usuario.getUsuarioId(), usuario.getUsuario().esAdmin());
        return PrestamoRespuestaDto.desde(prestamo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        cancelar.cancelar(id, usuario.getUsuarioId(), usuario.getUsuario().esAdmin());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PrestamoRespuestaDto> listarTodos() {
        return consultar.listarTodos().stream()
                .map(PrestamoRespuestaDto::desde)
                .toList();
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    public PrestamoRespuestaDto aprobar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado admin) {
        return PrestamoRespuestaDto.desde(decidir.aprobar(id, admin.getUsuarioId()));
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    public PrestamoRespuestaDto rechazar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado admin) {
        return PrestamoRespuestaDto.desde(decidir.rechazar(id, admin.getUsuarioId()));
    }
}
