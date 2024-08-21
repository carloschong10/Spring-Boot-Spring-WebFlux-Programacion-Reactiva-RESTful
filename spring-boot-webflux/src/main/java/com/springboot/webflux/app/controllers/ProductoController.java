package com.springboot.webflux.app.controllers;

import com.springboot.webflux.app.dao.ProductoDao;
import com.springboot.webflux.app.models.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoDao productoDao;

    @GetMapping({"/listar", "/"})
    public String listar(Model model) {
        Flux<Producto> productos = productoDao.findAll(); //aca no es necesario hacer el subscribe ya que thymeleaf lo hace por debajo (es decir muestra los datos en una plantilla con thymeleaf, es decir la plantilla thymeleaf es el observador que se suscribe a este observable)

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de Productos");

        return "listar";
    }
}
