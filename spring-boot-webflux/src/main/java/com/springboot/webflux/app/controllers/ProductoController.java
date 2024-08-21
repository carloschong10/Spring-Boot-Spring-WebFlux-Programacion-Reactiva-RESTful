package com.springboot.webflux.app.controllers;

import com.springboot.webflux.app.dao.ProductoDao;
import com.springboot.webflux.app.models.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoDao productoDao;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @GetMapping({"/listar", "/"})
    public String listar(Model model) {
        Flux<Producto> productos = productoDao.findAll().map(producto -> {  //aca no es necesario hacer el subscribe ya que thymeleaf lo hace por debajo (es decir muestra los datos en una plantilla con thymeleaf, es decir la plantilla thymeleaf es el observador que se suscribe a este observable)

            producto.setNombre(producto.getNombre().toUpperCase());

            return producto;
        });

        productos.subscribe(prod -> log.info(prod.getNombre()));

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de Productos");

        return "listar";
    }

    @GetMapping("/listarDataDriver")
    public String listarDataDriver(Model model) {
        Flux<Producto> productos = productoDao.findAll().map(producto -> {

            producto.setNombre(producto.getNombre().toUpperCase());

            return producto;
        }).delayElements(Duration.ofSeconds(1));

        productos.subscribe(prod -> log.info(prod.getNombre()));

        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 2));
        model.addAttribute("titulo", "Listado de Productos");

        return "listar";
    }

    @GetMapping("/listarFull")
    public String listarFull(Model model) {
        Flux<Producto> productos = productoDao.findAll().map(producto -> {

            producto.setNombre(producto.getNombre().toUpperCase());

            return producto;
        }).repeat(5000);

        productos.subscribe(prod -> log.info(prod.getNombre()));

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de Productos");

        return "listar";
    }

}
