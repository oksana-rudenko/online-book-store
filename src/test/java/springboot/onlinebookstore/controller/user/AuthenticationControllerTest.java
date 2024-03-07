package springboot.onlinebookstore.controller.user;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import springboot.onlinebookstore.dto.user.request.UserLoginRequestDto;
import springboot.onlinebookstore.dto.user.request.UserRegistrationRequestDto;
import springboot.onlinebookstore.dto.user.response.UserLoginResponseDto;
import springboot.onlinebookstore.dto.user.response.UserResponseDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {
    private static final String URL_TEMPLATE = "/auth";
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
        String path1 = "database/users/add-roles-to-role-table.sql";
        String path2 = "database/users/add-users-to-user-table.sql";
        String path3 = "database/users/add-users-roles-to-user-role-table.sql";
        executeScript(dataSource, path1);
        executeScript(dataSource, path2);
        executeScript(dataSource, path3);
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

    @Test
    @DisplayName("Register new user and saving it to database")
    void register_ValidRequest_ReturnsValidResponse() throws Exception {
        UserRegistrationRequestDto requestDto = getUserRegistrationRequestDto();
        UserResponseDto expected = getUserResponseDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(post(URL_TEMPLATE + "/registration")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), UserResponseDto.class
        );
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.id());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Login by valid credentials, returns valid login response")
    void login_ValidCredentials_ReturnsValidLoginResponse() throws Exception {
        UserLoginRequestDto requestDto =
                new UserLoginRequestDto("bobSmith@example.com", "12345678");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(post(URL_TEMPLATE + "/login")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserLoginResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), UserLoginResponseDto.class
        );
        Assertions.assertNotNull(actual);
    }

    private UserRegistrationRequestDto getUserRegistrationRequestDto() {
        UserRegistrationRequestDto userRegistration = new UserRegistrationRequestDto();
        userRegistration.setEmail("katePerry@example.com");
        userRegistration.setPassword("12345678");
        userRegistration.setRepeatPassword("12345678");
        userRegistration.setFirstName("Katerina");
        userRegistration.setLastName("Perry");
        userRegistration.setShippingAddress("8 Avenue, 18, New-York, USA");
        return userRegistration;
    }

    private UserResponseDto getUserResponseDto() {
        return new UserResponseDto(
                4L, "katePerry@example.com", "Katerina",
                "Perry", "8 Avenue, 18, New-York, USA"
        );
    }
}
