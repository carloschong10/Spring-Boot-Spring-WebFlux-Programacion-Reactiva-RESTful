package com.webflux.client.spring_boot_webflux_client.services;

import com.webflux.client.spring_boot_webflux_client.models.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private WebClient webClient;

    @Override
    public Flux<Producto> findAll() {
        return webClient.get() //ya no es necesario poner la url porque ya la tenemos configurada dentro de AppConfig
                .accept(MediaType.APPLICATION_JSON)
//                .exchangeToFlux(response -> response.bodyToFlux(Producto.class));
                .retrieve().bodyToFlux(Producto.class);
    }

    @Override
    public Mono<Producto> findById(String id) {
//        Map<String, Object> params = Map.of("id", id);

        /*
//        return webClient.get().uri("/{id}", params)
        return webClient.get().uri("/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> response.bodyToMono(Producto.class));
         */
        return webClient.get().uri("/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return webClient.post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(producto))
//                .bodyValue(producto)
                .retrieve().bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> update(Producto producto, String id) {
        return webClient.put()
                .uri("/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromValue(producto))
                .bodyValue(producto)
                .retrieve().bodyToMono(Producto.class);
    }

    @Override
    public Mono<Void> delete(String id) {
        return webClient.delete().uri("/{id}", Collections.singletonMap("id", id))
//                .retrieve().bodyToMono(Void.class);
                .exchangeToMono(response -> response.bodyToMono(Void.class))
                .then();
    }


}
