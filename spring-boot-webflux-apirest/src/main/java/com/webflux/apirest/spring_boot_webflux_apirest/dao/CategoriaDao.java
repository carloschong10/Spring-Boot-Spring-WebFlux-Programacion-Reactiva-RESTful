package com.webflux.apirest.spring_boot_webflux_apirest.dao;

import com.webflux.apirest.spring_boot_webflux_apirest.models.Categoria;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {
    public Mono<Categoria> findByNombre(String nombre);

    @Query("{'nombre': ?0}")
    public Mono<Categoria> obtenerPorNombre(String nombre);
}
