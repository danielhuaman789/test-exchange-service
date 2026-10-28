package pe.test.exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.application.service.ConversionApplicationService;
import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Currency;
import pe.test.exchange.domain.repository.ConversionRepository;
import pe.test.exchange.domain.service.ExchangeRateProvider;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConversionApplicationServiceTest {

    private ExchangeRateProvider provider;
    private ConversionRepository repository;
    private ConversionApplicationService service;

    @BeforeEach
    void setUp() {
        provider = mock(ExchangeRateProvider.class);
        repository = mock(ConversionRepository.class);
        service = new ConversionApplicationService(provider, repository);
    }

    @Test
    void convierteYGuardaLaOperacion() {
        when(provider.obtenerTipoCambio(
                new Currency("USD"), new Currency("PEN")
        )).thenReturn(new BigDecimal("3.4486"));

        ConversionResponse response = service.convertir(
                new ConversionRequest(
                        new BigDecimal("100"), "USD", "PEN"
                )
        );

        assertEquals(new BigDecimal("344.86"), response.montoConvertido());
        assertEquals(new BigDecimal("3.4486"), response.tipoCambio());
        assertEquals("USD", response.monedaOrigen());
        assertEquals("PEN", response.monedaDestino());

        var captor = org.mockito.ArgumentCaptor
                .forClass(Conversion.class);

        verify(repository).save(captor.capture());

        Conversion guardada = captor.getValue();
        assertEquals(response.montoConvertido(), guardada.montoConvertido());
        assertEquals(response.tipoCambio(), guardada.tipoCambio());
        assertNotNull(guardada.id());
        assertNotNull(guardada.fechaOperacion());
    }

    @Test
    void rechazaMontoNegativoSinConsultarNiGuardar() {
        assertThrows(IllegalArgumentException.class, () ->
                service.convertir(new ConversionRequest(
                        new BigDecimal("-10"), "USD", "PEN"
                ))
        );

        verifyNoInteractions(provider, repository);
    }

    @Test
    void rechazaMontoNuloSinConsultarNiGuardar() {
        assertThrows(IllegalArgumentException.class, () ->
                service.convertir(new ConversionRequest(
                        null, "USD", "PEN"
                ))
        );

        verifyNoInteractions(provider, repository);
    }

    @Test
    void normalizaLosCodigosDeMoneda() {
        when(provider.obtenerTipoCambio(
                new Currency("USD"), new Currency("PEN")
        )).thenReturn(new BigDecimal("3.70"));

        ConversionResponse response = service.convertir(
                new ConversionRequest(
                        new BigDecimal("100"), " usd ", "pen"
                )
        );

        assertEquals("USD", response.monedaOrigen());
        assertEquals("PEN", response.monedaDestino());
        assertEquals(new BigDecimal("370.00"), response.montoConvertido());
    }

    @Test
    void rechazaMonedaInvalidaSinConsultarNiGuardar() {
        assertThrows(IllegalArgumentException.class, () ->
                service.convertir(new ConversionRequest(
                        new BigDecimal("100"), "US", "PEN"
                ))
        );

        verifyNoInteractions(provider, repository);
    }

    @Test
    void noGuardaSiFallaElProveedor() {
        when(provider.obtenerTipoCambio(any(), any()))
                .thenThrow(new ExchangeRateUnavailableException(
                        "Proveedor no disponible"
                ));

        assertThrows(ExchangeRateUnavailableException.class, () ->
                service.convertir(new ConversionRequest(
                        new BigDecimal("100"), "USD", "PEN"
                ))
        );

        verifyNoInteractions(repository);
    }

    @Test
    void redondeaSegunLaMonedaDestino() {
        when(provider.obtenerTipoCambio(
                new Currency("USD"), new Currency("JPY")
        )).thenReturn(new BigDecimal("150.555"));

        ConversionResponse response = service.convertir(
                new ConversionRequest(
                        new BigDecimal("1"), "USD", "JPY"
                )
        );

        assertEquals(new BigDecimal("151"), response.montoConvertido());
    }
}
