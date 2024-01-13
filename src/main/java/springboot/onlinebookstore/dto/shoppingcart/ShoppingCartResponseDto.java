package springboot.onlinebookstore.dto.shoppingcart;

import java.util.Set;
import lombok.Data;
import springboot.onlinebookstore.dto.cartitem.response.CartItemResponseDto;

@Data
public class ShoppingCartResponseDto {
    private Long id;
    private Long userId;
    private Set<CartItemResponseDto> cartItems;
}
