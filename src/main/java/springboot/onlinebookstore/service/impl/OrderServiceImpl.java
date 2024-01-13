package springboot.onlinebookstore.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springboot.onlinebookstore.dto.order.request.OrderRequestDto;
import springboot.onlinebookstore.dto.order.request.OrderStatusRequestDto;
import springboot.onlinebookstore.dto.order.response.OrderResponseDto;
import springboot.onlinebookstore.dto.orderitem.OrderItemResponseDto;
import springboot.onlinebookstore.exception.EntityNotFoundException;
import springboot.onlinebookstore.mapper.OrderItemMapper;
import springboot.onlinebookstore.mapper.OrderMapper;
import springboot.onlinebookstore.model.Book;
import springboot.onlinebookstore.model.CartItem;
import springboot.onlinebookstore.model.Order;
import springboot.onlinebookstore.model.OrderItem;
import springboot.onlinebookstore.model.ShoppingCart;
import springboot.onlinebookstore.model.User;
import springboot.onlinebookstore.repository.book.BookRepository;
import springboot.onlinebookstore.repository.cartitem.CartItemRepository;
import springboot.onlinebookstore.repository.order.OrderRepository;
import springboot.onlinebookstore.repository.orderitem.OrderItemRepository;
import springboot.onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import springboot.onlinebookstore.service.OrderService;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public OrderResponseDto addOrder(Long userId, OrderRequestDto requestDto) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId).orElseThrow(() ->
                new EntityNotFoundException("Can't find your shopping cart"));
        Set<CartItem> cartItems = shoppingCart.getCartItems();
        if (cartItems == null || cartItems.size() == 0) {
            throw new EntityNotFoundException("You didn't choose any book to your shopping cart. "
                    + "Please, make your choice");
        }
        BigDecimal total = BigDecimal.valueOf(cartItems.stream()
                .map(i -> i.getBook().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .mapToInt(BigDecimal::intValue)
                .sum());
        Order order = orderMapper.toEntity(requestDto);
        order.setUser(new User(userId));
        order.setStatus(Order.Status.PENDING);
        order.setTotal(total);
        order.setOrderDate(LocalDateTime.now());
        orderRepository.save(order);
        Set<OrderItem> orderItems = cartItems.stream()
                .map(i -> createOrderItem(i, order))
                .collect(Collectors.toSet());
        order.setOrderItems(orderItems);
        return orderMapper.toDto(order);
    }

    private OrderItem createOrderItem(CartItem cartItem, Order order) {
        Book book = bookRepository.findById(cartItem.getBook().getId()).orElseThrow(() ->
                new EntityNotFoundException("Can't find book by id: "
                        + cartItem.getBook().getId()));
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setBook(book);
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(book.getPrice());
        orderItemRepository.save(orderItem);
        cartItemRepository.delete(cartItem);
        return orderItem;
    }

    @Override
    public List<OrderResponseDto> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderItemResponseDto> getOrderItems(Long userId, Long orderId) {
        Order order = orderRepository.findByUserId(userId).stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Can't find order by id: " + orderId));
        return order.getOrderItems().stream()
                .map(orderItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderItemResponseDto getItem(Long userId, Long orderId, Long id) {
        Order order = orderRepository.findByUserId(userId).stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Can't find order by id: " + orderId));
        Set<OrderItem> orderItems = order.getOrderItems();
        OrderItem orderItem = orderItems.stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Can't find item by id: " + id));
        return orderItemMapper.toDto(orderItem);
    }

    @Override
    public OrderResponseDto updateOrderStatus(Long id, OrderStatusRequestDto requestDto) {
        Order order = orderRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Can't find order with id: " + id));
        order.setStatus(requestDto.status());
        return orderMapper.toDto(orderRepository.save(order));
    }
}
