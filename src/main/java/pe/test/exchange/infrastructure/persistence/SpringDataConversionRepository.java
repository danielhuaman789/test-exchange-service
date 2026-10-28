package pe.test.exchange.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataConversionRepository
        extends JpaRepository<ConversionEntity, UUID> {
}
