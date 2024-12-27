package com.webflux.client.spring_boot_webflux_client.handler;

import com.webflux.client.spring_boot_webflux_client.models.Producto;
import com.webflux.client.spring_boot_webflux_client.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;

@Component
public class ProductoHandler {

    @Autowired
    ProductoService productoService;

    public Mono<ServerResponse> listar(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.findAll(), Producto.class);
    }

    public Mono<ServerResponse> buscarPorId(ServerRequest request) {
        String id = request.pathVariable("id");
        return productoService.findById(id)
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
//                        .body(BodyInserters.fromValue(p)))
                        .bodyValue(p))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        Mono<Producto> productoMono = request.bodyToMono(Producto.class);

        return productoMono
                .flatMap(p -> {
                    if (p.getCreateAt() == null)
                        p.setCreateAt(new Date());
                    return productoService.save(p);
                })
                .flatMap(p -> ServerResponse.created(URI.create("/api/client/" + p.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromValue(p)));
                        .bodyValue(p))
                .onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;

                    if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(errorResponse.getResponseBodyAsString());
                    }
                    return Mono.error(errorResponse);
                });
    }

    public Mono<ServerResponse> editar(ServerRequest request) {
        Mono<Producto> productoMono = request.bodyToMono(Producto.class);
        String id = request.pathVariable("id");

        return productoMono.flatMap(p -> ServerResponse.created(URI.create("/api/client/" + id))
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.update(p, id), Producto.class));
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        String id = request.pathVariable("id");

        return productoService.delete(id)
                .then(ServerResponse.noContent().build());
    }
}
