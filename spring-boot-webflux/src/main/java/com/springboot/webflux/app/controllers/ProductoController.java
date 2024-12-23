package com.springboot.webflux.app.controllers;

import com.springboot.webflux.app.models.Categoria;
import com.springboot.webflux.app.models.Producto;
import com.springboot.webflux.app.services.ProductoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@SessionAttributes("producto")
@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Value("${config.uploads.path}")
    private String path;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @ModelAttribute("categoriasList")
    private Flux<Categoria> categorias() {
        return productoService.findAllCategoria();
    }

    @GetMapping("/ver/{id}")
    public Mono<String> verDetalle(Model model, @PathVariable String id) {
        return productoService.findById(id)
                .doOnNext(p -> {
                    model.addAttribute("producto", p);
                    model.addAttribute("titulo", "Detalle del Producto");
                }).switchIfEmpty(Mono.just(new Producto())) //es lo mismo que defaultIfEmpty(new Producto())
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto"));
                    }
                    return Mono.just(p);
                }).then(Mono.just("ver"))
                .onErrorResume(ex -> Mono.just("redirect:/productos/listar?error=no+existe+el+producto"));
    }

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
    public Mono<String> guardar(@Valid @ModelAttribute("producto") Producto producto, BindingResult bindingResult, Model model, @RequestPart(name = "fileFoto") FilePart part, SessionStatus sessionStatus) { //BindingResult siempre tiene que ir al costado de @Valid
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Error en el Formulario Producto");
            model.addAttribute("boton", "guardar");
            return Mono.just("form");
        } else {
            sessionStatus.setComplete();

            Mono<Categoria> categoria = productoService.findCategoriaById(producto.getCategoria().getId());

            return categoria.flatMap(c -> {
                        if (producto.getCreateAt() == null) {
                            producto.setCreateAt(new Date());
                        }
                        if (!part.filename().isEmpty()) {
                            producto.setFoto(UUID.randomUUID().toString() + "-" + part.filename()
                                    .replace(" ", "_")
                                    .replace(":", "")
                                    .replace("\\", ""));
                        }
                        producto.setCategoria(c);
                        return productoService.save(producto);
                    }).doOnNext(p -> {
                        log.info("Categoria Seleccionada: {} Id Cat: {}", p.getCategoria().getNombre(), p.getCategoria().getId());
                        log.info("Producto Guardado: {} Id: {}", p.getNombre(), p.getId());
                    }).flatMap(p -> {
                        if (!part.filename().isEmpty()) {
                            return part.transferTo(new File(path + p.getFoto()));
                        }
                        return Mono.empty();
                    })
                    .thenReturn("redirect:/productos/listar?success=Producto+Guardado+Correctamente");
        }
    }

    @GetMapping("/eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id) {
        return productoService.findById(id)
                .doOnNext(p -> log.info("Producto a eliminar: {}", p.getNombre()))
                .defaultIfEmpty(new Producto())
                .flatMap(producto -> {
                    if (producto.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto a eliminar"));
                    }
                    return Mono.just(producto);
                })

                .flatMap(productoService::delete)
                .then(Mono.just("redirect:/productos/listar?Producto+Eliminado+Con+Exito"))
                .onErrorResume(ex -> Mono.just("redirect:/productos/listar?error=no+existe+el+producto+a+eliminar"));
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable(name = "id") String id, Model model) {
        Mono<Producto> productoMono = productoService.findById(id)
                .doOnNext(p -> log.info("Producto: {}", p.getNombre()))
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
