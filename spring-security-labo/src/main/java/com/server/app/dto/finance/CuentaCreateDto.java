package com.server.app.dto.finance;

import java.math.BigDecimal;

import com.server.app.entities.finance.TipoCuenta;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CuentaCreateDto {

    @NotBlank(message = "El alias es obligatorio")
    @Size(max = 120, message = "El alias no puede superar 120 caracteres")
    private String alias;

    @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 10, message = "La moneda debe tener entre 3 y 10 caracteres")
    private String moneda;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private TipoCuenta tipo;

    @DecimalMin(value = "0.00", inclusive = true, message = "El saldo base no puede ser negativo")
    private BigDecimal saldoBase = BigDecimal.ZERO;
}