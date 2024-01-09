package springboot.onlinebookstore.dto.cartitem.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequestDto(
        @NotNull
        Long bookId,
        @NotNull
        @Min(1)
        Integer quantity) {
}
