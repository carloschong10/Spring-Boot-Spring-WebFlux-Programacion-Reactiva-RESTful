package com.webflux.client.spring_boot_webflux_client.handler;

import com.webflux.client.spring_boot_webflux_client.models.Producto;
import com.webflux.client.spring_boot_webflux_client.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;
import java.util.Map;

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
        return errorHandler(
                productoService.findById(id)
                        .flatMap(p -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
//                        .body(BodyInserters.fromValue(p)))
                                .bodyValue(p))
                        .switchIfEmpty(ServerResponse.notFound().build())
                /*.onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;

                    if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
//                        return ServerResponse.notFound().build();
                        //si queremos retornar no un error sin contenido, sino un error genérico podemos hacerlo de la sgte forma construyendo el json personalizado:
                        Map<String, Object> body = Map.of(
                                "error", "No existe el producto: " + errorResponse.getMessage(),
                                "fecha", new Date(),
                                "status", errorResponse.getStatusCode().value()
                        );
                        return ServerResponse.status(HttpStatus.NOT_FOUND)
                                .bodyValue(body);
                    }
                    return Mono.error(errorResponse);
                });*/
        );
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

        return errorHandler(
                productoMono
                        .flatMap(p -> productoService.update(p, id)) //movimos el servicio para acá porque sino lo encapsularia y no ejecutaria el .onErrorResume(error -> {})
                        .flatMap(p -> ServerResponse.created(URI.create("/api/client/" + p.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
//                        .body(BodyInserters.fromValue(p)));
                                .bodyValue(p))
                /*.onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;

                    if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return ServerResponse.notFound().build();
                    }
                    return Mono.error(errorResponse);
                });*/
        );
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        String id = request.pathVariable("id");

        return errorHandler(
                productoService.delete(id)
                        .then(ServerResponse.noContent().build())
                /*.onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;

                    if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return ServerResponse.notFound().build();
                    }
                    return Mono.error(errorResponse);
                });*/
        );
    }

    public Mono<ServerResponse> cargarFoto(ServerRequest request) {
        String id = request.pathVariable("id");

        return errorHandler(
                request.multipartData()
                        .map(multipart -> multipart.toSingleValueMap().get("file"))
                        .cast(FilePart.class)
                        .flatMap(part -> productoService.upload(part, id))
                        .flatMap(p -> ServerResponse
                                .created(URI.create("/api/client/" + p.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(p))
                        /*.onErrorResume(error -> {
                            WebClientResponseException errorResponse = (WebClientResponseException) error;

                            if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                                return ServerResponse.notFound().build();
                            }
                            return Mono.error(errorResponse);
                        });*/
        );
    }

    private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response) {
        return response.onErrorResume(error -> {
            WebClientResponseException errorResponse = (WebClientResponseException) error;

            if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
//                        return ServerResponse.notFound().build();
                //si queremos retornar no un error sin contenido, sino un error genérico podemos hacerlo de la sgte forma construyendo el json personalizado:
                Map<String, Object> body = Map.of(
                        "error", "No existe el producto: " + errorResponse.getMessage(),
                        "fecha", new Date(),
                        "status", errorResponse.getStatusCode().value()
                );
                return ServerResponse.status(HttpStatus.NOT_FOUND)
                        .bodyValue(body);
            }
            return Mono.error(errorResponse);
        });
    }
}
