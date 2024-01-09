package springboot.onlinebookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.onlinebookstore.dto.cartitem.request.CartItemQuantityRequestDto;
import springboot.onlinebookstore.dto.cartitem.request.CartItemRequestDto;
import springboot.onlinebookstore.dto.shoppingcart.ShoppingCartResponseDto;
import springboot.onlinebookstore.model.User;
import springboot.onlinebookstore.service.ShoppingCartService;

@Tag(name = "Shopping cart management", description = "Endpoints for managing shopping cart")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping
    @Operation(summary = "Get user's shopping cart",
            description = "Getting info about user's shopping cart")
    public ShoppingCartResponseDto getByUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.getCartByUser(user.getId());
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping
    @Operation(summary = "Add a book to shopping cart",
            description = "Adding books to user's shopping cart")
    public ShoppingCartResponseDto addBookToShoppingCart(
            Authentication authentication,
            @RequestBody @Valid CartItemRequestDto requestDto
    ) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.addItemToCart(user.getId(), requestDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/cart-items/{cartItemId}")
    @Operation(summary = "Update quantity of book in shopping cart",
            description = "Update cart item's details in user's shopping cart")
    public ShoppingCartResponseDto updateCartItem(
            @PathVariable Long cartItemId,
            Authentication authentication,
            @RequestBody @Valid CartItemQuantityRequestDto requestDto
    ) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.updateItemQuantity(user.getId(), cartItemId, requestDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/cart-items/{cartItemId}")
    @Operation(summary = "Delete a cart item by its ID from user's cart",
            description = "Deleting a book's item by its ID from user's shopping cart")
    public ShoppingCartResponseDto removeBookFromShoppingCart(
            @PathVariable Long cartItemId, Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.removeBookFromShoppingCart(user.getId(), cartItemId);
    }
}
