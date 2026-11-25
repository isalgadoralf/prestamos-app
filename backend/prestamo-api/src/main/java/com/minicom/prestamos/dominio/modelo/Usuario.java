package com.minicom.prestamos.dominio.modelo;

import lombok.Getter;

@Getter
public class Usuario {

    private Long id;
    private String nombreCompleto;
    private String correo;
    private String hashContrasena;
    private Rol rol;

    private Usuario(Long id, String nombreCompleto, String correo, String hashContrasena, Rol rol) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.hashContrasena = hashContrasena;
        this.rol = rol;
    }

    public static Usuario de(Long id, String nombreCompleto, String correo, String hashContrasena, Rol rol) {
        return new Usuario(id, nombreCompleto, correo, hashContrasena, rol);
    }

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    public void actualizarDatos(String nombreCompleto, String correo, Rol rol) {
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.rol = rol;
    }

    public void cambiarContrasena(String hashContrasena) {
        this.hashContrasena = hashContrasena;
    }

    public void asignarId(Long id) {
        this.id = id;
    }
}
