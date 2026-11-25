package com.minicom.prestamos.infraestructura.adaptador.salida.seguridad;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.minicom.prestamos.dominio.puerto.salida.CodificadorContrasenaPuerto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CodificadorContrasenaAdaptador implements CodificadorContrasenaPuerto {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String codificar(String contrasenaPlana) {
        return passwordEncoder.encode(contrasenaPlana);
    }
}
