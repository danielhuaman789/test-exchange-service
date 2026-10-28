package pe.test.exchange.domain.model;

import java.util.Locale;

public record Currency(String code) {

    public Currency {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "La moneda es obligatoria"
            );
        }

        code = code.strip().toUpperCase(Locale.ROOT);

        if (!code.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException(
                    "La moneda debe contener tres letras, por ejemplo USD o PEN"
            );
        }
    }
}