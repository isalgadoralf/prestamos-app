package com.minicom.prestamos.dominio.modelo;

import java.math.BigDecimal;
import java.time.Instant;

import com.minicom.prestamos.dominio.excepcion.AccesoDenegadoException;
import com.minicom.prestamos.dominio.excepcion.PrestamoYaDecididoException;
import com.minicom.prestamos.dominio.excepcion.SolicitudPrestamoInvalidaException;

import lombok.Getter;

@Getter
public class Prestamo {

    private Long id;
    private final Long usuarioId;
    private final BigDecimal monto;
    private final int plazoMeses;
    private EstadoPrestamo estado;
    private final Instant solicitadoEn;
    private Instant decididoEn;
    private Long decididoPor;

    private Prestamo(Long usuarioId, BigDecimal monto, int plazoMeses, EstadoPrestamo estado, Instant solicitadoEn) {
        this.usuarioId = usuarioId;
        this.monto = monto;
        this.plazoMeses = plazoMeses;
        this.estado = estado;
        this.solicitadoEn = solicitadoEn;
    }

    public static Prestamo solicitar(Long usuarioId, BigDecimal monto, int plazoMeses, Instant ahora) {
        if (monto == null || monto.signum() <= 0) {
            throw new SolicitudPrestamoInvalidaException("El monto debe ser mayor a cero");
        }
        if (plazoMeses < 1 || plazoMeses > 360) {
            throw new SolicitudPrestamoInvalidaException("El plazo debe estar entre 1 y 360 meses");
        }
        return new Prestamo(usuarioId, monto, plazoMeses, EstadoPrestamo.PENDIENTE, ahora);
    }

    public static Prestamo reconstruir(Long id, Long usuarioId, BigDecimal monto, int plazoMeses,
            EstadoPrestamo estado, Instant solicitadoEn, Instant decididoEn, Long decididoPor) {
        Prestamo prestamo = new Prestamo(usuarioId, monto, plazoMeses, estado, solicitadoEn);
        prestamo.id = id;
        prestamo.decididoEn = decididoEn;
        prestamo.decididoPor = decididoPor;
        return prestamo;
    }

    public void aprobar(Long adminId, Instant ahora) {
        asegurarPendiente();
        this.estado = EstadoPrestamo.APROBADO;
        this.decididoPor = adminId;
        this.decididoEn = ahora;
    }

    public void rechazar(Long adminId, Instant ahora) {
        asegurarPendiente();
        this.estado = EstadoPrestamo.RECHAZADO;
        this.decididoPor = adminId;
        this.decididoEn = ahora;
    }

    /** Solo el dueño del préstamo o un administrador pueden verlo o cancelarlo. */
    public void verificarAcceso(Long solicitanteId, boolean esAdmin) {
        if (!esAdmin && !usuarioId.equals(solicitanteId)) {
            throw new AccesoDenegadoException("No tienes acceso al préstamo " + id);
        }
    }

    /** Solo se puede cancelar un préstamo que aún no fue decidido. */
    public void asegurarCancelable() {
        asegurarPendiente();
    }

    private void asegurarPendiente() {
        if (this.estado != EstadoPrestamo.PENDIENTE) {
            throw new PrestamoYaDecididoException("El préstamo " + id + " ya fue decidido (" + estado + ")");
        }
    }

    public void asignarId(Long id) {
        this.id = id;
    }
}
