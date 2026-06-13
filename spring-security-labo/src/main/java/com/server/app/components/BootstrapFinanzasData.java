package com.server.app.components;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.server.app.entities.finance.Categoria;
import com.server.app.entities.finance.TipoCategoria;
import com.server.app.repositories.CategoriaRepository;

@Component
@Order(4)
public class BootstrapFinanzasData implements ApplicationRunner {

    private final CategoriaRepository categoriaRepository;

    public BootstrapFinanzasData(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Categoria> defaults = List.of(
                Categoria.builder().nombre("TRANSFERENCIA").tipo(TipoCategoria.INGRESO).build(),
                Categoria.builder().nombre("TRANSFERENCIA").tipo(TipoCategoria.EGRESO).build(),
                Categoria.builder().nombre("GENERAL").tipo(TipoCategoria.INGRESO).build(),
                Categoria.builder().nombre("GENERAL").tipo(TipoCategoria.EGRESO).build()
        );

        for (Categoria categoria : defaults) {
            categoriaRepository.findByNombreIgnoreCaseAndTipo(categoria.getNombre(), categoria.getTipo())
                    .orElseGet(() -> categoriaRepository.save(categoria));
        }
    }
}