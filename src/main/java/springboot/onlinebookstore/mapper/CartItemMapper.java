package springboot.onlinebookstore.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import springboot.onlinebookstore.config.MapperConfig;
import springboot.onlinebookstore.dto.cartitem.request.CartItemRequestDto;
import springboot.onlinebookstore.model.CartItem;

@Mapper(config = MapperConfig.class, uses = BookMapper.class)
public interface CartItemMapper {
    @Mapping(target = "book", source = "bookId", qualifiedByName = "bookFromId")
    CartItem toEntity(CartItemRequestDto requestDto);

}
