
package Red.Booking.repository;

import Red.Booking.model.users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface  UserRepository extends JpaRepository<users, Long> {
    Optional<users> findByGmail(String gmail);
}

