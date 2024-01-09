package springboot.onlinebookstore.dto.cartitem.response;

public record CartItemResponseDto(Long id, Long bookId, String bookTitle, Integer quantity) {
}
