package springboot.onlinebookstore.service;

import springboot.onlinebookstore.dto.user.request.UserRegistrationRequestDto;
import springboot.onlinebookstore.dto.user.response.UserResponseDto;
import springboot.onlinebookstore.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserResponseDto getByEmail(String email);
}
