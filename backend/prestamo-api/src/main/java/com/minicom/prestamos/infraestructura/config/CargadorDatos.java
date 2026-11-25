package com.minicom.prestamos.infraestructura.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.minicom.prestamos.dominio.modelo.Rol;
import com.minicom.prestamos.dominio.modelo.Usuario;
import com.minicom.prestamos.dominio.puerto.salida.UsuarioRepositorioPuerto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CargadorDatos implements CommandLineRunner {

    private static final String CLAVE_DEMO = "123";

    private final UsuarioRepositorioPuerto usuarioRepositorio;
    private final PasswordEncoder codificadorContrasena;

    @Override
    public void run(String... args) {
        crearSiNoExiste("Usuario Test", "usuario@test.com", Rol.USUARIO);
        crearSiNoExiste("Admin Admin", "admin@test.com", Rol.ADMIN);
    }

    private void crearSiNoExiste(String nombre, String correo, Rol rol) {
        if (usuarioRepositorio.buscarPorCorreo(correo).isPresent()) {
            return;
        }
        usuarioRepositorio.guardar(Usuario.de(null, nombre, correo,
                codificadorContrasena.encode(CLAVE_DEMO), rol));
        log.info("Usuario semilla creado: {} ({})", correo, rol);
    }
}
