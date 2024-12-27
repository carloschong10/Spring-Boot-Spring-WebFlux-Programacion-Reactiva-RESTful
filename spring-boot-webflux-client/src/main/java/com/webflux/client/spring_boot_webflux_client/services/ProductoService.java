package com.webflux.client.spring_boot_webflux_client.services;

import com.webflux.client.spring_boot_webflux_client.models.Producto;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {
    public Flux<Producto> findAll();

    public Mono<Producto> findById(String id);

    public Mono<Producto> save(Producto producto);

    public Mono<Producto> update(Producto producto, String id);

    public Mono<Void> delete(String id);

    public Mono<Producto> upload(FilePart part, String id);
}
