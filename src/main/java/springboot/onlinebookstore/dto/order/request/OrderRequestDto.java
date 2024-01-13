package springboot.onlinebookstore.dto.order.request;

import jakarta.validation.constraints.NotEmpty;

public record OrderRequestDto(@NotEmpty String shippingAddress) {
}
