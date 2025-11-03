
package br.edu.atitus.currency_service.clients;

import java.util.List;

public class CurrencyBcResponse {

    private List<CurrencyBC> value;
    private String dataSource = "API BCB";

    public static class CurrencyBC{
        private double cotacaoVenda;

        public double getCotacaoVenda() {
            return cotacaoVenda;
        }

        public void setCotacaoVenda(double cotacaoVenda) {
            this.cotacaoVenda = cotacaoVenda;
        }
    }

    public List<CurrencyBC> getValue() {
        return value;
    }

    public void setValue(List<CurrencyBC> value) {
        this.value = value;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
