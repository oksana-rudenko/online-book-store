package springboot.onlinebookstore.repository.shoppingcart;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import springboot.onlinebookstore.model.Role;
import springboot.onlinebookstore.model.ShoppingCart;
import springboot.onlinebookstore.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShoppingCartRepositoryTest {
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/users/remove-role-user-and-shopping-cart-from-table.sql"
                    )
            );
        }
    }

    @Test
    @DisplayName("""
            Find shopping cart by valid user, returns shopping cart
            """)
    @Sql(scripts = {
            "classpath:database/users/remove-role-user-and-shopping-cart-from-table.sql",
            "classpath:database/users/add-roles-to-role-table.sql",
            "classpath:database/users/add-users-to-user-table.sql",
            "classpath:database/users/add-users-roles-to-user-role-table.sql",
            "classpath:database/users/add-shopping-cart-to-shopping-cart-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void findByUserId_ValidUser_ReturnsUsersShoppingCart() {
        User user = getUserOne();
        ShoppingCart expected = new ShoppingCart();
        expected.setId(1L);
        expected.setUser(user);
        expected.setCartItems(Set.of());
        expected.setDeleted(false);
        ShoppingCart actual = shoppingCartRepository.findByUserId(1L).get();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            Find shopping cart by valid user, returns shopping cart
            """)
    @Sql(scripts = {
            "classpath:database/users/remove-role-user-and-shopping-cart-from-table.sql",
            "classpath:database/users/add-roles-to-role-table.sql",
            "classpath:database/users/add-users-to-user-table.sql",
            "classpath:database/users/add-users-roles-to-user-role-table.sql",
            "classpath:database/users/add-shopping-cart-to-shopping-cart-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void findByUserId_InvalidUser_ReturnsEmptyOptional() {
        Long invalidUserId = -1L;
        boolean actual = shoppingCartRepository.findByUserId(invalidUserId).isPresent();
        Assertions.assertFalse(actual);
    }

    private User getUserOne() {
        User user = new User();
        Role userRole = getUserRole();
        Role adminRole = getAdminRole();
        user.setId(1L);
        user.setEmail("bobSmith@example.com");
        user.setPassword("$2a$10$j5vOyFFskzmJ8nuXW1IybOCgVZY7fQg0TohkSg1ius.riOiCI.PRK");
        user.setFirstName("Robert");
        user.setLastName("Smith");
        user.setShippingAddress("Golden St, 12, L.A., USA");
        user.setDeleted(false);
        user.setRoles(Set.of(userRole, adminRole));
        return user;
    }

    private Role getUserRole() {
        Role role = new Role();
        role.setId(1L);
        role.setName(Role.RoleName.USER);
        return role;
    }

    private Role getAdminRole() {
        Role role = new Role();
        role.setId(1L);
        role.setName(Role.RoleName.ADMIN);
        return role;
    }
}
