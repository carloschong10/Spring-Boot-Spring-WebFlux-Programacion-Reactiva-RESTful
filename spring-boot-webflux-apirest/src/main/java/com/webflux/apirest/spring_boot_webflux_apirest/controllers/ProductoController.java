package com.webflux.apirest.spring_boot_webflux_apirest.controllers;

import com.webflux.apirest.spring_boot_webflux_apirest.models.Producto;
import com.webflux.apirest.spring_boot_webflux_apirest.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    ProductoService productoService;

    @GetMapping
    public Mono<ResponseEntity<Flux<Producto>>> lista() { //con ResponseEntity nos permite manejar la respuesta
        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productoService.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Producto>> findById(@PathVariable String id) {
        return productoService.findById(id)
//                .map(ResponseEntity::ok) //producto -> ResponseEntity.ok(producto)
                .map(p -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
