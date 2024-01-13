package springboot.onlinebookstore.dto.cartitem.request;

import jakarta.validation.constraints.Min;

public record CartItemQuantityRequestDto(@Min(1) Integer quantity) {
}
