package com.springboot.webflux.app;

import com.springboot.webflux.app.dao.CategoriaDao;
import com.springboot.webflux.app.dao.ProductoDao;
import com.springboot.webflux.app.models.Categoria;
import com.springboot.webflux.app.models.Producto;
import com.springboot.webflux.app.services.ProductoService;
import com.springboot.webflux.app.services.ProductoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

    //    @Autowired
    private final ProductoServiceImpl productoService;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

    public SpringBootWebfluxApplication(ProductoServiceImpl productoService, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.productoService = productoService;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebfluxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        reactiveMongoTemplate.dropCollection("productos").subscribe();
        reactiveMongoTemplate.dropCollection("categorias").subscribe();

        Categoria electronica = new Categoria("Electronica");
        Categoria cocina = new Categoria("Cocina");
        Categoria muebles = new Categoria("Muebles");
        Categoria deporte = new Categoria("Deporte");

        Flux.just(electronica, cocina, muebles, deporte)
                .flatMap(productoService::saveCategoria)
                .doOnNext(c -> log.info("Categoria creada: {}, Id Cat: {}", c.getNombre(), c.getId()))
                .thenMany(
                        Flux.just(
                                new Producto("Producto 1", 456.789, electronica),
                                new Producto("Producto 2", 123D, electronica),
                                new Producto("Producto 3", 100D, muebles),
                                new Producto("Producto 4", 199.99, cocina),
                                new Producto("Producto 5", 350.49, deporte)
                        ).flatMap(producto -> {
                            producto.setCreateAt(new Date());
                            return productoService.save(producto);
                        })
                )
                .subscribe(producto -> {
                            log.info("Producto insertado: {} - {} - {}",
                                    producto.getId(),
                                    producto.getNombre(),
                                    producto.getCategoria().getNombre()
                            );

                        }
                );
    }
}
