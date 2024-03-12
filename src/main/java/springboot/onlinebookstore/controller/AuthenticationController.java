package springboot.onlinebookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springboot.onlinebookstore.dto.user.request.UserLoginRequestDto;
import springboot.onlinebookstore.dto.user.request.UserRegistrationRequestDto;
import springboot.onlinebookstore.dto.user.response.UserLoginResponseDto;
import springboot.onlinebookstore.dto.user.response.UserResponseDto;
import springboot.onlinebookstore.exception.RegistrationException;
import springboot.onlinebookstore.security.AuthenticationService;
import springboot.onlinebookstore.service.UserService;

@Tag(name = "User registration and authentication",
        description = "Endpoints for registration new user and validation existing user")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/auth")
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    @Operation(summary = "New user registration",
            description = "Creating a new user account in the book store data base")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto request)
            throws RegistrationException {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "User's validation",
            description = "Endpoint for existing user validation in the book store application")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto request) {
        return authenticationService.authenticate(request);
    }
}
