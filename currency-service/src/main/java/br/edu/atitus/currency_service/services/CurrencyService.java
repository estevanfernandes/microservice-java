package br.edu.atitus.currency_service.services;

import br.edu.atitus.currency_service.clients.BcbApiClient;
import br.edu.atitus.currency_service.entities.CurrencyEntity;
import br.edu.atitus.currency_service.repositories.CurrencyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.cache.annotation.Cacheable;

@Service
public class CurrencyService {

    private final BcbApiClient bcbApiClient;
    private final ObjectMapper objectMapper;
    private final CurrencyRepository repository;

    public CurrencyService(BcbApiClient bcbApiClient, ObjectMapper objectMapper, CurrencyRepository repository) {
        this.bcbApiClient = bcbApiClient;
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @Cacheable(value = "currency", key = "#source + '-' + #target")
    @CircuitBreaker(name = "default", fallbackMethod = "getConversionFallback")
    public CurrencyEntity getConversion(double value, String source, String target) {
        try {
            String cotacaoDoDia = fetchCotacaoFromBcb(source);

            JsonNode root = objectMapper.readTree(cotacaoDoDia);
            JsonNode valueNode = root.path("value");

            if (valueNode.isEmpty() || !valueNode.isArray() || valueNode.get(0) == null) {
                throw new RuntimeException("Cotação não encontrada na API do BCB para a moeda: " + source);
            }
            double conversionRate = valueNode.get(0).path("cotacaoVenda").asDouble();

            CurrencyEntity currency = new CurrencyEntity();
            currency.setSource(source);
            currency.setTarget(target);
            currency.setConversionRate(conversionRate);
            currency.setConvertedValue(value * conversionRate);
            currency.setEnviroment("API BCB");
            return currency;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CurrencyEntity getConversionFallback(double value, String source, String target, Throwable t) {
        System.err.println("CIRCUIT BREAKER: Entrando no método de Fallback. Causa -> " + t.getMessage());

        CurrencyEntity currency = repository
                .findBySourceAndTarget(source, target)
                .orElseThrow(() -> new RuntimeException("Moeda não encontrada no banco de dados local: " + source));

        currency.setConvertedValue(value * currency.getConversionRate());
        currency.setEnviroment("Banco Local");
        return currency;
    }

    private String fetchCotacaoFromBcb(String currencyCode) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        String formattedDate = today.format(formatter);

        return bcbApiClient.getCotacao(currencyCode, formattedDate, "json");
    }
}