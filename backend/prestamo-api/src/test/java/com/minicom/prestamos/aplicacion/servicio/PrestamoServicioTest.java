package com.minicom.prestamos.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.minicom.prestamos.dominio.excepcion.AccesoDenegadoException;
import com.minicom.prestamos.dominio.excepcion.PrestamoNoEncontradoException;
import com.minicom.prestamos.dominio.excepcion.PrestamoYaDecididoException;
import com.minicom.prestamos.dominio.modelo.Prestamo;
import com.minicom.prestamos.dominio.puerto.salida.PrestamoRepositorioPuerto;

class PrestamoServicioTest {

    private final PrestamoRepositorioPuerto repo = mock(PrestamoRepositorioPuerto.class);
    private final PrestamoServicio servicio = new PrestamoServicio(repo, Clock.systemUTC());

    private Prestamo pendienteDe(Long usuarioId) {
        Prestamo prestamo = Prestamo.solicitar(usuarioId, new BigDecimal("1000"), 12, Instant.now());
        prestamo.asignarId(10L);
        return prestamo;
    }

    @Test
    void falla_al_aprobar_un_prestamo_que_no_existe() {
        when(repo.buscarPorId(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.aprobar(404L, 99L))
                .isInstanceOf(PrestamoNoEncontradoException.class);
    }

    @Test
    void el_dueno_cancela_su_prestamo_pendiente() {
        when(repo.buscarPorId(10L)).thenReturn(Optional.of(pendienteDe(1L)));

        servicio.cancelar(10L, 1L, false);

        verify(repo).eliminar(10L);
    }

    @Test
    void otro_usuario_no_puede_cancelarlo() {
        when(repo.buscarPorId(10L)).thenReturn(Optional.of(pendienteDe(1L)));

        assertThatThrownBy(() -> servicio.cancelar(10L, 2L, false))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(repo, never()).eliminar(anyLong());
    }

    @Test
    void no_se_cancela_un_prestamo_ya_decidido() {
        Prestamo aprobado = pendienteDe(1L);
        aprobado.aprobar(99L, Instant.now());
        when(repo.buscarPorId(10L)).thenReturn(Optional.of(aprobado));

        assertThatThrownBy(() -> servicio.cancelar(10L, 1L, false))
                .isInstanceOf(PrestamoYaDecididoException.class);
    }
}
