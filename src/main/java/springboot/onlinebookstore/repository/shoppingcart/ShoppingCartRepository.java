package springboot.onlinebookstore.repository.shoppingcart;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import springboot.onlinebookstore.model.ShoppingCart;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    @Query("FROM ShoppingCart sc "
            + "LEFT JOIN FETCH sc.user u "
            + "WHERE u.id = :id")
    Optional<ShoppingCart> findByUserId(Long id);
}
