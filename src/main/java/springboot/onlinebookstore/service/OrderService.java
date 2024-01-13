package springboot.onlinebookstore.service;

import java.util.List;
import springboot.onlinebookstore.dto.order.request.OrderRequestDto;
import springboot.onlinebookstore.dto.order.request.OrderStatusRequestDto;
import springboot.onlinebookstore.dto.order.response.OrderResponseDto;
import springboot.onlinebookstore.dto.orderitem.OrderItemResponseDto;

public interface OrderService {
    OrderResponseDto addOrder(Long userId, OrderRequestDto requestDto);

    List<OrderResponseDto> getUserOrders(Long userId);

    List<OrderItemResponseDto> getOrderItems(Long userId, Long orderId);

    OrderItemResponseDto getItem(Long userId, Long orderId, Long id);

    OrderResponseDto updateOrderStatus(Long id, OrderStatusRequestDto requestDto);
}
