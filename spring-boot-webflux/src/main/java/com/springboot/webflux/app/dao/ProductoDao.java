package com.springboot.webflux.app.dao;

import com.springboot.webflux.app.models.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {
}
