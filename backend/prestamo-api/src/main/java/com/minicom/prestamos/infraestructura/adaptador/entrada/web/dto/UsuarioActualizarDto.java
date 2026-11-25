package com.minicom.prestamos.infraestructura.adaptador.entrada.web.dto;

import com.minicom.prestamos.dominio.modelo.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** La contraseña es opcional: si no se envía, se conserva la actual. */
public record UsuarioActualizarDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String correo,

        @Size(min = 3, max = 72, message = "La contraseña debe tener entre 3 y 72 caracteres")
        String contrasena,

        @NotNull(message = "El rol es obligatorio")
        Rol rol) {
}
