package RedBus.Booking.bus.repository;

import RedBus.Booking.bus.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    boolean existsByNameIgnoreCase(String name);
}
