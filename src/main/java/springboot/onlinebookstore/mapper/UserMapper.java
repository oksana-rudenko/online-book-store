package springboot.onlinebookstore.mapper;

import org.mapstruct.Mapper;
import springboot.onlinebookstore.config.MapperConfig;
import springboot.onlinebookstore.dto.user.request.UserRegistrationRequestDto;
import springboot.onlinebookstore.dto.user.response.UserResponseDto;
import springboot.onlinebookstore.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserRegistrationRequestDto requestDto);

    UserResponseDto toUserResponseDto(User user);
}
