
package br.edu.atitus.currency_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CurrencyBcClient", url = "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata", fallback = CurrencyBcFallback.class)
public interface CurrencyBcClient {

    @GetMapping("/CotacaoMoedaDia(moeda='{moeda}',dataCotacao='{dataCotacao}')?$format=json")
    CurrencyBcResponse getCurrency(
            @PathVariable("moeda") String moeda,
            @PathVariable("dataCotacao") String dataCotacao
    );
}
