
package User.Servires.repository;

import User.Servires.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface  UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByGmail(String gmail);
}

