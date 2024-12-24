package com.webflux.apirest.spring_boot_webflux_apirest.handler;

import com.webflux.apirest.spring_boot_webflux_apirest.models.Producto;
import com.webflux.apirest.spring_boot_webflux_apirest.services.ProductoService;
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
public class ProductoHandler { //este seria como nuestro controlador o handler, lo importante es anotarlo con @Component y no con @Controller

    @Autowired
    private ProductoService productoService;

    public Mono<ServerResponse> listar(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.findAll(), Producto.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("id");
        return productoService.findById(id)
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        Mono<Producto> productoMono = request.bodyToMono(Producto.class);
        return productoMono.flatMap(p -> {
            if (p.getCreateAt() == null) {
                p.setCreateAt(new Date());
            }
            return productoService.save(p);
        }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/" + p.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(p)));
    }

    public Mono<ServerResponse> editar(ServerRequest request) {
        Mono<Producto> productoMono = request.bodyToMono(Producto.class);
        String id = request.pathVariable("id");
        Mono<Producto> productoMonoDb = productoService.findById(id);

        return productoMonoDb.zipWith(productoMono, (pdb, preq) -> {
                    pdb.setNombre(preq.getNombre());
                    pdb.setPrecio(preq.getPrecio());
                    pdb.setCategoria(preq.getCategoria());
                    return pdb;
                }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/" + p.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productoService.save(p), Producto.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
