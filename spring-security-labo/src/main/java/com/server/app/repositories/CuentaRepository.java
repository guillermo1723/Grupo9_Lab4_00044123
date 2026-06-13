package com.server.app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.server.app.entities.finance.Cuenta;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    List<Cuenta> findAllByUsuario_Id(Integer usuarioId);

    Optional<Cuenta> findByIdAndUsuario_Id(Long id, Integer usuarioId);
}