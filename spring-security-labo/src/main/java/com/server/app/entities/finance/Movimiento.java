package com.server.app.entities.finance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimientos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "moneda_original", nullable = false, length = 10)
    private String monedaOriginal;

    @Column(name = "tasa_cambio", nullable = false, precision = 19, scale = 6)
    private BigDecimal tasaCambio;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = true, length = 255)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    @JsonIgnore
    private Cuenta cuenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonIgnore
    private Categoria categoria;

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}