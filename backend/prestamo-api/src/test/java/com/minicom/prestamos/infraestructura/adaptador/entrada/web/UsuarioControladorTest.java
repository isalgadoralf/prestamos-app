package com.minicom.prestamos.infraestructura.adaptador.entrada.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.minicom.prestamos.dominio.excepcion.CorreoYaRegistradoException;
import com.minicom.prestamos.dominio.excepcion.OperacionNoPermitidaException;
import com.minicom.prestamos.dominio.excepcion.UsuarioNoEncontradoException;
import com.minicom.prestamos.dominio.modelo.Rol;
import com.minicom.prestamos.dominio.modelo.Usuario;
import com.minicom.prestamos.dominio.puerto.entrada.GestionarUsuariosCasoUso;
import com.minicom.prestamos.infraestructura.seguridad.UsuarioAutenticado;

@WebMvcTest(UsuarioControlador.class)
class UsuarioControladorTest {

    private static final UsuarioAutenticado ADMIN = new UsuarioAutenticado(
            Usuario.de(99L, "Admin", "admin@test.com", "x", Rol.ADMIN));

    private static final String CUERPO =
            "{\"nombreCompleto\":\"Nuevo\",\"correo\":\"%s\",\"contrasena\":\"123\",\"rol\":\"USUARIO\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GestionarUsuariosCasoUso usuarios;

    @Test
    void no_expone_la_contrasena() throws Exception {
        when(usuarios.listarTodos()).thenReturn(List.of(
                Usuario.de(1L, "Usuario", "usuario@test.com", "secreto", Rol.USUARIO)));

        mockMvc.perform(get("/api/usuarios").with(user(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("usuario@test.com"))
                .andExpect(jsonPath("$[0].hashContrasena").doesNotExist());
    }

    @Test
    void crear_responde_201() throws Exception {
        when(usuarios.crear("Nuevo", "nuevo@test.com", "123", Rol.USUARIO))
                .thenReturn(Usuario.de(5L, "Nuevo", "nuevo@test.com", "hash", Rol.USUARIO));

        mockMvc.perform(post("/api/usuarios").with(user(ADMIN)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO.formatted("nuevo@test.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void crear_con_correo_invalido_responde_400() throws Exception {
        mockMvc.perform(post("/api/usuarios").with(user(ADMIN)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO.formatted("no-es-correo")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_con_correo_repetido_responde_409() throws Exception {
        when(usuarios.crear(any(), eq("usuario@test.com"), any(), any()))
                .thenThrow(new CorreoYaRegistradoException("ya existe"));

        mockMvc.perform(post("/api/usuarios").with(user(ADMIN)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO.formatted("usuario@test.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void obtener_inexistente_responde_404() throws Exception {
        when(usuarios.obtenerPorId(404L)).thenThrow(new UsuarioNoEncontradoException("no existe"));

        mockMvc.perform(get("/api/usuarios/404").with(user(ADMIN)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_responde_204_y_409_si_tiene_prestamos() throws Exception {
        mockMvc.perform(delete("/api/usuarios/2").with(user(ADMIN)).with(csrf()))
                .andExpect(status().isNoContent());

        doThrow(new OperacionNoPermitidaException("tiene prestamos")).when(usuarios).eliminar(1L, 99L);
        mockMvc.perform(delete("/api/usuarios/1").with(user(ADMIN)).with(csrf()))
                .andExpect(status().isConflict());
    }
}
