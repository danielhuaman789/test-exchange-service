package pe.test.exchange.domain.service;

import pe.test.exchange.domain.model.Currency;

import java.math.BigDecimal;

public interface ExchangeRateProvider {

    BigDecimal obtenerTipoCambio(
            Currency monedaOrigen,
            Currency monedaDestino
    );
}
