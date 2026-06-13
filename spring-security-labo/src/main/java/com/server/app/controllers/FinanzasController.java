package com.server.app.controllers;

import java.time.LocalDate;
import java.util.List;

import com.server.app.dto.finance.CategoriaResponseDto;
import com.server.app.dto.finance.CuentaCreateDto;
import com.server.app.dto.finance.CuentaResponseDto;
import com.server.app.dto.finance.MovimientoResponseDto;
import com.server.app.dto.finance.TransferenciaDto;
import com.server.app.dto.finance.TransferenciaResponseDto;
import com.server.app.dto.response.Pagination;
import com.server.app.entities.User;
import com.server.app.services.FinanzasService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finanzas")
public class FinanzasController {

    private final FinanzasService finanzasService;

    public FinanzasController(FinanzasService finanzasService) {
        this.finanzasService = finanzasService;
    }

    @GetMapping("/cuentas")
    public ResponseEntity<List<CuentaResponseDto>> cuentas(Authentication authentication) {
        User usuario = (User) authentication.getPrincipal();
        return ResponseEntity.ok(finanzasService.listarCuentasDelUsuario(usuario));
    }

    @PostMapping("/cuentas")
    public ResponseEntity<CuentaResponseDto> crearCuenta(
            Authentication authentication,
            @Valid @RequestBody CuentaCreateDto dto
    ) {
        User usuario = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(finanzasService.crearCuenta(usuario, dto));
    }

    @GetMapping("/movimientos")
    public ResponseEntity<Pagination<MovimientoResponseDto>> movimientos(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        User usuario = (User) authentication.getPrincipal();
        return ResponseEntity.ok(finanzasService.listarMovimientos(usuario, page, size, desde, hasta));
    }

    @PostMapping("/transferencias")
    public ResponseEntity<TransferenciaResponseDto> transferencias(
            Authentication authentication,
            @Valid @RequestBody TransferenciaDto dto
    ) {
        User usuario = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(finanzasService.transferir(usuario, dto));
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaResponseDto>> categorias() {
        return ResponseEntity.ok(finanzasService.listarCategorias());
    }
}