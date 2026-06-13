package com.server.app.dto.finance;

import java.math.BigDecimal;

import com.server.app.entities.finance.TipoCuenta;

public record CuentaResponseDto(
        Long id,
        String alias,
        String moneda,
        BigDecimal saldoBase,
        TipoCuenta tipo,
        Integer usuarioId
) {}