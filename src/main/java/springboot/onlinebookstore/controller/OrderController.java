package springboot.onlinebookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.onlinebookstore.dto.order.request.OrderRequestDto;
import springboot.onlinebookstore.dto.order.request.OrderStatusRequestDto;
import springboot.onlinebookstore.dto.order.response.OrderResponseDto;
import springboot.onlinebookstore.dto.orderitem.OrderItemResponseDto;
import springboot.onlinebookstore.model.User;
import springboot.onlinebookstore.service.OrderService;

@Tag(name = "Order management", description = "Endpoints for managing orders")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/orders")
public class OrderController {
    private final OrderService orderService;

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping
    @Operation(summary = "Place an order",
            description = "Placing an order for purchasing books")
    public OrderResponseDto placeOrder(
            Authentication authentication, @RequestBody @Valid OrderRequestDto requestDto
    ) {
        User user = (User) authentication.getPrincipal();
        return orderService.addOrder(user.getId(), requestDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping
    @Operation(summary = "Get an information about user's orders",
            description = "Retrieving orders from user's order history")
    public List<OrderResponseDto> getOrders(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return orderService.getUserOrders(user.getId());
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{orderId}/items")
    @Operation(summary = "Get order item's details",
            description = "Retrieving items from user's order")
    public List<OrderItemResponseDto> getOrderItems(Authentication authentication,
                                                    @PathVariable Long orderId) {
        User user = (User) authentication.getPrincipal();
        return orderService.getOrderItems(user.getId(), orderId);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{orderId}/items/{id}")
    @Operation(summary = "Get order item details",
            description = "Retrieving an information about item from user's order")
    public OrderItemResponseDto getItemFromOrder(
            Authentication authentication, @PathVariable Long orderId, @PathVariable Long id
    ) {
        User user = (User) authentication.getPrincipal();
        return orderService.getItem(user.getId(), orderId, id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    @Operation(summary = "Update order status",
            description = "Updating user's order status to manage processing workflow")
    public OrderResponseDto updateOrderStatus(
            @PathVariable Long id, @RequestBody @Valid OrderStatusRequestDto requestDto
    ) {
        return orderService.updateOrderStatus(id, requestDto);
    }
}
