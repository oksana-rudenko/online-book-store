package springboot.onlinebookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.onlinebookstore.dto.user.request.UserRegistrationRequestDto;
import springboot.onlinebookstore.dto.user.response.UserResponseDto;
import springboot.onlinebookstore.exception.RegistrationException;
import springboot.onlinebookstore.service.UserService;

@Tag(name = "User registration", description = "Endpoints for registration new user")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/auth/registration")
public class AuthenticationController {
    private final UserService userService;

    @PostMapping
    @Operation(summary = "New user registration",
            description = "Creating a new user account in the book store data base")
    public UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto request)
            throws RegistrationException {
        return userService.register(request);
    }
}
