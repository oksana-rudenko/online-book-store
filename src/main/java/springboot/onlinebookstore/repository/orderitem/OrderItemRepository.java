package springboot.onlinebookstore.repository.orderitem;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot.onlinebookstore.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
