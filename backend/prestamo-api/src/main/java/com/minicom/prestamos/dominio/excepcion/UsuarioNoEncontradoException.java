package com.minicom.prestamos.dominio.excepcion;

public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
