package springboot.onlinebookstore.controller.book;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
import springboot.onlinebookstore.dto.book.request.CreateBookRequestDto;
import springboot.onlinebookstore.dto.book.response.BookResponseDto;
import springboot.onlinebookstore.repository.book.BookSpecificationBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerTest {
    private static final String URL_TEMPLATE = "/books";
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BookSpecificationBuilder bookSpecificationBuilder;

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
        String path2 = "database/books/add-categories-to-category-table.sql";
        String path3 = "database/books/add-category-to-book-in-book-category-table.sql";
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

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Create a new book and saving it to database")
    void createBook_ValidRequestDto_ReturnsValidResponse() throws Exception {
        CreateBookRequestDto requestDto = getRequestDtoFive();
        BookResponseDto expected = getResponseDtoFive();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(post(URL_TEMPLATE)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        BookResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), BookResponseDto.class
        );
        expected.setId(actual.getId());
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());
        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Get all books from database, returns list of four books")
    void getAll_FourValidBooks_ReturnsValidList() throws Exception {
        BookResponseDto responseDtoOne = getResponseDtoOne();
        BookResponseDto responseDtoTwo = getResponseDtoTwo();
        BookResponseDto responseDtoThree = getResponseDtoThree();
        BookResponseDto responseDtoFour = getResponseDtoFour();
        List<BookResponseDto> expected = new ArrayList<>();
        expected.add(responseDtoOne);
        expected.add(responseDtoTwo);
        expected.add(responseDtoThree);
        expected.add(responseDtoFour);
        MvcResult result = mockMvc.perform(get(URL_TEMPLATE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookResponseDto[] bookList = objectMapper
                .readValue(result.getResponse().getContentAsByteArray(), BookResponseDto[].class);
        List<BookResponseDto> actual = Arrays.stream(bookList).toList();
        Assertions.assertEquals(4, actual.size());
        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Get book from database by id, returns valid book")
    void getBookById_ValidBook_ReturnsValidBook() throws Exception {
        BookResponseDto expected = getResponseDtoOne();
        long id = 1L;
        MvcResult result = mockMvc.perform(get("/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), BookResponseDto.class
        );
        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update book by its id, returns valid book")
    void update_ValidBook_ReturnsUpdatedBook() throws Exception {
        CreateBookRequestDto requestDto = getRequestDtoOne();
        requestDto.setDescription("History");
        requestDto.setPrice(BigDecimal.valueOf(22L));
        BookResponseDto expected = getResponseDtoOne();
        expected.setDescription("History");
        expected.setPrice(BigDecimal.valueOf(22L));
        long id = 1L;
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(put("/books/{id}", id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), BookResponseDto.class
        );
        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete book by its id, status no content")
    void deleteById_ValidBook_StatusNoContent() throws Exception {
        long id = 1L;
        mockMvc.perform(delete("/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Search books by parameters, returns valid books list")
    void searchBooks_ValidParameters_ReturnsValidList() throws Exception {
        BookResponseDto book1 = getResponseDtoOne();
        List<BookResponseDto> expected = new ArrayList<>();
        expected.add(book1);
        String searchParams = "?author=Timothy Snyder&price=10,30";
        MvcResult result = mockMvc.perform(get("/books/search" + searchParams)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookResponseDto[] bookList = objectMapper
                .readValue(result.getResponse().getContentAsByteArray(), BookResponseDto[].class);
        List<BookResponseDto> actual = Arrays.stream(bookList).toList();
        Assertions.assertEquals(1, actual.size());
        Assertions.assertEquals(expected, actual);
    }

    private CreateBookRequestDto getRequestDtoOne() {
        CreateBookRequestDto requestDto = new CreateBookRequestDto();
        requestDto.setTitle("Bloodlands");
        requestDto.setAuthor("Timothy Snyder");
        requestDto.setIsbn("978-1541600065");
        requestDto.setPrice(BigDecimal.valueOf(26));
        requestDto.setDescription("Europe between Hitler and Stalin");
        requestDto.setCoverImage("https://m.media-amazon.com/images/I/818gorntorL._SL1500_.jpg");
        requestDto.setCategoryIds(Set.of(1L));
        return requestDto;
    }

    private BookResponseDto getResponseDtoOne() {
        BookResponseDto responseDto = new BookResponseDto();
        responseDto.setId(1L);
        responseDto.setTitle("Bloodlands");
        responseDto.setAuthor("Timothy Snyder");
        responseDto.setIsbn("978-1541600065");
        responseDto.setPrice(BigDecimal.valueOf(26));
        responseDto.setDescription("Europe between Hitler and Stalin");
        responseDto.setCoverImage("https://m.media-amazon.com/images/I/818gorntorL._SL1500_.jpg");
        responseDto.setCategoryIds(Set.of(1L));
        return responseDto;
    }

    private BookResponseDto getResponseDtoTwo() {
        BookResponseDto responseDto = new BookResponseDto();
        responseDto.setId(2L);
        responseDto.setTitle("The Red Prince");
        responseDto.setAuthor("Timothy Snyder");
        responseDto.setIsbn("978-1845951207");
        responseDto.setPrice(BigDecimal.valueOf(31));
        responseDto.setDescription("Life of Wilhelm von Habsburg, a Habsburg archduke");
        responseDto.setCoverImage("https://m.media-amazon.com/images/I/716rUFPputL._SL1360_.jpg");
        responseDto.setCategoryIds(Set.of(1L));
        return responseDto;
    }

    private BookResponseDto getResponseDtoThree() {
        BookResponseDto responseDto = new BookResponseDto();
        responseDto.setId(3L);
        responseDto.setTitle("The Gates of Europe");
        responseDto.setAuthor("Serhii Plohy");
        responseDto.setIsbn("978-0465094868");
        responseDto.setPrice(BigDecimal.valueOf(28));
        responseDto.setDescription("A History of Ukraine");
        responseDto.setCoverImage("https://m.media-amazon.com/images/I/812JAY5J35L._SL1500_.jpg");
        responseDto.setCategoryIds(Set.of(1L));
        return responseDto;
    }

    private BookResponseDto getResponseDtoFour() {
        BookResponseDto responseDto = new BookResponseDto();
        responseDto.setId(4L);
        responseDto.setTitle("Witcher. The Last Wish");
        responseDto.setAuthor("Andrzej Sapkowski");
        responseDto.setIsbn("978-0316333528");
        responseDto.setPrice(BigDecimal.valueOf(19));
        responseDto.setDescription("Magic adventure");
        responseDto.setCoverImage("https://m.media-amazon.com/images/I/81MTXlALp+L._SL1500_.jpg");
        responseDto.setCategoryIds(Set.of(2L, 3L));
        return responseDto;
    }

    private CreateBookRequestDto getRequestDtoFive() {
        CreateBookRequestDto requestDto = new CreateBookRequestDto();
        requestDto.setTitle("Friends, Lovers and the Big Terrible Thing");
        requestDto.setAuthor("Matthew Perry");
        requestDto.setIsbn("978-1472295934");
        requestDto.setPrice(BigDecimal.valueOf(12));
        requestDto.setDescription("Funny, fascinating and compelling");
        requestDto.setCoverImage("https://m.media-amazon.com/images/I/61lorBa6JwL._SL1000_.jpg");
        requestDto.setCategoryIds(Set.of(4L));
        return requestDto;
    }

    private BookResponseDto getResponseDtoFive() {
        BookResponseDto responseDto = new BookResponseDto();
        responseDto.setId(5L);
        responseDto.setTitle("Friends, Lovers and the Big Terrible Thing");
        responseDto.setAuthor("Matthew Perry");
        responseDto.setIsbn("978-1472295934");
        responseDto.setPrice(BigDecimal.valueOf(12));
        responseDto.setDescription("Funny, fascinating and compelling");
        responseDto.setCoverImage("https://m.media-amazon.com/images/I/61lorBa6JwL._SL1000_.jpg");
        responseDto.setCategoryIds(Set.of(4L));
        return responseDto;
    }
}
