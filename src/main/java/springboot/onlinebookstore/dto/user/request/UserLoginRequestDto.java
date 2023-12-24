package springboot.onlinebookstore.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UserLoginRequestDto(
        @NotNull
        @Email
        String email,
        @NotNull
        @Length(min = 8, max = 100)
        String password
) {
}
