package com.springboot.webflux.app;

import com.springboot.webflux.app.dao.ProductoDao;
import com.springboot.webflux.app.models.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

    //    @Autowired
    private final ProductoDao productoDao;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

    public SpringBootWebfluxApplication(ProductoDao productoDao, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.productoDao = productoDao;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebfluxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        reactiveMongoTemplate.dropCollection("productos").subscribe();

        Flux.just(
                        new Producto("Producto 1", 456.789),
                        new Producto("Producto 2", 123D),
                        new Producto("Producto 3", 100D),
                        new Producto("Producto 4", 199.99),
                        new Producto("Producto 5", 350.49)
                ).flatMap(producto -> {
                    producto.setCreateAt(new Date());
                    return productoDao.save(producto);
                })
                .subscribe(producto -> log.info("Insertado: {} - {}", producto.getId(), producto.getNombre()));
    }
}
