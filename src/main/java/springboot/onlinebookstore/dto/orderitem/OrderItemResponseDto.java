package springboot.onlinebookstore.dto.orderitem;

public record OrderItemResponseDto(Long id, Long bookId, Integer quantity) {
}
