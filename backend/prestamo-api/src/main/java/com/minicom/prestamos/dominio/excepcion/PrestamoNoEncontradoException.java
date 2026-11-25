package com.minicom.prestamos.dominio.excepcion;

public class PrestamoNoEncontradoException extends RuntimeException {

    public PrestamoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
