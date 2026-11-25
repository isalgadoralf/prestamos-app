package com.minicom.prestamos.aplicacion.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minicom.prestamos.dominio.excepcion.CorreoYaRegistradoException;
import com.minicom.prestamos.dominio.excepcion.OperacionNoPermitidaException;
import com.minicom.prestamos.dominio.excepcion.UsuarioNoEncontradoException;
import com.minicom.prestamos.dominio.modelo.Rol;
import com.minicom.prestamos.dominio.modelo.Usuario;
import com.minicom.prestamos.dominio.puerto.entrada.GestionarUsuariosCasoUso;
import com.minicom.prestamos.dominio.puerto.salida.CodificadorContrasenaPuerto;
import com.minicom.prestamos.dominio.puerto.salida.PrestamoRepositorioPuerto;
import com.minicom.prestamos.dominio.puerto.salida.UsuarioRepositorioPuerto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServicio implements GestionarUsuariosCasoUso {

    private final UsuarioRepositorioPuerto usuarioRepositorio;
    private final PrestamoRepositorioPuerto prestamoRepositorio;
    private final CodificadorContrasenaPuerto codificador;

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepositorio.buscarTodos();
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long usuarioId) {
        return buscar(usuarioId);
    }

    @Override
    @Transactional
    public Usuario crear(String nombreCompleto, String correo, String contrasena, Rol rol) {
        asegurarCorreoLibre(correo, null);
        return usuarioRepositorio.guardar(
                Usuario.de(null, nombreCompleto, correo, codificador.codificar(contrasena), rol));
    }

    @Override
    @Transactional
    public Usuario actualizar(Long usuarioId, String nombreCompleto, String correo, String contrasena, Rol rol) {
        Usuario usuario = buscar(usuarioId);
        asegurarCorreoLibre(correo, usuarioId);
        usuario.actualizarDatos(nombreCompleto, correo, rol);
        if (contrasena != null && !contrasena.isBlank()) {
            usuario.cambiarContrasena(codificador.codificar(contrasena));
        }
        return usuarioRepositorio.guardar(usuario);
    }

    @Override
    @Transactional
    public void eliminar(Long usuarioId, Long solicitanteId) {
        buscar(usuarioId);
        if (usuarioId.equals(solicitanteId)) {
            throw new OperacionNoPermitidaException("No puedes eliminar tu propio usuario");
        }
        if (prestamoRepositorio.existePorUsuarioId(usuarioId)) {
            throw new OperacionNoPermitidaException("El usuario " + usuarioId + " tiene préstamos registrados");
        }
        usuarioRepositorio.eliminar(usuarioId);
    }

    private Usuario buscar(Long usuarioId) {
        return usuarioRepositorio.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("No existe el usuario con id " + usuarioId));
    }

    private void asegurarCorreoLibre(String correo, Long usuarioIdActual) {
        usuarioRepositorio.buscarPorCorreo(correo)
                .filter(existente -> !existente.getId().equals(usuarioIdActual))
                .ifPresent(existente -> {
                    throw new CorreoYaRegistradoException("El correo " + correo + " ya está registrado");
                });
    }
}
