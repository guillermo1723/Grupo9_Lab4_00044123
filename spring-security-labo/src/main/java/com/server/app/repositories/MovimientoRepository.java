package com.server.app.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.server.app.entities.finance.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    Page<Movimiento> findByCuentaUsuarioId(Integer usuarioId, Pageable pageable);

    Page<Movimiento> findByCuentaUsuarioIdAndFechaGreaterThanEqual(
            Integer usuarioId,
            LocalDateTime desde,
            Pageable pageable
    );

    Page<Movimiento> findByCuentaUsuarioIdAndFechaLessThanEqual(
            Integer usuarioId,
            LocalDateTime hasta,
            Pageable pageable
    );

    Page<Movimiento> findByCuentaUsuarioIdAndFechaBetween(
            Integer usuarioId,
            LocalDateTime desde,
            LocalDateTime hasta,
            Pageable pageable
    );
}