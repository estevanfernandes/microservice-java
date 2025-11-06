package br.edu.atitus.currency_service.clients;

import java.util.List;

public class CurrencyBCResponse {

    private List<Values> value;

    public List<Values> getValue() {
        return value;
    }

    public void setValue(List<Values> value) {
        this.value = value;
    }

    public static class Values{
        private double cotacaoVenda;

        public double getCotacaoVenda() {
            return cotacaoVenda;
        }

        public void setCotacaoVenda(double cotacaoVenda) {
            this.cotacaoVenda = cotacaoVenda;
        }

    }


}