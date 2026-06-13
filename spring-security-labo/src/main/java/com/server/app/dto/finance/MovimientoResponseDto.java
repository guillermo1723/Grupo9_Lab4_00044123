package com.server.app.dto.finance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponseDto(
        Long id,
        BigDecimal monto,
        String monedaOriginal,
        BigDecimal tasaCambio,
        LocalDateTime fecha,
        String descripcion,
        Long cuentaId,
        String cuentaAlias,
        Long categoriaId,
        String categoriaNombre
) {}