package com.minicom.prestamos.infraestructura.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.minicom.prestamos.dominio.modelo.Prestamo;

@DataJpaTest
@Import(PrestamoRepositorioAdaptador.class)
class PrestamoRepositorioAdaptadorTest {

    @Autowired
    private PrestamoRepositorioAdaptador adaptador;

    @Test
    void guarda_y_lo_encuentra_por_usuario() {
        Prestamo guardado = adaptador.guardar(Prestamo.solicitar(1L, new BigDecimal("1000"), 12, Instant.now()));

        assertThat(guardado.getId()).isNotNull();
        assertThat(adaptador.buscarPorUsuarioId(1L)).hasSize(1);
    }
}
