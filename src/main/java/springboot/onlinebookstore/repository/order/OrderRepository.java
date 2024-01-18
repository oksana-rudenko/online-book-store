package springboot.onlinebookstore.repository.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import springboot.onlinebookstore.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long id);
}
