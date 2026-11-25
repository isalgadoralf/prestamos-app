package com.minicom.prestamos.infraestructura.adaptador.entrada.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.minicom.prestamos.dominio.puerto.entrada.GestionarUsuariosCasoUso;
import com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto.UsuarioActualizarDto;
import com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto.UsuarioCrearDto;
import com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto.UsuarioRespuestaDto;
import com.minicom.prestamos.infraestructura.seguridad.UsuarioAutenticado;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UsuarioControlador {

    private final GestionarUsuariosCasoUso usuarios;

    @GetMapping
    public List<UsuarioRespuestaDto> listarTodos() {
        return usuarios.listarTodos().stream()
                .map(UsuarioRespuestaDto::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public UsuarioRespuestaDto obtenerPorId(@PathVariable Long id) {
        return UsuarioRespuestaDto.desde(usuarios.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioRespuestaDto crear(@Valid @RequestBody UsuarioCrearDto dto) {
        return UsuarioRespuestaDto.desde(
                usuarios.crear(dto.nombreCompleto(), dto.correo(), dto.contrasena(), dto.rol()));
    }

    @PutMapping("/{id}")
    public UsuarioRespuestaDto actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioActualizarDto dto) {
        return UsuarioRespuestaDto.desde(
                usuarios.actualizar(id, dto.nombreCompleto(), dto.correo(), dto.contrasena(), dto.rol()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado admin) {
        usuarios.eliminar(id, admin.getUsuarioId());
    }
}
