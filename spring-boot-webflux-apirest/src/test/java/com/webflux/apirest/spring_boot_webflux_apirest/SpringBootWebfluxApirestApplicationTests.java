package com.webflux.apirest.spring_boot_webflux_apirest;

import com.webflux.apirest.spring_boot_webflux_apirest.models.Categoria;
import com.webflux.apirest.spring_boot_webflux_apirest.models.Producto;
import com.webflux.apirest.spring_boot_webflux_apirest.services.ProductoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductoService productoService;

    @Test
    void listarTest() {
        webTestClient.get()
                .uri("/api/v2/productos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Producto.class)
                .consumeWith(response -> {
                    List<Producto> productos = response.getResponseBody();
                    productos.forEach(p -> System.out.println(p.getNombre()));

                    Assertions.assertTrue(productos.size() > 0);
                });
//                .hasSize(5);
    }

    @Test
    void listarPorIdTest() {
        Producto producto = productoService.findByNombre("Producto 4").block(); ////con block convertimos el Mono o FLux en un Producto o elemento sìncrono ya que no se puede trabajra con elementos asincronos, ademas las pruebas unitarias no se pueden trabajar dentro de un suscribe dentro de un Observable.

        webTestClient.get()
                .uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto p = response.getResponseBody();

                    Assertions.assertTrue(!p.getId().isEmpty());
                    Assertions.assertTrue(p.getNombre().equals("Producto 4"));
                });
                /*.expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("Producto 4");*/
    }

    @Test
    void crearTest() {
        Categoria categoria = productoService.findCategoriaByNombre("Cocina").block();
        Producto producto = new Producto("producto de ejemplo", 99.99, categoria);

        webTestClient.post().uri("/api/v2/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(producto), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("producto de ejemplo")
                .jsonPath("$.categoria.nombre").isEqualTo("Cocina");
    }

    @Test
    void crearTest2() {
        Categoria categoria = productoService.findCategoriaByNombre("Cocina").block();
        Producto producto = new Producto("producto de ejemplo", 99.99, categoria);

        webTestClient.post().uri("/api/v2/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(producto), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto p = response.getResponseBody();

                    Assertions.assertTrue(!p.getId().isEmpty());
                    Assertions.assertTrue(p.getNombre().equals("producto de ejemplo"));
                    Assertions.assertTrue(p.getCategoria().getNombre().equals("Cocina"));
                });
    }

    @Test
    void editarTest() {
        Producto producto = productoService.findByNombre("Producto 3").block();
        Categoria categoria = productoService.findCategoriaByNombre("Muebles").block();

        Producto productoEditado = new Producto("Asus Notebook", 700.99, categoria);

        webTestClient.put().uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(productoEditado), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("Asus Notebook")
                .jsonPath("$.precio").isEqualTo(700.99)
                .jsonPath("$.categoria.nombre").isEqualTo("Muebles");
    }
}
