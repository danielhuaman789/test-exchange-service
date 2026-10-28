package pe.test.exchange.application.service;

import org.springframework.stereotype.Service;
import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Currency;
import pe.test.exchange.domain.repository.ConversionRepository;
import pe.test.exchange.domain.service.ExchangeRateProvider;

import java.math.BigDecimal;

@Service
public class ConversionApplicationService {

    private final ExchangeRateProvider exchangeRateProvider;
    private final ConversionRepository conversionRepository;

    public ConversionApplicationService(
            ExchangeRateProvider exchangeRateProvider,
            ConversionRepository conversionRepository
    ) {
        this.exchangeRateProvider = exchangeRateProvider;
        this.conversionRepository = conversionRepository;
    }

    public ConversionResponse convertir(ConversionRequest request) {
        validarMonto(request);

        Currency origen = new Currency(request.monedaOrigen());
        Currency destino = new Currency(request.monedaDestino());

        validarMoneda(origen);
        validarMoneda(destino);

        BigDecimal tipoCambio = exchangeRateProvider
                .obtenerTipoCambio(origen, destino);

        Conversion conversion = Conversion.crear(
                request.monto(),
                origen,
                destino,
                tipoCambio
        );

        conversionRepository.save(conversion);

        return new ConversionResponse(
                conversion.monto(),
                conversion.montoConvertido(),
                conversion.monedaOrigen().code(),
                conversion.monedaDestino().code(),
                conversion.tipoCambio()
        );
    }

    private void validarMonto(ConversionRequest request) {
        if (request == null || request.monto() == null) {
            throw new IllegalArgumentException(
                    "El monto es obligatorio"
            );
        }

        BigDecimal monto = request.monto();

        if (monto.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero"
            );
        }

        // Límites compatibles con la columna de persistencia.
        int decimales = Math.max(monto.stripTrailingZeros().scale(), 0);
        int enteros = monto.precision() - monto.scale();

        if (decimales > 10 || enteros > 20) {
            throw new IllegalArgumentException(
                    "El monto admite hasta 20 enteros y 10 decimales"
            );
        }
    }

    private void validarMoneda(Currency moneda) {
        try {
            int decimales = java.util.Currency
                    .getInstance(moneda.code())
                    .getDefaultFractionDigits();

            if (decimales < 0) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Moneda no admitida: " + moneda.code()
            );
        }
    }
}
