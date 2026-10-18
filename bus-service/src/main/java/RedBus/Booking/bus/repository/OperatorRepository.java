package RedBus.Booking.bus.repository;

import RedBus.Booking.bus.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatorRepository  extends JpaRepository<Operator, Long> {
    boolean existsByLicenseNumber(String licenseNumber);
}
