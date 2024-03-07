package springboot.onlinebookstore.controller.shoppingcart;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
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
import springboot.onlinebookstore.dto.cartitem.request.CartItemQuantityRequestDto;
import springboot.onlinebookstore.dto.cartitem.request.CartItemRequestDto;
import springboot.onlinebookstore.dto.cartitem.response.CartItemResponseDto;
import springboot.onlinebookstore.dto.shoppingcart.ShoppingCartResponseDto;
import springboot.onlinebookstore.model.Role;
import springboot.onlinebookstore.model.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerTest {
    private static final String URL_TEMPLATE = "/cart";
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
    @DisplayName("Get user's shopping cart, valid data, returns valid shopping cart")
    void getByUser_ValidUser_ReturnsShoppingCart() throws Exception {
        ShoppingCartResponseDto expected = getShoppingCartResponseDto();
        CartItemResponseDto item = new CartItemResponseDto(1L, 1L, "Bloodlands", 1);
        expected.setCartItems(Set.of(item));
        User mockUser = getUser();
        MvcResult result = mockMvc.perform(get(URL_TEMPLATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andReturn();
        ShoppingCartResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), ShoppingCartResponseDto.class
        );
        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"USER"})
    @Test
    @DisplayName("Add new item to user's shopping cart, returns updated shopping cart")
    void addBookToShoppingCart_ValidData_ReturnsUpdatedCart() throws Exception {
        User mockUser = getUser();
        CartItemRequestDto requestDto = new CartItemRequestDto(2L, 1);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(post(URL_TEMPLATE)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andReturn();
        ShoppingCartResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), ShoppingCartResponseDto.class
        );
        ShoppingCartResponseDto expected = getShoppingCartResponseDto();
        CartItemResponseDto item1 = new CartItemResponseDto(1L, 1L, "Bloodlands", 1);
        CartItemResponseDto item2 = new CartItemResponseDto(2L, 2L, "The Red Prince", 1);
        expected.setCartItems(Set.of(item1, item2));
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());
        Assertions.assertEquals(expected.getCartItems(), actual.getCartItems());
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"USER"})
    @Test
    @DisplayName("Update cart item, valid data, returns cart with updated item")
    void updateCartItem_ValidData_ReturnsUpdatedCart() throws Exception {
        Long itemId = 1L;
        User mockUser = getUser();
        CartItemQuantityRequestDto requestDto = new CartItemQuantityRequestDto(5);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(put("/cart/cart-items/{cartItemId}", itemId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andReturn();
        ShoppingCartResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), ShoppingCartResponseDto.class
        );
        ShoppingCartResponseDto expected = getShoppingCartResponseDto();
        expected.setCartItems(Set.of(new CartItemResponseDto(1L, 1L, "Bloodlands", 5)));
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());
        Assertions.assertEquals(expected, actual);
        Assertions.assertEquals(expected.getCartItems(), actual.getCartItems());
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"USER"})
    @Test
    @DisplayName("Remove book from shopping cart, returns updated cart")
    void removeBookFromShoppingCart_ValidData_ReturnsUpdatedCart() throws Exception {
        Long itemId = 1L;
        User mockUser = getUser();
        MvcResult result = mockMvc.perform(delete("/cart/cart-items/{cartItemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andReturn();
        ShoppingCartResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), ShoppingCartResponseDto.class
        );
        ShoppingCartResponseDto expected = getShoppingCartResponseDto();
        expected.setCartItems(Set.of());
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());
        Assertions.assertEquals(expected, actual);
        Assertions.assertEquals(expected.getCartItems(), actual.getCartItems());
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

    private ShoppingCartResponseDto getShoppingCartResponseDto() {
        ShoppingCartResponseDto shoppingCartResponseDto = new ShoppingCartResponseDto();
        shoppingCartResponseDto.setId(USER_ID);
        shoppingCartResponseDto.setUserId(USER_ID);
        Set<CartItemResponseDto> itemResponseDtos = new HashSet<>();
        shoppingCartResponseDto.setCartItems(itemResponseDtos);
        return shoppingCartResponseDto;
    }
}
