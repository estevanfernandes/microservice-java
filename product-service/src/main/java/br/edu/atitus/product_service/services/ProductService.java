package br.edu.atitus.product_service.services;

import br.edu.atitus.product_service.clients.CurrencyClient;
import br.edu.atitus.product_service.clients.CurrencyResponse;
import br.edu.atitus.product_service.entities.ProductEntity;
import br.edu.atitus.product_service.repositories.ProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.cache.annotation.Cacheable;


@Service
public class ProductService {

    private final ProductRepository repository;
    private final CurrencyClient currencyClient;

    @Value("${server.port}")
    private int serverPort;

    public ProductService(ProductRepository repository, CurrencyClient currencyClient) {
        this.repository = repository;
        this.currencyClient = currencyClient;
    }

    @Cacheable(value = "product", key = "#id + '-' + #targetCurrency")
    @CircuitBreaker(name = "default", fallbackMethod = "getProductByIdFallback")
    public ProductEntity getProductById(Long id, String targetCurrency) throws Exception {
        ProductEntity product = repository.findById(id)
                .orElseThrow(() -> new Exception("Product not found"));

        String originalEnvironment = "Product-service running on Port: " + serverPort;

        if (targetCurrency.equalsIgnoreCase(product.getCurrency())) {
            product.setConvertedPrice(product.getPrice());
            product.setEnviroment(originalEnvironment);
        } else {
            CurrencyResponse currency = currencyClient.getCurrency(
                    product.getPrice(),
                    product.getCurrency(),
                    targetCurrency);
            product.setConvertedPrice(currency.getConvertedValue());
            product.setEnviroment(originalEnvironment + " --- " + currency.getEnviroment());
        }
        return product;
    }

    public ProductEntity getProductByIdFallback(Long id, String targetCurrency, Throwable t) throws Exception {
        System.err.println("CIRCUIT BREAKER: Entrando no fallback do ProductService. Causa -> " + t.getMessage());

        ProductEntity product = repository.findById(id)
                .orElseThrow(() -> new Exception("Product not found"));

        product.setEnviroment("Product-service running on Port: " + serverPort + " --- Currency Service OFFLINE");
        product.setConvertedPrice(-1.0);
        return product;
    }
}