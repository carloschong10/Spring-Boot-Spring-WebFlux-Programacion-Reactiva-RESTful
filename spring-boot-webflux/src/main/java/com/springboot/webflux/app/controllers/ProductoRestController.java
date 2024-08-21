package com.springboot.webflux.app.controllers;

import com.springboot.webflux.app.dao.ProductoDao;
import com.springboot.webflux.app.models.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    @Autowired
    private ProductoDao productoDao;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @GetMapping()
    public Flux<Producto> index() {
        Flux<Producto> productos = productoDao.findAll().map(producto -> {

            producto.setNombre(producto.getNombre().toUpperCase());

            return producto;
        }).doOnNext(prod -> log.info(prod.getNombre()));

        return productos;
    }

    @GetMapping("/{id}")
    public Mono<Producto> show(@PathVariable String id) {
//        Mono<Producto> productos = productoDao.findById(id);
        Flux<Producto> productos = productoDao.findAll();
        Mono<Producto> producto = productos.filter(producto1 -> {
                    return producto1.getId().equals(id);
                })
                .next()
                .doOnNext(prod -> log.info(prod.getNombre()));

        return producto;
    }
}
