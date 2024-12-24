package com.webflux.apirest.spring_boot_webflux_apirest;

import com.webflux.apirest.spring_boot_webflux_apirest.handler.ProductoHandler;
import com.webflux.apirest.spring_boot_webflux_apirest.models.Producto;
import com.webflux.apirest.spring_boot_webflux_apirest.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterFunctionConfig {

    @Autowired
    private ProductoService productoService;

    /*
    //primera forma handler o request en la misma peticion
    @Bean
    public RouterFunction<ServerResponse> routes() {
        return route(GET("/api/v2/productos").or(GET("/api/v3/productos")), request -> { //handler en la misma peticion
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(productoService.findAll(), Producto.class);
        });
    }
    */

    //segunda forma en un método de una clase handler
    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler handler) {
        return route(GET("/api/v2/productos").or(GET("/api/v3/productos")), handler::listar)
                .andRoute(GET("/api/v2/productos/{id}").or(GET("/api/v3/productos")), handler::findById)
                .andRoute(POST("/api/v2/productos").or(POST("/api/v3/productos")).and(contentType(MediaType.APPLICATION_JSON)), handler::crear);
    }
}
