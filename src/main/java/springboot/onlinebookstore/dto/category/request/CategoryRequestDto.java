package springboot.onlinebookstore.dto.category.request;

import jakarta.validation.constraints.NotEmpty;

public record CategoryRequestDto(
        @NotEmpty
        String name,
        String description
) {
}
