package br.edu.atitus.currency_service.controllers;

import br.edu.atitus.currency_service.entities.CurrencyEntity;
import br.edu.atitus.currency_service.services.CurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("currency")
public class CurrencyController {

    private final CurrencyService service;

    public CurrencyController(CurrencyService service) {
        this.service = service;
    }

    @GetMapping("/{value}/{source}/{target}")
    public ResponseEntity<CurrencyEntity> getConversion(
            @PathVariable double value,
            @PathVariable String source,
            @PathVariable String target) {

        CurrencyEntity currency = service.getConversion(value, source, target);
        return ResponseEntity.ok(currency);
    }
}