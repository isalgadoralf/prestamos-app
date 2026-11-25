package com.minicom.prestamos.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.minicom.prestamos.dominio.excepcion.PrestamoYaDecididoException;
import com.minicom.prestamos.dominio.excepcion.SolicitudPrestamoInvalidaException;

class PrestamoTest {

    @Test
    void nace_pendiente() {
        Prestamo prestamo = Prestamo.solicitar(1L, new BigDecimal("1000"), 12, Instant.now());

        assertThat(prestamo.getEstado()).isEqualTo(EstadoPrestamo.PENDIENTE);
    }

    @Test
    void no_se_decide_dos_veces() {
        Prestamo prestamo = Prestamo.solicitar(1L, new BigDecimal("1000"), 12, Instant.now());
        prestamo.aprobar(99L, Instant.now());

        assertThatThrownBy(() -> prestamo.rechazar(99L, Instant.now()))
                .isInstanceOf(PrestamoYaDecididoException.class);
    }

    @Test
    void rechaza_monto_cero() {
        assertThatThrownBy(() -> Prestamo.solicitar(1L, BigDecimal.ZERO, 12, Instant.now()))
                .isInstanceOf(SolicitudPrestamoInvalidaException.class);
    }

    @Test
    void rechaza_plazo_fuera_de_rango() {
        assertThatThrownBy(() -> Prestamo.solicitar(1L, new BigDecimal("500"), 0, Instant.now()))
                .isInstanceOf(SolicitudPrestamoInvalidaException.class);
        assertThatThrownBy(() -> Prestamo.solicitar(1L, new BigDecimal("500"), 361, Instant.now()))
                .isInstanceOf(SolicitudPrestamoInvalidaException.class);
    }
}
