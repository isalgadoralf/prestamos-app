package com.minicom.prestamos.aplicacion.servicio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minicom.prestamos.dominio.excepcion.PrestamoNoEncontradoException;
import com.minicom.prestamos.dominio.modelo.Prestamo;
import com.minicom.prestamos.dominio.puerto.entrada.CancelarPrestamoCasoUso;
import com.minicom.prestamos.dominio.puerto.entrada.ConsultarPrestamoCasoUso;
import com.minicom.prestamos.dominio.puerto.entrada.DecidirPrestamoCasoUso;
import com.minicom.prestamos.dominio.puerto.entrada.SolicitarPrestamoCasoUso;
import com.minicom.prestamos.dominio.puerto.salida.PrestamoRepositorioPuerto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrestamoServicio implements SolicitarPrestamoCasoUso, DecidirPrestamoCasoUso, ConsultarPrestamoCasoUso,
        CancelarPrestamoCasoUso {

    private final PrestamoRepositorioPuerto prestamoRepositorio;
    private final Clock reloj;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "prestamosUsuarioCache", key = "#usuarioId")
    public Prestamo solicitarPrestamo(Long usuarioId, BigDecimal monto, int plazoMeses) {
        return prestamoRepositorio.guardar(Prestamo.solicitar(usuarioId, monto, plazoMeses, Instant.now(reloj)));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "prestamoPorIdCache", key = "#prestamoId"),
            @CacheEvict(cacheNames = "prestamosUsuarioCache", key = "#result.usuarioId")
    })
    public Prestamo aprobar(Long prestamoId, Long adminId) {
        Prestamo prestamo = buscar(prestamoId);
        prestamo.aprobar(adminId, Instant.now(reloj));
        return prestamoRepositorio.guardar(prestamo);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "prestamoPorIdCache", key = "#prestamoId"),
            @CacheEvict(cacheNames = "prestamosUsuarioCache", key = "#result.usuarioId")
    })
    public Prestamo rechazar(Long prestamoId, Long adminId) {
        Prestamo prestamo = buscar(prestamoId);
        prestamo.rechazar(adminId, Instant.now(reloj));
        return prestamoRepositorio.guardar(prestamo);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "prestamoPorIdCache", key = "#prestamoId"),
            @CacheEvict(cacheNames = "prestamosUsuarioCache", key = "#result.usuarioId")
    })
    public Prestamo cancelar(Long prestamoId, Long solicitanteId, boolean esAdmin) {
        Prestamo prestamo = buscar(prestamoId);
        prestamo.verificarAcceso(solicitanteId, esAdmin);
        prestamo.asegurarCancelable();
        prestamoRepositorio.eliminar(prestamoId);
        return prestamo;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "prestamoPorIdCache", key = "#prestamoId")
    public Prestamo obtenerPorId(Long prestamoId) {
        return buscar(prestamoId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "prestamosUsuarioCache", key = "#usuarioId")
    public List<Prestamo> listarPorUsuario(Long usuarioId) {
        return prestamoRepositorio.buscarPorUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> listarTodos() {
        return prestamoRepositorio.buscarTodos();
    }

    private Prestamo buscar(Long prestamoId) {
        return prestamoRepositorio.buscarPorId(prestamoId)
                .orElseThrow(() -> new PrestamoNoEncontradoException("No existe el préstamo con id " + prestamoId));
    }
}
