package com.webflux.apirest.spring_boot_webflux_apirest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webflux.apirest.spring_boot_webflux_apirest.models.Categoria;
import com.webflux.apirest.spring_boot_webflux_apirest.models.Producto;
import com.webflux.apirest.spring_boot_webflux_apirest.services.ProductoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductoService productoService;

    @Value("${config.base.endpoint}")
    private String baseUrl;

    @Value("${config.base.endpointHandler}")
    private String urlHandler;

    @Value("${config.base.endpointRestController}")
    private String urlRestController;

    @BeforeEach
    void setUp() {
        baseUrl = (baseUrl.equals(urlHandler) ? urlHandler : urlRestController);
    }

    @Test
    void listarTest() {
        webTestClient.get()
                .uri(baseUrl)
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
                .uri(baseUrl + "/{id}", Collections.singletonMap("id", producto.getId()))
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

        webTestClient.post().uri(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(producto), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath(baseUrl.equals(urlHandler) ? "$.id" : "$.producto.id").isNotEmpty()
                .jsonPath(baseUrl.equals(urlHandler) ? "$.nombre" : "$.producto.nombre").isEqualTo("producto de ejemplo")
                .jsonPath(baseUrl.equals(urlHandler) ? "$.categoria.nombre" : "$.producto.categoria.nombre").isEqualTo("Cocina");
    }

    @Test
    void crearTest2WithUrlHandler() {
        assumeTrue(baseUrl.equals(urlHandler));

        Categoria categoria = productoService.findCategoriaByNombre("Cocina").block();
        Producto producto = new Producto("producto de ejemplo", 99.99, categoria);

        webTestClient.post().uri(baseUrl)
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
    void crearTest2WithUrlRestController() {
        assumeTrue(!baseUrl.equals(urlHandler));
        Categoria categoria = productoService.findCategoriaByNombre("Cocina").block();
        Producto producto = new Producto("producto de ejemplo", 99.99, categoria);

        webTestClient.post().uri(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(producto), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {
                })
                .consumeWith(response -> {
                    Object o = response.getResponseBody().get("producto");
                    Producto p = new ObjectMapper().convertValue(o, Producto.class);

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

        webTestClient.put().uri(baseUrl + "/{id}", Collections.singletonMap("id", producto.getId()))
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

    @Test
    void eliminarTest() {
        Producto producto = productoService.findByNombre("Producto 1").block();

        webTestClient.delete().uri(baseUrl + "/{id}", Collections.singletonMap("id", producto.getId()))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .isEmpty();
    }

    @Test
    void eliminarTest2() {
        Producto producto = productoService.findByNombre("Producto 2").block();

        webTestClient.delete().uri(baseUrl + "/{id}", Collections.singletonMap("id", producto.getId()))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .isEmpty();

        webTestClient.get().uri(baseUrl + "/{id}", Collections.singletonMap("id", producto.getId()))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .isEmpty();
    }
}
