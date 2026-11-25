package com.minicom.prestamos.infraestructura.adaptador.salida.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.minicom.prestamos.dominio.modelo.Usuario;
import com.minicom.prestamos.dominio.puerto.salida.UsuarioRepositorioPuerto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UsuarioRepositorioAdaptador implements UsuarioRepositorioPuerto {

    private final UsuarioJpaRepositorio jpaRepositorio;

    @Override
    public Usuario guardar(Usuario usuario) {
        return UsuarioMapeador.aDominio(jpaRepositorio.save(UsuarioMapeador.aEntidad(usuario)));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpaRepositorio.findById(id).map(UsuarioMapeador::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return jpaRepositorio.findByCorreo(correo).map(UsuarioMapeador::aDominio);
    }

    @Override
    public List<Usuario> buscarTodos() {
        return jpaRepositorio.findAll().stream().map(UsuarioMapeador::aDominio).toList();
    }

    @Override
    public void eliminar(Long id) {
        jpaRepositorio.deleteById(id);
    }
}
