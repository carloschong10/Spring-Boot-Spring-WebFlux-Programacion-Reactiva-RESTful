package com.webflux.apirest.spring_boot_webflux_apirest.dao;

import com.webflux.apirest.spring_boot_webflux_apirest.models.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {
}
