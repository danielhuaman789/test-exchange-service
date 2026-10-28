package pe.test.exchange.infrastructure.external;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.exception.UnsupportedCurrencyException;
import pe.test.exchange.domain.model.Currency;
import pe.test.exchange.domain.service.ExchangeRateProvider;
import pe.test.exchange.infrastructure.external.dto.ExchangeRateApiResponse;

import java.math.BigDecimal;

@Component
public class ExchangeRateProviderAdapter implements ExchangeRateProvider {

    private final ExchangeRateApiClient client;
    private final String apiKey;

    public ExchangeRateProviderAdapter(
            ExchangeRateApiClient client,
            @Value("${exchange-rate.api.key}") String apiKey
    ) {
        this.client = client;
        this.apiKey = apiKey;
    }

    @Override
    public BigDecimal obtenerTipoCambio(
            Currency monedaOrigen,
            Currency monedaDestino
    ) {
        ExchangeRateApiResponse response;

        try {
            response = client.getLatestRates(
                    apiKey,
                    monedaOrigen.code()
            );
        } catch (FeignException exception) {
            throw new ExchangeRateUnavailableException(
                    "No se pudo consultar el servicio de tipo de cambio",
                    exception
            );
        }

        if (response == null
                || !"success".equals(response.result())
                || !monedaOrigen.code().equals(response.baseCode())
                || response.conversionRates() == null) {
            throw new ExchangeRateUnavailableException(
                    "El proveedor devolvió una respuesta inválida"
            );
        }

        BigDecimal tipoCambio = response.conversionRates()
                .get(monedaDestino.code());

        if (tipoCambio == null) {
            throw new UnsupportedCurrencyException(
                    "Moneda destino no admitida: "
                            + monedaDestino.code()
            );
        }

        if (tipoCambio.signum() <= 0) {
            throw new ExchangeRateUnavailableException(
                    "El proveedor devolvió un tipo de cambio inválido"
            );
        }

        return tipoCambio;
    }
}
