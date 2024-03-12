package springboot.onlinebookstore.service.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import springboot.onlinebookstore.dto.user.request.UserRegistrationRequestDto;
import springboot.onlinebookstore.dto.user.response.UserResponseDto;
import springboot.onlinebookstore.exception.EntityNotFoundException;
import springboot.onlinebookstore.exception.RegistrationException;
import springboot.onlinebookstore.mapper.UserMapper;
import springboot.onlinebookstore.model.Role;
import springboot.onlinebookstore.model.User;
import springboot.onlinebookstore.repository.role.RoleRepository;
import springboot.onlinebookstore.repository.user.UserRepository;
import springboot.onlinebookstore.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "bobSmith@example.com";
    private static final String USER_PASSWORD = "12345678";
    private static final String USER_FIRST_NAME = "Robert";
    private static final String USER_LAST_NAME = "Smith";
    private static final String USER_ADDRESS = "Golden St, 12, L.A., USA";
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Register new user, returns valid user response dto")
    void register_ValidData_ReturnsValidResponseDto() throws RegistrationException {
        UserRegistrationRequestDto userRegistrationRequestDto = getUserRegistrationRequestDto();
        Role userRole = new Role();
        userRole.setName(Role.RoleName.USER);
        UserResponseDto userResponseDto = new UserResponseDto(USER_ID,
                USER_EMAIL, USER_FIRST_NAME, USER_LAST_NAME, USER_ADDRESS);
        User user = getUser();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userRegistrationRequestDto.getPassword()))
                .thenReturn(userRegistrationRequestDto.getPassword());
        when(roleRepository.findByName(any())).thenReturn(Optional.of(userRole));
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);
        UserResponseDto actual = userService.register(userRegistrationRequestDto);
        Assertions.assertEquals(userResponseDto, actual);
        verify(userRepository, times(1)).findByEmail(any());
        verify(passwordEncoder, times(1)).encode(any());
        verify(roleRepository, times(1)).findByName(any());
        verify(userMapper, times(1)).toUserResponseDto(any());
    }

    @Test
    @DisplayName("Register existing user, returns RegistrationException")
    void register_ExistingUser_ReturnsException() throws RegistrationException {
        User user = getUser();
        UserRegistrationRequestDto userRegistrationRequestDto = getUserRegistrationRequestDto();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        RegistrationException exception = assertThrows(RegistrationException.class,
                () -> userService.register(userRegistrationRequestDto));
        String expected = "User with such email is already present. "
                + "Please, enter another email";
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(userRepository, times(1)).findByEmail(any());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(roleRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Get existing user by valid email, returns valid user response dto")
    void getByEmail_ExistingUserValidEmail_ReturnsValidUser() {
        User user = getUser();
        UserResponseDto userResponseDto = new UserResponseDto(USER_ID,
                USER_EMAIL, USER_FIRST_NAME, USER_LAST_NAME, USER_ADDRESS);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);
        UserResponseDto actual = userService.getByEmail(USER_EMAIL);
        Assertions.assertEquals(userResponseDto, actual);
        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(userMapper, times(1)).toUserResponseDto(user);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(userMapper);
    }

    @Test
    @DisplayName("Get user by unknown email, returns exception")
    void getByEmail_UserNotFound_ReturnsException() {
        String email = "bob@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.getByEmail(email));
        String expected = "Can't find user by email: " + email;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
    }

    private User getUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        user.setPassword(USER_PASSWORD);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setShippingAddress(USER_ADDRESS);
        Role userRole = new Role();
        userRole.setName(Role.RoleName.USER);
        userRole.setId(1L);
        user.setRoles(Set.of(userRole));
        user.setDeleted(false);
        return user;
    }

    private UserRegistrationRequestDto getUserRegistrationRequestDto() {
        UserRegistrationRequestDto userRegistrationRequestDto = new UserRegistrationRequestDto();
        userRegistrationRequestDto.setEmail(USER_EMAIL);
        userRegistrationRequestDto.setPassword(USER_PASSWORD);
        userRegistrationRequestDto.setRepeatPassword(USER_PASSWORD);
        userRegistrationRequestDto.setFirstName(USER_FIRST_NAME);
        userRegistrationRequestDto.setLastName(USER_LAST_NAME);
        userRegistrationRequestDto.setShippingAddress(USER_ADDRESS);
        return userRegistrationRequestDto;
    }
}
