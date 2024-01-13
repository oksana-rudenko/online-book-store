package springboot.onlinebookstore.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import springboot.onlinebookstore.config.MapperConfig;
import springboot.onlinebookstore.dto.order.request.OrderRequestDto;
import springboot.onlinebookstore.dto.order.response.OrderResponseDto;
import springboot.onlinebookstore.dto.orderitem.OrderItemResponseDto;
import springboot.onlinebookstore.model.Order;

@Mapper(config = MapperConfig.class)
public interface OrderMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "orderItems", ignore = true)
    OrderResponseDto toDto(Order order);

    @AfterMapping
    default void setOrderItemsToOrderDto(@MappingTarget OrderResponseDto responseDto,
                                         Order order) {
        if (order.getOrderItems() != null) {
            Set<OrderItemResponseDto> orderItems = order.getOrderItems().stream()
                    .map(i -> new OrderItemResponseDto(i.getId(),
                            i.getBook().getId(), i.getQuantity()))
                    .collect(Collectors.toSet());
            responseDto.setOrderItems(orderItems);
        }
    }

    Order toEntity(OrderRequestDto requestDto);
}
