package com.minicom.prestamos.dominio.excepcion;

public class SolicitudPrestamoInvalidaException extends RuntimeException {

    public SolicitudPrestamoInvalidaException(String mensaje) {
        super(mensaje);
    }
}
