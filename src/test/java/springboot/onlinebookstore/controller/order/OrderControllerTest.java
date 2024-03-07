package springboot.onlinebookstore.controller.order;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import springboot.onlinebookstore.dto.order.request.OrderRequestDto;
import springboot.onlinebookstore.dto.order.request.OrderStatusRequestDto;
import springboot.onlinebookstore.dto.order.response.OrderResponseDto;
import springboot.onlinebookstore.dto.orderitem.OrderItemResponseDto;
import springboot.onlinebookstore.model.Order;
import springboot.onlinebookstore.model.Role;
import springboot.onlinebookstore.model.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTest {
    private static final String URL_TEMPLATE = "/orders";
    private static final Long USER_ID = 1L;
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    void setUp(@Autowired DataSource dataSource) {
        clearDataBase(dataSource);
        fillDataBase(dataSource);
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        clearDataBase(dataSource);
    }

    @SneakyThrows
    static void clearDataBase(DataSource dataSource) {
        String path1 = "database/users/remove-role-user-and-shopping-cart-from-table.sql";
        String path2 = "database/books/remove-book-and-category-data-from-tables.sql";
        executeScript(dataSource, path1);
        executeScript(dataSource, path2);
    }

    @SneakyThrows
    static void fillDataBase(DataSource dataSource) {
        String path1 = "database/books/add-books-to-book-table.sql";
        executeScript(dataSource, path1);
        String path2 = "database/books/add-categories-to-category-table.sql";
        executeScript(dataSource, path2);
        String path3 = "database/books/add-category-to-book-in-book-category-table.sql";
        executeScript(dataSource, path3);
        String path4 = "database/users/add-roles-to-role-table.sql";
        executeScript(dataSource, path4);
        String path5 = "database/users/add-users-to-user-table.sql";
        executeScript(dataSource, path5);
        String path6 = "database/users/add-users-roles-to-user-role-table.sql";
        executeScript(dataSource, path6);
        String path7 = "database/users/add-shopping-cart-to-shopping-cart-table.sql";
        executeScript(dataSource, path7);
        String path8 = "database/users/add-cart-item-to-cart-item-table.sql";
        executeScript(dataSource, path8);
        String path9 = "database/users/add-order-to-order-table.sql";
        executeScript(dataSource, path9);
        String path10 = "database/users/add-order-item-to-order-items-table.sql";
        executeScript(dataSource, path10);
    }

    @SneakyThrows
    static void executeScript(DataSource dataSource, String path) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(path)
            );
        }
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"USER"})
    @Test
    @DisplayName("Place order for user, returns valid order response")
    void placeOrder_ValidData_ReturnsOrderResponse() throws Exception {
        User mockUser = getUser();
        OrderRequestDto requestDto = new OrderRequestDto("Golden St, 12, L.A., USA");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(post(URL_TEMPLATE)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andReturn();
        OrderResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), OrderResponseDto.class
        );
        OrderResponseDto expected = getOrderResponseDto();
        expected.setId(actual.getId());
        expected.setOrderItems(Set.of(new OrderItemResponseDto(2L, 1L, 1)));
        expected.setOrderDate(actual.getOrderDate());
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());
        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"USER"})
    @Test
    @DisplayName("Get orders by user, returns list of one order response")
    void getOrders_ExistedOrder_ReturnsOrdersList() throws Exception {
        User mockUser = getUser();
        MvcResult result = mockMvc.perform(get(URL_TEMPLATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andReturn();
        OrderResponseDto[] orders = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), OrderResponseDto[].class
        );
        List<OrderResponseDto> actual = Arrays.stream(orders).toList();
        OrderResponseDto orderResponseDto = getOrderResponseDto();
        orderResponseDto.setOrderDate(actual.stream().findFirst().get().getOrderDate());
        List<OrderResponseDto> expected = List.of(orderResponseDto);
        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"USER"})
    @Test
    @DisplayName("Get order items by order id, returns list of one order item")
    void getOrderItems_ValidUsersOrder_ReturnsListOfOrderItem() throws Exception {
        User mockUser = getUser();
        Long orderId = 1L;
        MvcResult result = mockMvc.perform(get("/orders/{orderId}/items", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andReturn();
        OrderItemResponseDto[] orderItems = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), OrderItemResponseDto[].class
        );
        List<OrderItemResponseDto> actual = Arrays.stream(orderItems).toList();
        OrderItemResponseDto itemResponseDto =
                getOrderResponseDto().getOrderItems().stream().findFirst().get();
        List<OrderItemResponseDto> expected = List.of(itemResponseDto);
        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"USER"})
    @Test
    @DisplayName("Get items by id from user's order, returns valid order item")
    void getItemFromOrder_ValidData_ReturnsOrderItem() throws Exception {
        User mockUser = getUser();
        Long orderId = 1L;
        Long itemId = 1L;
        MvcResult result = mockMvc.perform(get("/orders/{orderId}/items/{id}", orderId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andReturn();
        OrderItemResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), OrderItemResponseDto.class
        );
        OrderItemResponseDto expected = new OrderItemResponseDto(1L, 1L, 1);
        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Update order status, returns valid order response")
    void updateOrderStatus() throws Exception {
        OrderStatusRequestDto requestDto = new OrderStatusRequestDto(Order.Status.COMPLETED);
        Long id = 1L;
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(patch("/orders/{id}", id)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        OrderResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), OrderResponseDto.class
        );
        OrderResponseDto expected = getOrderResponseDto();
        expected.setStatus(Order.Status.COMPLETED.toString());
        expected.setOrderDate(actual.getOrderDate());
        Assertions.assertEquals(expected, actual);
    }

    private OrderResponseDto getOrderResponseDto() {
        OrderResponseDto responseDto = new OrderResponseDto();
        responseDto.setId(1L);
        responseDto.setUserId(USER_ID);
        responseDto.setOrderItems(Set.of(new OrderItemResponseDto(1L, 1L, 1)));
        responseDto.setOrderDate(LocalDateTime.now());
        responseDto.setTotal(BigDecimal.valueOf(26));
        responseDto.setStatus(Order.Status.PENDING.toString());
        return responseDto;
    }

    private User getUser() {
        User user = new User();
        user.setId(USER_ID);
        Role userRole = new Role();
        userRole.setName(Role.RoleName.USER);
        userRole.setId(1L);
        user.setRoles(Set.of(userRole));
        return user;
    }
}
