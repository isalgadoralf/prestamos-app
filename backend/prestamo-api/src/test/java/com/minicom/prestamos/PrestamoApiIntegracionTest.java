package com.minicom.prestamos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PrestamoApiIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sin_token_da_401() throws Exception {
        mockMvc.perform(get("/api/prestamos/mios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deja_pasar_cors_del_front() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:4201")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4201"));
    }

    @Test
    void login_con_clave_mala_da_401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"usuario@test.com\",\"contrasena\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuario_no_ve_todos_los_prestamos() throws Exception {
        mockMvc.perform(get("/api/prestamos").header("Authorization", "Bearer " + login("usuario@test.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuario_no_ve_la_lista_de_usuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + login("usuario@test.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuario_no_puede_crear_usuarios() throws Exception {
        mockMvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + login("usuario@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":\"X\",\"correo\":\"x@test.com\",\"contrasena\":\"123\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_ve_los_usuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + login("admin@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.correo == 'admin@test.com')]").exists());
    }

    @Test
    void usuario_pide_y_admin_aprueba() throws Exception {
        String tokenUsuario = login("usuario@test.com");
        String tokenAdmin = login("admin@test.com");

        String respuesta = mockMvc.perform(post("/api/prestamos")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":5000,\"plazoMeses\":24}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn().getResponse().getContentAsString();

        long prestamoId = objectMapper.readTree(respuesta).get("id").asLong();

        mockMvc.perform(post("/api/prestamos/" + prestamoId + "/aprobar")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"));
    }

    @Test
    void registro_publico_y_no_ve_prestamos_ajenos() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":\"Otro\",\"correo\":\"otro@test.com\",\"contrasena\":\"123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("USUARIO"));

        String respuesta = mockMvc.perform(post("/api/prestamos")
                        .header("Authorization", "Bearer " + login("usuario@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":1000,\"plazoMeses\":12}"))
                .andReturn().getResponse().getContentAsString();
        long prestamoId = objectMapper.readTree(respuesta).get("id").asLong();

        mockMvc.perform(get("/api/prestamos/" + prestamoId)
                        .header("Authorization", "Bearer " + login("otro@test.com")))
                .andExpect(status().isForbidden());
    }

    private String login(String correo) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(respuesta).get("token").asText();
    }
}
