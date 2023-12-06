package springboot.onlinebookstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springboot.onlinebookstore.dto.user.request.UserRegistrationRequestDto;
import springboot.onlinebookstore.dto.user.response.UserResponseDto;
import springboot.onlinebookstore.exception.EntityNotFoundException;
import springboot.onlinebookstore.exception.RegistrationException;
import springboot.onlinebookstore.mapper.UserMapper;
import springboot.onlinebookstore.model.User;
import springboot.onlinebookstore.repository.user.UserRepository;
import springboot.onlinebookstore.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RegistrationException("User with such email is already present. "
                    + "Please, enter another email");
        }
        User user = userMapper.toModel(requestDto);
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    public UserResponseDto getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toUserResponseDto)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can't find user by email: " + email));
    }
}
