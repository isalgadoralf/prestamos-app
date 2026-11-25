package com.minicom.prestamos.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.minicom.prestamos.dominio.modelo.EstadoPrestamo;
import com.minicom.prestamos.dominio.modelo.Prestamo;
import com.minicom.prestamos.dominio.puerto.entrada.ConsultarPrestamoCasoUso;
import com.minicom.prestamos.dominio.puerto.entrada.DecidirPrestamoCasoUso;
import com.minicom.prestamos.dominio.puerto.salida.PrestamoRepositorioPuerto;

@SpringBootTest
class PrestamoCacheTest {

    @Autowired
    private ConsultarPrestamoCasoUso consultar;

    @Autowired
    private DecidirPrestamoCasoUso decidir;

    @MockBean
    private PrestamoRepositorioPuerto repo;

    private Prestamo pendiente(long id) {
        Prestamo p = Prestamo.solicitar(1L, new BigDecimal("1000"), 12, Instant.now());
        p.asignarId(id);
        return p;
    }

    @Test
    void la_segunda_consulta_sale_del_cache() {
        when(repo.buscarPorId(7L)).thenReturn(Optional.of(pendiente(7L)));

        consultar.obtenerPorId(7L);
        consultar.obtenerPorId(7L);

        verify(repo, times(1)).buscarPorId(7L);
    }

    @Test
    void aprobar_limpia_el_cache() {
        when(repo.buscarPorId(8L)).thenReturn(Optional.of(pendiente(8L)));
        when(repo.guardar(any(Prestamo.class))).thenAnswer(i -> i.getArgument(0));

        consultar.obtenerPorId(8L);
        decidir.aprobar(8L, 99L);
        Prestamo despues = consultar.obtenerPorId(8L);

        assertThat(despues.getEstado()).isEqualTo(EstadoPrestamo.APROBADO);
        verify(repo, times(3)).buscarPorId(8L);
    }
}
