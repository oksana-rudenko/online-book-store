package springboot.onlinebookstore.repository.cartitem;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot.onlinebookstore.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
