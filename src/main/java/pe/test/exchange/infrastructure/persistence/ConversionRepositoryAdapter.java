package pe.test.exchange.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.repository.ConversionRepository;

@Repository
public class ConversionRepositoryAdapter
        implements ConversionRepository {

    private final SpringDataConversionRepository repository;

    public ConversionRepositoryAdapter(
            SpringDataConversionRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Conversion save(Conversion conversion) {
        ConversionEntity entity = new ConversionEntity(
                conversion.id(),
                conversion.monto(),
                conversion.monedaOrigen().code(),
                conversion.monedaDestino().code(),
                conversion.tipoCambio(),
                conversion.montoConvertido(),
                conversion.fechaOperacion()
        );

        repository.saveAndFlush(entity);

        return conversion;
    }
}
