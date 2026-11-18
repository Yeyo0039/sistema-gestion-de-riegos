package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


// Controlador de la página web estática del sistema.
@Controller
public class PageController {

    // Redirige la raíz del servidor al archivo HTML ubicado en static.
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}
