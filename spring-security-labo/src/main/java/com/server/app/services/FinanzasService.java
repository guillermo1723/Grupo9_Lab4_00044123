package com.server.app.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.app.dto.finance.CategoriaResponseDto;
import com.server.app.dto.finance.CuentaCreateDto;
import com.server.app.dto.finance.CuentaResponseDto;
import com.server.app.dto.finance.MovimientoResponseDto;
import com.server.app.dto.finance.TransferenciaDto;
import com.server.app.dto.finance.TransferenciaResponseDto;
import com.server.app.dto.response.Pagination;
import com.server.app.dto.response.PaginationMeta;
import com.server.app.entities.User;
import com.server.app.entities.finance.Categoria;
import com.server.app.entities.finance.Cuenta;
import com.server.app.entities.finance.Movimiento;
import com.server.app.entities.finance.TipoCategoria;
import com.server.app.entities.finance.TipoCuenta;
import com.server.app.exceptions.ConfictException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.CategoriaRepository;
import com.server.app.repositories.CuentaRepository;
import com.server.app.repositories.MovimientoRepository;

@Service
public class FinanzasService {

    private static final String TRANSFERENCIA = "TRANSFERENCIA";

    private final CuentaRepository cuentaRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoRepository movimientoRepository;

    public FinanzasService(
            CuentaRepository cuentaRepository,
            CategoriaRepository categoriaRepository,
            MovimientoRepository movimientoRepository
    ) {
        this.cuentaRepository = cuentaRepository;
        this.categoriaRepository = categoriaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Transactional(readOnly = true)
    public List<CuentaResponseDto> listarCuentasDelUsuario(User usuario) {
        return cuentaRepository.findAllByUsuario_Id(usuario.getId())
                .stream()
                .map(this::toCuentaResponse)
                .toList();
    }

    @Transactional
    public CuentaResponseDto crearCuenta(User usuario, CuentaCreateDto dto) {
        Cuenta cuenta = Cuenta.builder()
                .alias(dto.getAlias().trim())
                .moneda(dto.getMoneda().trim().toUpperCase())
                .tipo(dto.getTipo())
                .saldoBase(dto.getSaldoBase() == null ? BigDecimal.ZERO : dto.getSaldoBase())
                .usuario(usuario)
                .build();

        return toCuentaResponse(cuentaRepository.save(cuenta));
    }

    @Transactional(readOnly = true)
    public Pagination<MovimientoResponseDto> listarMovimientos(
            User usuario,
            int page,
            int size,
            LocalDate desde,
            LocalDate hasta
    ) {
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = hasta != null ? hasta.atTime(LocalTime.MAX) : null;

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<Movimiento> movimientos = listarMovimientosFiltrados(
                usuario.getId(),
                desdeDateTime,
                hastaDateTime,
                pageable
        );

        return new Pagination<>(
                movimientos.getContent().stream().map(this::toMovimientoResponse).toList(),
                new PaginationMeta(
                        movimientos.getNumber(),
                        movimientos.getSize(),
                        movimientos.getTotalPages(),
                        movimientos.getTotalElements()
                )
        );
    }

    private Page<Movimiento> listarMovimientosFiltrados(
            Integer usuarioId,
            LocalDateTime desde,
            LocalDateTime hasta,
            PageRequest pageable
    ) {
        if (desde != null && hasta != null) {
            return movimientoRepository.findByCuentaUsuarioIdAndFechaBetween(usuarioId, desde, hasta, pageable);
        }
        if (desde != null) {
            return movimientoRepository.findByCuentaUsuarioIdAndFechaGreaterThanEqual(usuarioId, desde, pageable);
        }
        if (hasta != null) {
            return movimientoRepository.findByCuentaUsuarioIdAndFechaLessThanEqual(usuarioId, hasta, pageable);
        }

        return movimientoRepository.findByCuentaUsuarioId(usuarioId, pageable);
    }

    @Transactional
    public TransferenciaResponseDto transferir(User usuario, TransferenciaDto dto) {
        if (dto.getCuentaOrigenId().equals(dto.getCuentaDestinoId())) {
            throw new ConfictException("La cuenta origen y destino no pueden ser la misma");
        }

        Cuenta origen = cuentaRepository.findByIdAndUsuario_Id(dto.getCuentaOrigenId(), usuario.getId())
                .orElseThrow(() -> new NotFoundException("Cuenta origen no encontrada"));
        Cuenta destino = cuentaRepository.findByIdAndUsuario_Id(dto.getCuentaDestinoId(), usuario.getId())
                .orElseThrow(() -> new NotFoundException("Cuenta destino no encontrada"));

        BigDecimal monto = dto.getMonto();
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ConfictException("El monto debe ser mayor a cero");
        }

        boolean mismaMoneda = origen.getMoneda().equalsIgnoreCase(destino.getMoneda());
        BigDecimal tasaCambio = dto.getTasaCambio() != null ? dto.getTasaCambio() : BigDecimal.ONE;
        if (!mismaMoneda && dto.getTasaCambio() == null) {
            throw new ConfictException("La tasa de cambio es obligatoria cuando las monedas son diferentes");
        }
        if (tasaCambio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ConfictException("La tasa de cambio debe ser mayor a cero");
        }

        if (mismaMoneda) {
            tasaCambio = BigDecimal.ONE;
        }

        BigDecimal montoDestino = monto.multiply(tasaCambio);
        if (origen.getSaldoBase().compareTo(monto) < 0) {
            throw new ConfictException("Saldo insuficiente en la cuenta origen");
        }

        Categoria egreso = obtenerCategoriaTransferencia(TipoCategoria.EGRESO);
        Categoria ingreso = obtenerCategoriaTransferencia(TipoCategoria.INGRESO);

        origen.setSaldoBase(origen.getSaldoBase().subtract(monto));
        destino.setSaldoBase(destino.getSaldoBase().add(montoDestino));

        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        Movimiento movimientoOrigen = movimientoRepository.save(Movimiento.builder()
                .cuenta(origen)
                .categoria(egreso)
                .monto(monto)
                .monedaOriginal(origen.getMoneda())
                .tasaCambio(tasaCambio)
                .descripcion(dto.getDescripcion() != null && !dto.getDescripcion().isBlank()
                        ? dto.getDescripcion()
                        : "Transferencia hacia " + destino.getAlias())
                .build());

        Movimiento movimientoDestino = movimientoRepository.save(Movimiento.builder()
                .cuenta(destino)
                .categoria(ingreso)
                .monto(montoDestino)
                .monedaOriginal(origen.getMoneda())
                .tasaCambio(tasaCambio)
                .descripcion(dto.getDescripcion() != null && !dto.getDescripcion().isBlank()
                        ? dto.getDescripcion()
                        : "Transferencia recibida desde " + origen.getAlias())
                .build());

        return new TransferenciaResponseDto(
                toMovimientoResponse(movimientoOrigen),
                toMovimientoResponse(movimientoDestino)
        );
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDto> listarCategorias() {
        return categoriaRepository.findAllByOrderByTipoAscNombreAsc()
                .stream()
                .map(this::toCategoriaResponse)
                .toList();
    }

    private Categoria obtenerCategoriaTransferencia(TipoCategoria tipo) {
        return categoriaRepository.findByNombreIgnoreCaseAndTipo(TRANSFERENCIA, tipo)
                .orElseGet(() -> categoriaRepository.save(Categoria.builder()
                        .nombre(TRANSFERENCIA)
                        .tipo(tipo)
                        .build()));
    }

    private CuentaResponseDto toCuentaResponse(Cuenta cuenta) {
        return new CuentaResponseDto(
                cuenta.getId(),
                cuenta.getAlias(),
                cuenta.getMoneda(),
                cuenta.getSaldoBase(),
                cuenta.getTipo(),
                cuenta.getUsuario() != null ? cuenta.getUsuario().getId() : null
        );
    }

    private CategoriaResponseDto toCategoriaResponse(Categoria categoria) {
        return new CategoriaResponseDto(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getTipo(),
                categoria.getCategoriaPadre() != null ? categoria.getCategoriaPadre().getId() : null
        );
    }

    private MovimientoResponseDto toMovimientoResponse(Movimiento movimiento) {
        return new MovimientoResponseDto(
                movimiento.getId(),
                movimiento.getMonto(),
                movimiento.getMonedaOriginal(),
                movimiento.getTasaCambio(),
                movimiento.getFecha(),
                movimiento.getDescripcion(),
                movimiento.getCuenta() != null ? movimiento.getCuenta().getId() : null,
                movimiento.getCuenta() != null ? movimiento.getCuenta().getAlias() : null,
                movimiento.getCategoria() != null ? movimiento.getCategoria().getId() : null,
                movimiento.getCategoria() != null ? movimiento.getCategoria().getNombre() : null
        );
    }
}