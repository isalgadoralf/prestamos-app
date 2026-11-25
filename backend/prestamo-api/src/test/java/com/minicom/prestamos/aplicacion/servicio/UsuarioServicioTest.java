package com.minicom.prestamos.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.minicom.prestamos.dominio.excepcion.CorreoYaRegistradoException;
import com.minicom.prestamos.dominio.excepcion.OperacionNoPermitidaException;
import com.minicom.prestamos.dominio.modelo.Rol;
import com.minicom.prestamos.dominio.modelo.Usuario;
import com.minicom.prestamos.dominio.puerto.salida.CodificadorContrasenaPuerto;
import com.minicom.prestamos.dominio.puerto.salida.PrestamoRepositorioPuerto;
import com.minicom.prestamos.dominio.puerto.salida.UsuarioRepositorioPuerto;

class UsuarioServicioTest {

    private final UsuarioRepositorioPuerto usuarios = mock(UsuarioRepositorioPuerto.class);
    private final PrestamoRepositorioPuerto prestamos = mock(PrestamoRepositorioPuerto.class);
    private final CodificadorContrasenaPuerto codificador = contrasena -> "hash:" + contrasena;
    private final UsuarioServicio servicio = new UsuarioServicio(usuarios, prestamos, codificador);

    @Test
    void crea_usuario_con_contrasena_codificada() {
        when(usuarios.buscarPorCorreo("nuevo@test.com")).thenReturn(Optional.empty());
        when(usuarios.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario creado = servicio.crear("Nuevo", "nuevo@test.com", "123", Rol.USUARIO);

        assertThat(creado.getHashContrasena()).isEqualTo("hash:123");
    }

    @Test
    void no_permite_correo_repetido() {
        when(usuarios.buscarPorCorreo("usuario@test.com"))
                .thenReturn(Optional.of(Usuario.de(1L, "U", "usuario@test.com", "h", Rol.USUARIO)));

        assertThatThrownBy(() -> servicio.crear("Otro", "usuario@test.com", "123", Rol.USUARIO))
                .isInstanceOf(CorreoYaRegistradoException.class);
    }

    @Test
    void actualizar_sin_contrasena_conserva_la_actual() {
        Usuario existente = Usuario.de(1L, "U", "usuario@test.com", "hash-viejo", Rol.USUARIO);
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(existente));
        when(usuarios.buscarPorCorreo("usuario@test.com")).thenReturn(Optional.of(existente));
        when(usuarios.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario actualizado = servicio.actualizar(1L, "Nuevo Nombre", "usuario@test.com", null, Rol.ADMIN);

        assertThat(actualizado.getNombreCompleto()).isEqualTo("Nuevo Nombre");
        assertThat(actualizado.getRol()).isEqualTo(Rol.ADMIN);
        assertThat(actualizado.getHashContrasena()).isEqualTo("hash-viejo");
    }

    @Test
    void no_elimina_usuario_con_prestamos() {
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(Usuario.de(1L, "U", "u@t.com", "h", Rol.USUARIO)));
        when(prestamos.existePorUsuarioId(1L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.eliminar(1L, 99L)).isInstanceOf(OperacionNoPermitidaException.class);
        verify(usuarios, never()).eliminar(1L);
    }

    @Test
    void admin_no_se_elimina_a_si_mismo() {
        when(usuarios.buscarPorId(99L)).thenReturn(Optional.of(Usuario.de(99L, "A", "a@t.com", "h", Rol.ADMIN)));

        assertThatThrownBy(() -> servicio.eliminar(99L, 99L)).isInstanceOf(OperacionNoPermitidaException.class);
    }
}
