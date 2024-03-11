package springboot.onlinebookstore.controller.user;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import springboot.onlinebookstore.dto.user.request.UserLoginRequestDto;
import springboot.onlinebookstore.dto.user.request.UserRegistrationRequestDto;
import springboot.onlinebookstore.dto.user.response.UserResponseDto;
import springboot.onlinebookstore.queries.SqlScriptPath;

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
        executeScript(dataSource, SqlScriptPath.REMOVE_USER_DATA_SCRIPT);
        executeScript(dataSource, SqlScriptPath.REMOVE_BOOK_DATA_SCRIPT);
    }

    @SneakyThrows
    static void fillDataBase(DataSource dataSource) {
        executeScript(dataSource, SqlScriptPath.ADD_ROLE_SCRIPT);
        executeScript(dataSource, SqlScriptPath.ADD_USER_SCRIPT);
        executeScript(dataSource, SqlScriptPath.ADD_USERS_ROLE_SCRIPT);
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
        mockMvc.perform(post(URL_TEMPLATE + "/registration")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is(expected.email())))
                .andExpect(jsonPath("$.firstName", is(expected.firstName())))
                .andExpect(jsonPath("$.lastName", is(expected.lastName())))
                .andExpect(jsonPath("$.shippingAddress", is(expected.shippingAddress())))
                .andReturn();
    }

    @Test
    @DisplayName("Login by valid credentials, returns valid login response")
    void login_ValidCredentials_ReturnsValidLoginResponse() throws Exception {
        UserLoginRequestDto requestDto =
                new UserLoginRequestDto("bobSmith@example.com", "12345678");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        mockMvc.perform(post(URL_TEMPLATE + "/login")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
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
