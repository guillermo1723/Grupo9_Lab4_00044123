package com.server.app.dto.finance;

import com.server.app.entities.finance.TipoCategoria;

public record CategoriaResponseDto(
        Long id,
        String nombre,
        TipoCategoria tipo,
        Long categoriaPadreId
) {}