package com.springboot.webflux.app.dao;

import com.springboot.webflux.app.models.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {
}
