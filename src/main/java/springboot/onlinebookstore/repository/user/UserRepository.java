package springboot.onlinebookstore.repository.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import springboot.onlinebookstore.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
