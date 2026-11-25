package com.minicom.prestamos.infraestructura.adaptador.salida.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.minicom.prestamos.dominio.modelo.Prestamo;
import com.minicom.prestamos.dominio.puerto.salida.PrestamoRepositorioPuerto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PrestamoRepositorioAdaptador implements PrestamoRepositorioPuerto {

    private final PrestamoJpaRepositorio jpaRepositorio;

    @Override
    public Prestamo guardar(Prestamo prestamo) {
        return PrestamoMapeador.aDominio(jpaRepositorio.save(PrestamoMapeador.aEntidad(prestamo)));
    }

    @Override
    public Optional<Prestamo> buscarPorId(Long id) {
        return jpaRepositorio.findById(id).map(PrestamoMapeador::aDominio);
    }

    @Override
    public List<Prestamo> buscarPorUsuarioId(Long usuarioId) {
        return jpaRepositorio.findByUsuarioId(usuarioId).stream().map(PrestamoMapeador::aDominio).toList();
    }

    @Override
    public List<Prestamo> buscarTodos() {
        return jpaRepositorio.findAll().stream().map(PrestamoMapeador::aDominio).toList();
    }

    @Override
    public boolean existePorUsuarioId(Long usuarioId) {
        return jpaRepositorio.existsByUsuarioId(usuarioId);
    }

    @Override
    public void eliminar(Long id) {
        jpaRepositorio.deleteById(id);
    }
}
