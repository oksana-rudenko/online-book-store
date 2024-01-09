package springboot.onlinebookstore.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import springboot.onlinebookstore.config.MapperConfig;
import springboot.onlinebookstore.dto.cartitem.response.CartItemResponseDto;
import springboot.onlinebookstore.dto.shoppingcart.ShoppingCartResponseDto;
import springboot.onlinebookstore.model.ShoppingCart;

@Mapper(config = MapperConfig.class)
public interface ShoppingCartMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "cartItems", ignore = true)
    ShoppingCartResponseDto toDto(ShoppingCart shoppingCart);

    @AfterMapping
    default void setCartItemsToShoppingCartDto(
            @MappingTarget ShoppingCartResponseDto responseDto, ShoppingCart shoppingCart
    ) {
        Set<CartItemResponseDto> cartItemDtos = shoppingCart.getCartItems().stream()
                .map(c -> new CartItemResponseDto(c.getId(),
                        c.getBook().getId(), c.getBook().getTitle(), c.getQuantity()))
                .collect(Collectors.toSet());
        responseDto.setCartItems(cartItemDtos);
    }
}
