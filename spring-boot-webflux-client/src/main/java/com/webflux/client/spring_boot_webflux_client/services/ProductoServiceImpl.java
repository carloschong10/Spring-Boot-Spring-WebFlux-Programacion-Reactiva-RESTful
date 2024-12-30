package com.webflux.client.spring_boot_webflux_client.services;

import com.webflux.client.spring_boot_webflux_client.models.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
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
    private WebClient.Builder webClient;

    @Override
    public Flux<Producto> findAll() {
        return webClient.build().get() //ya no es necesario poner la url porque ya la tenemos configurada dentro de AppConfig
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
        return webClient.build().get().uri("/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return webClient.build().post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(producto))
//                .bodyValue(producto)
                .retrieve().bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> update(Producto producto, String id) {
        return webClient.build().put()
                .uri("/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromValue(producto))
                .bodyValue(producto)
                .retrieve().bodyToMono(Producto.class);
    }

    @Override
    public Mono<Void> delete(String id) {
        return webClient.build().delete().uri("/{id}", Collections.singletonMap("id", id))
                .retrieve().bodyToMono(Void.class); //es mejor usar el retrieve ya que el retrieve lanza una excepcion WebClientResponseException en caso no encuentre el Producto por el Id, y esta excepcion la podemos controlar en el Controlador ProductoHandler con un .onErrorResume(error -> {})
//                .exchangeToMono(response -> response.bodyToMono(Void.class))
//                .then();
    }

    @Override
    public Mono<Producto> upload(FilePart filePart, String id) {
        MultipartBodyBuilder parts = new MultipartBodyBuilder();
        parts
                .asyncPart("file", filePart.content(), DataBuffer.class)
                .headers(h -> {
                    h.setContentDispositionFormData("file", filePart.filename());
                });

        return webClient.build().post()
                .uri("/upload/{id}", Map.of("id", id))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(parts.build())
                .retrieve()
                .bodyToMono(Producto.class);
    }


}
