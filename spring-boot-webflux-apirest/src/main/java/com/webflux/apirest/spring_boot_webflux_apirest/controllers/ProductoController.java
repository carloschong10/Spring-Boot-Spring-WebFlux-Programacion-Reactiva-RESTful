package com.webflux.apirest.spring_boot_webflux_apirest.controllers;

import com.webflux.apirest.spring_boot_webflux_apirest.models.Producto;
import com.webflux.apirest.spring_boot_webflux_apirest.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;

@RestController
@RequestMapping("/api/productos")
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

    @PostMapping
    public Mono<ResponseEntity<Producto>> guardar(@RequestBody Producto producto) {
        if (producto.getCreateAt() == null) {
            producto.setCreateAt(new Date());
        }

        return productoService.save(producto)
                .map(p -> ResponseEntity.created(URI.create("/api/productos/" + p.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p));
    }
}
