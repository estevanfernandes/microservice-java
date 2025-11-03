
package br.edu.atitus.currency_service.clients;

import org.springframework.stereotype.Component;
import java.util.Collections;

@Component
public class CurrencyBcFallback implements CurrencyBcClient {

    @Override
    public CurrencyBcResponse getCurrency(String moeda, String dataCotacao) {
        CurrencyBcResponse fallbackResponse = new CurrencyBcResponse();
        fallbackResponse.setValue(Collections.emptyList());
        fallbackResponse.setDataSource("Local Database");
        return fallbackResponse;
    }
}
