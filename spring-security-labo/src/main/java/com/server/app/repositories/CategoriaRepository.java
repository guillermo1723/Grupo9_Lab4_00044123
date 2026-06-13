package com.server.app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.server.app.entities.finance.Categoria;
import com.server.app.entities.finance.TipoCategoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByTipoAscNombreAsc();

    Optional<Categoria> findByNombreIgnoreCaseAndTipo(String nombre, TipoCategoria tipo);
}