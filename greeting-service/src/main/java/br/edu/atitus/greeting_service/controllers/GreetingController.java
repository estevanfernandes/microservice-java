package br.edu.atitus.greeting_service.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class GreetingController {

    @Value("${greeting-service.greeting}")
    private String greeting;

    @Value("${greeting-service.default-name}")
    private String defaultName;

    @GetMapping("/greeting/{name}")
    public String greeting(@PathVariable String name) {
        return String.format("%s, %s!!!", greeting, name);
    }

    @GetMapping("/greeting")
    public String greetingDefault() {
        return String.format("%s, %s!!!", greeting, defaultName);
    }

    @PostMapping("/greeting")
    public String greetingPost(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", defaultName);
        return String.format("%s, %s!!!", greeting, name);
    }
}
