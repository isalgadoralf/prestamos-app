package com.minicom.prestamos.dominio.excepcion;

public class PrestamoYaDecididoException extends RuntimeException {

    public PrestamoYaDecididoException(String mensaje) {
        super(mensaje);
    }
}
