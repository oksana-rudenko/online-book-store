package springboot.onlinebookstore.dto.category.response;

public record CategoryResponseDto(
        Long id,
        String name,
        String description
) {
}
