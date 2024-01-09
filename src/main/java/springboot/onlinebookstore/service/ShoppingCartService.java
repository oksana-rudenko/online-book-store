package springboot.onlinebookstore.service;

import springboot.onlinebookstore.dto.cartitem.request.CartItemQuantityRequestDto;
import springboot.onlinebookstore.dto.cartitem.request.CartItemRequestDto;
import springboot.onlinebookstore.dto.shoppingcart.ShoppingCartResponseDto;

public interface ShoppingCartService {
    ShoppingCartResponseDto getCartByUser(Long userId);

    ShoppingCartResponseDto addItemToCart(Long userId, CartItemRequestDto requestDto);

    ShoppingCartResponseDto updateItemQuantity(
            Long userId, Long cartItemId, CartItemQuantityRequestDto requestDto);

    ShoppingCartResponseDto removeBookFromShoppingCart(Long userId, Long cartItemId);
}
