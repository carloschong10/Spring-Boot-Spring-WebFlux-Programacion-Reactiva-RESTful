package com.webflux.apirest.spring_boot_webflux_apirest.dao;

import com.webflux.apirest.spring_boot_webflux_apirest.models.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {
}
