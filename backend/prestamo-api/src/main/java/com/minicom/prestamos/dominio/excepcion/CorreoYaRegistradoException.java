package com.minicom.prestamos.dominio.excepcion;

public class CorreoYaRegistradoException extends RuntimeException {

    public CorreoYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
