package com.minicom.prestamos.infraestructura.adaptador.entrada.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.minicom.prestamos.dominio.excepcion.PrestamoNoEncontradoException;
import com.minicom.prestamos.dominio.modelo.Prestamo;
import com.minicom.prestamos.dominio.modelo.Rol;
import com.minicom.prestamos.dominio.modelo.Usuario;
import com.minicom.prestamos.dominio.puerto.entrada.CancelarPrestamoCasoUso;
import com.minicom.prestamos.dominio.puerto.entrada.ConsultarPrestamoCasoUso;
import com.minicom.prestamos.dominio.puerto.entrada.DecidirPrestamoCasoUso;
import com.minicom.prestamos.dominio.puerto.entrada.SolicitarPrestamoCasoUso;
import com.minicom.prestamos.infraestructura.seguridad.UsuarioAutenticado;

@WebMvcTest(PrestamoControlador.class)
class PrestamoControladorTest {

    private static final UsuarioAutenticado USUARIO = new UsuarioAutenticado(
            Usuario.de(1L, "Usuario", "usuario@test.com", "x", Rol.USUARIO));
    private static final UsuarioAutenticado ADMIN = new UsuarioAutenticado(
            Usuario.de(99L, "Admin", "admin@test.com", "x", Rol.ADMIN));

    @Autowired
    MockMvc mvc;

    @MockBean
    SolicitarPrestamoCasoUso solicitar;

    @MockBean
    DecidirPrestamoCasoUso decidir;

    @MockBean
    ConsultarPrestamoCasoUso consultar;

    @MockBean
    CancelarPrestamoCasoUso cancelar;

    @Test
    void usuario_no_ve_el_prestamo_de_otro_403() throws Exception {
        Prestamo ajeno = Prestamo.solicitar(2L, new BigDecimal("500"), 6, Instant.now());
        ajeno.asignarId(7L);
        when(consultar.obtenerPorId(7L)).thenReturn(ajeno);

        mvc.perform(get("/api/prestamos/7").with(user(USUARIO)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/prestamos/7").with(user(ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void cancelar_responde_204() throws Exception {
        mvc.perform(delete("/api/prestamos/7").with(user(USUARIO)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Nested
    class Solicitar {

        @Test
        void responde_201_si_es_valido() throws Exception {
            when(solicitar.solicitarPrestamo(eq(1L), any(BigDecimal.class), anyInt()))
                    .thenReturn(Prestamo.solicitar(1L, new BigDecimal("1000"), 12, Instant.now()));

            mvc.perform(post("/api/prestamos").with(user(USUARIO)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"monto\":1000,\"plazoMeses\":12}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                    .andExpect(jsonPath("$.usuarioId").value(1));
        }

        @Test
        void responde_400_con_monto_negativo() throws Exception {
            mvc.perform(post("/api/prestamos").with(user(USUARIO)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"monto\":-100,\"plazoMeses\":12}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void responde_400_sin_plazo() throws Exception {
            mvc.perform(post("/api/prestamos").with(user(USUARIO)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"monto\":100}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void aprobar_uno_inexistente_da_404() throws Exception {
        when(decidir.aprobar(5L, 99L)).thenThrow(new PrestamoNoEncontradoException("no existe"));

        mvc.perform(post("/api/prestamos/5/aprobar").with(user(ADMIN)).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
