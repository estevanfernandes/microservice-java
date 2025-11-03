package br.edu.atitus.currency_service.controllers;

import br.edu.atitus.currency_service.clients.CurrencyBcClient;
import br.edu.atitus.currency_service.clients.CurrencyBcResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.currency_service.entities.CurrencyEntity;
import br.edu.atitus.currency_service.repositories.CurrencyRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("currency")
public class CurrencyController {

    private final CurrencyRepository repository;
    private final CurrencyBcClient currencyBcClient;
    private final CacheManager cacheManager;

    @Value("${server.port}")
    private int serverPort;

    public CurrencyController(CurrencyRepository repository, CurrencyBcClient currencyBcClient, CacheManager cacheManager) {
        super();
        this.repository = repository;
        this.currencyBcClient = currencyBcClient;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/{value}/{source}/{target}")
    public ResponseEntity<CurrencyEntity> getConversion(
            @PathVariable double value,
            @PathVariable String source,
            @PathVariable String target) throws Exception {

        source = source.toUpperCase();
        target = target.toUpperCase();
        String dataSource; // Não inicializamos mais com "None"

        String nameCache = "Currency";
        String keyCache = source + target;

        CurrencyEntity currency = cacheManager.getCache(nameCache).get(keyCache, CurrencyEntity.class);

        if (currency != null) {
            dataSource = "Cache";
        } else {
            currency = new CurrencyEntity();
            currency.setSource(source);
            currency.setTarget(target);

            if (source.equals(target)) {
                currency.setConversionRate(1);
                dataSource = "N/A"; // Não precisou de fonte de dados
            } else {
                // Lógica da data permanece a mesma
                LocalDate lastBusinessDay = LocalDate.now();
                if (lastBusinessDay.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    lastBusinessDay = lastBusinessDay.minusDays(1);
                } else if (lastBusinessDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    lastBusinessDay = lastBusinessDay.minusDays(2);
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                String quoteDate = lastBusinessDay.format(formatter);

                // --- INÍCIO DA LÓGICA ALTERADA ---

                // Chamamos o Feign Client diretamente, sem try-catch para o fallback
                CurrencyBcResponse responseSource = source.equals("BRL") ? null : currencyBcClient.getCurrency(source, quoteDate);
                CurrencyBcResponse responseTarget = target.equals("BRL") ? null : currencyBcClient.getCurrency(target, quoteDate);

                // Determinamos a fonte dos dados com base na resposta do client
                // Se qualquer uma das respostas veio do fallback, consideramos "Local Database"
                dataSource = (responseSource != null && "Local Database".equals(responseSource.getDataSource())) ||
                        (responseTarget != null && "Local Database".equals(responseTarget.getDataSource()))
                        ? "Local Database" : "API BCB";

                if ("API BCB".equals(dataSource)) {
                    double currencySource = 1;
                    double currencyTarget = 1;

                    if (responseSource != null) {
                        if (responseSource.getValue().isEmpty()) throw new Exception("Currency not found " + source);
                        currencySource = responseSource.getValue().get(0).getCotacaoVenda();
                    }
                    if (responseTarget != null) {
                        if (responseTarget.getValue().isEmpty()) throw new Exception("Currency not found " + target);
                        currencyTarget = responseTarget.getValue().get(0).getCotacaoVenda();
                    }
                    currency.setConversionRate(currencySource / currencyTarget);
                } else {
                    currency = repository.findBySourceAndTarget(source, target)
                            .orElseThrow(() -> new Exception("Currency Unsupported"));
                }
            }
            cacheManager.getCache(nameCache).put(keyCache, currency);
        }

        currency.setConvertedValue(value * currency.getConversionRate());
        currency.setEnviroment("Currency running in port: " + serverPort + " - DataSource: " + dataSource);

        return ResponseEntity.ok(currency);
    }
}