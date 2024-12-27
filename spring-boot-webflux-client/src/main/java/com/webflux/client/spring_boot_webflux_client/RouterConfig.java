package com.webflux.client.spring_boot_webflux_client;

import com.webflux.client.spring_boot_webflux_client.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;


@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler handler) {
        return RouterFunctions.route(RequestPredicates.GET("/api/client"), handler::listar)
                .andRoute(RequestPredicates.GET("/api/client/{id}"), handler::buscarPorId)
                ;
    }
}
