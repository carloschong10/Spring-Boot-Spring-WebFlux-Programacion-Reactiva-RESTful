package com.webflux.client.spring_boot_webflux_client.handler;

import com.webflux.client.spring_boot_webflux_client.models.Producto;
import com.webflux.client.spring_boot_webflux_client.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
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
}
