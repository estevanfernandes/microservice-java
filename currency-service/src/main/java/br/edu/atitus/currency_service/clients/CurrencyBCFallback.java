package br.edu.atitus.currency_service.clients;import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class CurrencyBCFallback implements CurrencyBCClient{

    @Override
    public CurrencyBCResponse getCurrencyBC(String moeda) {
        CurrencyBCResponse currency = new CurrencyBCResponse();
        currency.setValue(new ArrayList());
        return currency;
    }

}