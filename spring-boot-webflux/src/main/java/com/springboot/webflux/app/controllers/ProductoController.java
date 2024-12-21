package com.springboot.webflux.app.controllers;

import com.springboot.webflux.app.models.Producto;
import com.springboot.webflux.app.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@SessionAttributes("producto")
@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @GetMapping({"/listar", "/"})
    public Mono<String> listar(Model model) {
        Flux<Producto> productos = productoService.findAllConNombreUpperCase();

        productos.subscribe(prod -> log.info(prod.getNombre()));

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de Productos");

        return Mono.just("listar");
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Formulario de producto");
        model.addAttribute("boton", "Crear");

        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> guardar(Producto producto, SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return productoService.save(producto).doOnNext(p -> {
            log.info("Producto Guardado: {} Id: {}", p.getNombre(), p.getId());
        }).thenReturn("redirect:/productos/listar");
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable(name = "id") String id, Model model) {
        Mono<Producto> productoMono = productoService.findById(id)
                .doOnNext(p -> {
                    log.info("Producto: " + p.getNombre());
                })
                .defaultIfEmpty(new Producto());

        model.addAttribute("titulo", "editarProducto");
        model.addAttribute("producto", productoMono);
        model.addAttribute("boton", "Editar");

        return Mono.just("form");
    }

    @GetMapping("/form-v2/{id}")
    public Mono<String> editarv2(@PathVariable(name = "id") String id, Model model) {
        return productoService.findById(id)
                .doOnNext(p -> {
                    log.info("Producto: " + p.getNombre());
                    model.addAttribute("titulo", "editarProducto");
                    model.addAttribute("producto", p);
                    model.addAttribute("boton", "Editar");
                })
                .defaultIfEmpty(new Producto())
                .flatMap(producto -> {
                    if (producto.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto"));
                    }

                    return Mono.just(producto);
                })
                .then(Mono.just("form"))
                .onErrorResume(ex -> Mono.just("redirect:/productos/listar?error=no+existe+el+producto"));
    }

    @GetMapping("/listarDataDriver")
    public String listarDataDriver(Model model) {
        Flux<Producto> productos = productoService.findAllConNombreUpperCase().delayElements(Duration.ofSeconds(1));

        productos.subscribe(prod -> log.info(prod.getNombre()));

        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 2));
        model.addAttribute("titulo", "Listado de Productos");

        return "listar";
    }

    @GetMapping("/listarFull")
    public String listarFull(Model model) {
        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat();

        productos.subscribe(prod -> log.info(prod.getNombre()));

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de Productos");

        return "listar";
    }

    @GetMapping("/listarChunked")
    public String listarChunked(Model model) {
        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat();

        productos.subscribe(prod -> log.info(prod.getNombre()));

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de Productos");

        return "listar-chunked";
    }

}
