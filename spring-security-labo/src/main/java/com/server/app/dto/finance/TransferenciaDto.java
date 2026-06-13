package com.server.app.dto.finance;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransferenciaDto {

    @NotNull(message = "La cuenta origen es obligatoria")
    private Long cuentaOrigenId;

    @NotNull(message = "La cuenta destino es obligatoria")
    private Long cuentaDestinoId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @DecimalMin(value = "0.000001", inclusive = true, message = "La tasa de cambio debe ser mayor a cero")
    private BigDecimal tasaCambio;

    private String descripcion;
}