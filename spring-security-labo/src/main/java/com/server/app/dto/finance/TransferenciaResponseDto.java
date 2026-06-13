package com.server.app.dto.finance;

public record TransferenciaResponseDto(
        MovimientoResponseDto movimientoOrigen,
        MovimientoResponseDto movimientoDestino
) {}