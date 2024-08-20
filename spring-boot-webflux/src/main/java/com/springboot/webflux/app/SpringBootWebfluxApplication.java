package com.springboot.webflux.app;

import com.springboot.webflux.app.dao.ProductoDao;
import com.springboot.webflux.app.models.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApplication{
    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebfluxApplication.class, args);
    }
}
