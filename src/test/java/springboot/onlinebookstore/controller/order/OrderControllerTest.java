package springboot.onlinebookstore.controller.order;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
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
import springboot.onlinebookstore.queries.SqlScriptPath;

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
        executeScript(dataSource, SqlScriptPath.REMOVE_USER_DATA_SCRIPT);
        executeScript(dataSource, SqlScriptPath.REMOVE_BOOK_DATA_SCRIPT);
    }

    @SneakyThrows
    static void fillDataBase(DataSource dataSource) {
        executeScript(dataSource, SqlScriptPath.ADD_BOOK_SCRIPT);
        executeScript(dataSource, SqlScriptPath.ADD_CATEGORY_SCRIPT);
        executeScript(dataSource, SqlScriptPath.ADD_CATEGORY_TO_BOOK_SCRIPT);
        executeScript(dataSource, SqlScriptPath.ADD_ROLE_SCRIPT);
        executeScript(dataSource, SqlScriptPath.ADD_USER_SCRIPT);
        executeScript(dataSource, SqlScriptPath.ADD_USERS_ROLE_SCRIPT);
        executeScript(dataSource, SqlScriptPath.ADD_SHOPPING_CART_SCRIPT);
        executeScript(dataSource, SqlScriptPath.ADD_CART_ITEM_SCRIPT);
        executeScript(dataSource, SqlScriptPath.ADD_ORDER_SCRIPT);
        executeScript(dataSource, SqlScriptPath.ADD_ORDER_ITEM_SCRIPT);
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
        mockMvc.perform(post(URL_TEMPLATE)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.orderItems", hasSize(1)))
                .andExpect(jsonPath("$.orderDate").isNotEmpty())
                .andExpect(jsonPath("$.total").isNotEmpty())
                .andExpect(jsonPath("$.status").isNotEmpty())
                .andReturn();
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
                .andExpect(jsonPath("$.*").isArray())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").isNotEmpty())
                .andExpect(jsonPath("$[0].orderItems", hasSize(1)))
                .andExpect(jsonPath("$[0].orderDate").isNotEmpty())
                .andExpect(jsonPath("$[0].total").isNotEmpty())
                .andExpect(jsonPath("$[0].status").isNotEmpty())
                .andReturn();
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"USER"})
    @Test
    @DisplayName("Get order items by order id, returns list of one order item")
    void getOrderItems_ValidUsersOrder_ReturnsListOfOrderItem() throws Exception {
        User mockUser = getUser();
        Long orderId = 1L;
        OrderItemResponseDto itemResponseDto =
                getOrderResponseDto().getOrderItems().stream().findFirst().get();
        mockMvc.perform(get("/orders/{orderId}/items", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemResponseDto.id()))
                .andExpect(jsonPath("$[0].bookId").value(itemResponseDto.bookId()))
                .andExpect(jsonPath("$[0].quantity").value(itemResponseDto.quantity()))
                .andReturn();
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"USER"})
    @Test
    @DisplayName("Get items by id from user's order, returns valid order item")
    void getItemFromOrder_ValidData_ReturnsOrderItem() throws Exception {
        User mockUser = getUser();
        Long orderId = 1L;
        Long itemId = 1L;
        OrderItemResponseDto expected = new OrderItemResponseDto(1L, 1L, 1);
        mockMvc.perform(get("/orders/{orderId}/items/{id}", orderId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expected.id()))
                .andExpect(jsonPath("$.bookId").value(expected.bookId()))
                .andExpect(jsonPath("$.quantity").value(expected.quantity()))
                .andReturn();
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Update order status, returns valid order response")
    void updateOrderStatus_ValidData_ReturnsValidResponse() throws Exception {
        OrderStatusRequestDto requestDto = new OrderStatusRequestDto(Order.Status.COMPLETED);
        Long id = 1L;
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        OrderResponseDto expected = getOrderResponseDto();
        expected.setStatus(Order.Status.COMPLETED.toString());
        mockMvc.perform(patch("/orders/{id}", id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expected.getId()))
                .andExpect(jsonPath("$.userId").value(expected.getUserId()))
                .andExpect(jsonPath("$.orderItems").isArray())
                .andExpect(jsonPath("$.orderItems", hasSize(1)))
                .andExpect(jsonPath("$.orderDate").isNotEmpty())
                .andExpect(jsonPath("$.total").value(expected.getTotal()))
                .andExpect(jsonPath("$.status").value(expected.getStatus()))
                .andReturn();
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
