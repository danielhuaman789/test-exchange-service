package pe.test.exchange.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Conversion(
        UUID id,
        BigDecimal monto,
        Currency monedaOrigen,
        Currency monedaDestino,
        BigDecimal tipoCambio,
        BigDecimal montoConvertido,
        Instant fechaOperacion
) {

    public Conversion {
        Objects.requireNonNull(id, "El identificador es obligatorio");
        Objects.requireNonNull(monto, "El monto es obligatorio");
        Objects.requireNonNull(monedaOrigen, "La moneda origen es obligatoria");
        Objects.requireNonNull(monedaDestino, "La moneda destino es obligatoria");
        Objects.requireNonNull(tipoCambio, "El tipo de cambio es obligatorio");
        Objects.requireNonNull(montoConvertido, "El resultado es obligatorio");
        Objects.requireNonNull(fechaOperacion, "La fecha es obligatoria");

        if (monto.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero"
            );
        }

        if (tipoCambio.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El tipo de cambio debe ser mayor que cero"
            );
        }

        BigDecimal resultadoEsperado = calcular(
                monto, tipoCambio, monedaDestino
        );

        if (montoConvertido.compareTo(resultadoEsperado) != 0) {
            throw new IllegalArgumentException(
                    "El monto convertido no coincide con el cálculo"
            );
        }
    }

    public static Conversion crear(
            BigDecimal monto,
            Currency monedaOrigen,
            Currency monedaDestino,
            BigDecimal tipoCambio
    ) {
        Objects.requireNonNull(monto, "El monto es obligatorio");
        Objects.requireNonNull(tipoCambio, "El tipo de cambio es obligatorio");
        Objects.requireNonNull(monedaDestino, "La moneda destino es obligatoria");

        return new Conversion(
                UUID.randomUUID(),
                monto,
                monedaOrigen,
                monedaDestino,
                tipoCambio,
                calcular(monto, tipoCambio, monedaDestino),
                Instant.now()
        );
    }

    private static BigDecimal calcular(
            BigDecimal monto,
            BigDecimal tipoCambio,
            Currency monedaDestino
    ) {
        int decimales = java.util.Currency
                .getInstance(monedaDestino.code())
                .getDefaultFractionDigits();

        if (decimales < 0) {
            throw new IllegalArgumentException(
                    "La moneda destino no admite importes monetarios"
            );
        }

        return monto.multiply(tipoCambio)
                .setScale(decimales, RoundingMode.HALF_UP);
    }
}