package springboot.onlinebookstore.service.order;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import springboot.onlinebookstore.dto.order.request.OrderRequestDto;
import springboot.onlinebookstore.dto.order.request.OrderStatusRequestDto;
import springboot.onlinebookstore.dto.order.response.OrderResponseDto;
import springboot.onlinebookstore.dto.orderitem.OrderItemResponseDto;
import springboot.onlinebookstore.exception.EntityNotFoundException;
import springboot.onlinebookstore.mapper.OrderItemMapper;
import springboot.onlinebookstore.mapper.OrderMapper;
import springboot.onlinebookstore.model.Book;
import springboot.onlinebookstore.model.CartItem;
import springboot.onlinebookstore.model.Category;
import springboot.onlinebookstore.model.Order;
import springboot.onlinebookstore.model.OrderItem;
import springboot.onlinebookstore.model.ShoppingCart;
import springboot.onlinebookstore.model.User;
import springboot.onlinebookstore.repository.book.BookRepository;
import springboot.onlinebookstore.repository.cartitem.CartItemRepository;
import springboot.onlinebookstore.repository.order.OrderRepository;
import springboot.onlinebookstore.repository.orderitem.OrderItemRepository;
import springboot.onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import springboot.onlinebookstore.service.impl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    private static final Long USER_ID = 1L;
    @InjectMocks
    private OrderServiceImpl orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;

    @Test
    @DisplayName("Add order for existing user with valid data, returns valid response")
    void addOrder_ValidRequest_ReturnsValidResponseDto() {
        ShoppingCart shoppingCart = getShoppingCart();
        String shippingAddress = "Golden St, 12, L.A., USA";
        OrderRequestDto requestDto = new OrderRequestDto(shippingAddress);
        Order order = getOrder(requestDto);
        OrderResponseDto responseDto = getOrderResponseDto(order);
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(orderRepository.save(any())).thenReturn(order);
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(getBookOne()));
        when(orderItemRepository.save(any())).thenReturn(getOrderItem(order));
        doNothing().when(cartItemRepository).delete(any());
        when(orderMapper.toDto(any())).thenReturn(responseDto);
        OrderResponseDto actual = orderService.addOrder(USER_ID, requestDto);
        Assertions.assertEquals(responseDto, actual);
        verify(shoppingCartRepository, times(1)).findByUserId(anyLong());
        verify(orderRepository, times(1)).save(any());
        verify(bookRepository, times(1)).findById(anyLong());
        verify(orderItemRepository, times(1)).save(any());
        verify(cartItemRepository, times(1)).delete(any());
        verify(orderMapper, times(1)).toDto(any());
    }

    @Test
    @DisplayName("Add order for empty shopping cart, returns exception")
    void addOrder_EmptyCartItemsSet_ReturnsException() {
        ShoppingCart shoppingCart = getShoppingCart();
        shoppingCart.setCartItems(Set.of());
        String shippingAddress = "Golden St, 12, L.A., USA";
        OrderRequestDto requestDto = new OrderRequestDto(shippingAddress);
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.addOrder(USER_ID, requestDto));
        String expected = "You didn't choose any book to your shopping cart. "
                + "Please, make your choice";
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(shoppingCartRepository, times(1)).findByUserId(anyLong());
        verifyNoMoreInteractions(shoppingCartRepository);
        verifyNoInteractions(orderMapper);
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(orderItemRepository);
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("Get all user's orders by id, valid user's id, returns valid list")
    void getUserOrders_ValidData_ReturnsListOfOneOrder() {
        String shippingAddress = "Golden St, 12, L.A., USA";
        OrderRequestDto requestDto = new OrderRequestDto(shippingAddress);
        Order order = getOrder(requestDto);
        List<Order> orders = List.of(order);
        OrderResponseDto responseDto = getOrderResponseDto(order);
        List<OrderResponseDto> expected = List.of(responseDto);
        when(orderRepository.findByUserId(USER_ID)).thenReturn(orders);
        when(orderMapper.toDto(order)).thenReturn(responseDto);
        List<OrderResponseDto> actual = orderService.getUserOrders(USER_ID);
        Assertions.assertEquals(expected, actual);
        verify(orderRepository, times(1)).findByUserId(USER_ID);
        verify(orderMapper, times(1)).toDto(order);
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(orderMapper);
    }

    @Test
    @DisplayName("Get user's orders by id, orders do not exist, returns empty list")
    void getUserOrders_OrderNotExist_ReturnsEmptyList() {
        List<Order> orders = Collections.emptyList();
        List<OrderResponseDto> responseDtos = Collections.emptyList();
        when(orderRepository.findByUserId(USER_ID)).thenReturn(orders);
        List<OrderResponseDto> actual = orderService.getUserOrders(USER_ID);
        Assertions.assertEquals(responseDtos, actual);
        verify(orderRepository, times(1)).findByUserId(USER_ID);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderMapper);
    }

    @Test
    @DisplayName("Get order's items by user's id and order id, returns valid list")
    void getOrderItems_ValidData_ReturnsValidList() {
        String shippingAddress = "Golden St, 12, L.A., USA";
        OrderRequestDto requestDto = new OrderRequestDto(shippingAddress);
        Order order = getOrder(requestDto);
        OrderItem orderItem = getOrderItem(order);
        OrderItemResponseDto orderItemResponseDto =
                new OrderItemResponseDto(1L, getBookOne().getId(), orderItem.getQuantity());
        List<OrderItemResponseDto> expected = List.of(orderItemResponseDto);
        Long orderId = 1L;
        when(orderRepository.findByUserId(USER_ID)).thenReturn(List.of(order));
        when(orderItemMapper.toDto(orderItem)).thenReturn(orderItemResponseDto);
        List<OrderItemResponseDto> actual = orderService.getOrderItems(USER_ID, orderId);
        Assertions.assertEquals(expected, actual);
        verify(orderRepository, times(1)).findByUserId(USER_ID);
        verify(orderItemMapper, times(1)).toDto(orderItem);
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(orderItemMapper);
    }

    @Test
    @DisplayName("Get order's items, order does not exist, returns exception")
    void getOrderItems_OrderNotExist_ReturnsException() {
        Long orderId = 1L;
        when(orderRepository.findByUserId(USER_ID)).thenReturn(List.of());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.getOrderItems(USER_ID, orderId));
        String expected = "Can't find order by id: " + orderId;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(orderRepository, times(1)).findByUserId(USER_ID);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderItemMapper);
    }

    @Test
    @DisplayName("Get item from user's order by item id, returns valid order item")
    void getItem_ValidData_ReturnsOrderItem() {
        String shippingAddress = "Golden St, 12, L.A., USA";
        OrderRequestDto requestDto = new OrderRequestDto(shippingAddress);
        Order order = getOrder(requestDto);
        OrderItem orderItem = getOrderItem(order);
        Long orderId = 1L;
        Long itemId = 1L;
        OrderItemResponseDto orderItemResponseDto =
                new OrderItemResponseDto(itemId, getBookOne().getId(), orderItem.getQuantity());
        when(orderRepository.findByUserId(USER_ID)).thenReturn(List.of(order));
        when(orderItemMapper.toDto(orderItem)).thenReturn(orderItemResponseDto);
        OrderItemResponseDto actual = orderService.getItem(USER_ID, orderId, itemId);
        Assertions.assertEquals(orderItemResponseDto, actual);
        verify(orderRepository, times(1)).findByUserId(USER_ID);
        verify(orderItemMapper, times(1)).toDto(orderItem);
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(orderItemMapper);
    }

    @Test
    @DisplayName("Get item from user's order by item id, order does not exist, returns exception")
    void getItem_OrderNotExist_ReturnsException() {
        List<Order> orders = Collections.emptyList();
        Long orderId = 1L;
        Long itemId = 1L;
        when(orderRepository.findByUserId(USER_ID)).thenReturn(orders);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.getItem(USER_ID, orderId, itemId));
        String expected = "Can't find order by id: " + orderId;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(orderRepository, times(1)).findByUserId(USER_ID);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderItemMapper);
    }

    @Test
    @DisplayName("Get order item by id, order item does not exist, returns exception")
    void getItem_OrderItemNotExist_ReturnsException() {
        String shippingAddress = "Golden St, 12, L.A., USA";
        OrderRequestDto requestDto = new OrderRequestDto(shippingAddress);
        Order order = getOrder(requestDto);
        order.setOrderItems(Set.of());
        List<Order> orders = List.of(order);
        Long orderId = 1L;
        Long itemId = 1L;
        when(orderRepository.findByUserId(USER_ID)).thenReturn(orders);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.getItem(USER_ID, orderId, itemId));
        String expected = "Can't find item by id: " + itemId;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(orderRepository, times(1)).findByUserId(USER_ID);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderItemMapper);
    }

    @Test
    @DisplayName("Update order status by id, valid request, returns updated order")
    void updateOrderStatus_ValidData_ReturnsUpdatedOrder() {
        OrderStatusRequestDto orderStatusRequestDto =
                new OrderStatusRequestDto(Order.Status.COMPLETED);
        String shippingAddress = "Golden St, 12, L.A., USA";
        OrderRequestDto requestDto = new OrderRequestDto(shippingAddress);
        Order order = getOrder(requestDto);
        order.setStatus(orderStatusRequestDto.status());
        Long orderId = 1L;
        OrderResponseDto orderResponseDto = getOrderResponseDto(order);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);
        OrderResponseDto actual = orderService.updateOrderStatus(orderId, orderStatusRequestDto);
        Assertions.assertEquals(orderResponseDto, actual);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
        verify(orderMapper, times(1)).toDto(order);
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(orderMapper);
    }

    @Test
    @DisplayName("Update order status by id, order does not exist, returns exception")
    void updateOrderStatus_OrderNotExist_ReturnsException() {
        OrderStatusRequestDto orderStatusRequestDto =
                new OrderStatusRequestDto(Order.Status.COMPLETED);
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.updateOrderStatus(orderId, orderStatusRequestDto));
        String expected = "Can't find order with id: " + orderId;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(orderRepository, times(1)).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderMapper);
    }

    private ShoppingCart getShoppingCart() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(USER_ID);
        shoppingCart.setUser(new User(USER_ID));
        CartItem cartItemOne = getCartItem(shoppingCart);
        shoppingCart.setCartItems(Set.of(cartItemOne));
        shoppingCart.setDeleted(false);
        return shoppingCart;
    }

    private Order getOrder(OrderRequestDto requestDto) {
        Order order = new Order();
        order.setId(1L);
        order.setUser(new User(USER_ID));
        order.setStatus(Order.Status.PENDING);
        OrderItem orderItem = getOrderItem(order);
        BigDecimal total = orderItem.getBook().getPrice()
                .multiply(BigDecimal.valueOf(orderItem.getQuantity()));
        order.setTotal(total);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(requestDto.shippingAddress());
        order.setOrderItems(Set.of(orderItem));
        order.setDeleted(false);
        return order;
    }

    private OrderResponseDto getOrderResponseDto(Order order) {
        OrderResponseDto responseDto = new OrderResponseDto();
        responseDto.setId(order.getId());
        responseDto.setUserId(order.getUser().getId());
        OrderItemResponseDto orderItemResponseDto = new OrderItemResponseDto(1L, 1L, 1);
        responseDto.setOrderItems(Set.of(orderItemResponseDto));
        responseDto.setOrderDate(order.getOrderDate());
        responseDto.setTotal(order.getTotal());
        responseDto.setStatus(order.getStatus().toString());
        return responseDto;
    }

    private OrderItem getOrderItem(Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrder(order);
        orderItem.setBook(getBookOne());
        orderItem.setQuantity(1);
        orderItem.setPrice(getBookOne().getPrice());
        orderItem.setDeleted(false);
        return orderItem;
    }

    private CartItem getCartItem(ShoppingCart shoppingCart) {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setShoppingCart(shoppingCart);
        cartItem.setBook(getBookOne());
        cartItem.setQuantity(1);
        cartItem.setDeleted(false);
        return cartItem;
    }

    private Book getBookOne() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Witcher. The Last Wish");
        book.setAuthor("Andrzej Sapkowski");
        book.setIsbn("9780316333528");
        book.setPrice(BigDecimal.valueOf(19));
        book.setDescription("Magic adventures");
        book.setCoverImage("https://m.media-amazon.com/images/I/81MTXlALp+L._SL1500_.jpg");
        book.setCategories(Set.of(new Category(1L)));
        book.setDeleted(false);
        return book;
    }
}
