package springboot.onlinebookstore.dto.order.request;

import jakarta.validation.constraints.NotNull;
import springboot.onlinebookstore.model.Order;

public record OrderStatusRequestDto(@NotNull Order.Status status) {
}
