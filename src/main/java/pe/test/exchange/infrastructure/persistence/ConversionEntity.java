package pe.test.exchange.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversiones")
public class ConversionEntity {

    @Id
    private UUID id;

    @Column(nullable = false, precision = 30, scale = 10)
    private BigDecimal monto;

    @Column(nullable = false, length = 3)
    private String monedaOrigen;

    @Column(nullable = false, length = 3)
    private String monedaDestino;

    @Column(nullable = false, precision = 30, scale = 15)
    private BigDecimal tipoCambio;

    @Column(nullable = false, precision = 38, scale = 10)
    private BigDecimal montoConvertido;

    @Column(nullable = false)
    private Instant fechaOperacion;

    protected ConversionEntity() {
        // Constructor requerido por JPA.
    }

    public ConversionEntity(
            UUID id,
            BigDecimal monto,
            String monedaOrigen,
            String monedaDestino,
            BigDecimal tipoCambio,
            BigDecimal montoConvertido,
            Instant fechaOperacion
    ) {
        this.id = id;
        this.monto = monto;
        this.monedaOrigen = monedaOrigen;
        this.monedaDestino = monedaDestino;
        this.tipoCambio = tipoCambio;
        this.montoConvertido = montoConvertido;
        this.fechaOperacion = fechaOperacion;
    }
}
