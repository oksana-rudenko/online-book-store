package springboot.onlinebookstore.repository.role;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import springboot.onlinebookstore.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(Role.RoleName name);
}
