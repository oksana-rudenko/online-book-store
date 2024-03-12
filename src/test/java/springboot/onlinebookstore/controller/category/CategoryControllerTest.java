package springboot.onlinebookstore.controller.category;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import springboot.onlinebookstore.dto.book.response.BookDtoWithoutCategoryIds;
import springboot.onlinebookstore.dto.category.request.CategoryRequestDto;
import springboot.onlinebookstore.dto.category.response.CategoryResponseDto;
import springboot.onlinebookstore.model.Category;
import springboot.onlinebookstore.queries.SqlScriptPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerTest {
    private static final String URL_TEMPLATE = "/categories";
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

    @WithMockUser(username = "bobSmith@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Create a new category and saving it to database")
    void createCategory_ValidRequestDto_ReturnsValidResponse() throws Exception {
        Category category = getCategoryOne();
        CategoryRequestDto requestDto = getCategoryRequestDto(category);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        CategoryResponseDto expected = getCategoryResponseDto(category);
        mockMvc.perform(post(URL_TEMPLATE)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name", is(expected.name())))
                .andExpect(jsonPath("$.description", is(expected.description())))
                .andReturn();
    }

    @WithMockUser(username = "bobSmith@example.com")
    @Test
    @DisplayName("Get all categories from database, returns list of four categories")
    void getAll_FourValidCategories_ReturnsValidList() throws Exception {
        List<CategoryResponseDto> expected = new ArrayList<>();
        CategoryResponseDto dtoOne = getCategoryResponseDto(getCategoryOne());
        expected.add(dtoOne);
        CategoryResponseDto dtoTwo = getCategoryResponseDto(getCategoryTwo());
        expected.add(dtoTwo);
        CategoryResponseDto dtoThree = getCategoryResponseDto(getCategoryThree());
        expected.add(dtoThree);
        CategoryResponseDto dtoFour = getCategoryResponseDto(getCategoryFour());
        expected.add(dtoFour);
        mockMvc.perform(get(URL_TEMPLATE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*").isArray())
                .andExpect(jsonPath("$.*", hasSize(4)))
                .andExpect(jsonPath("$[0].name", is(dtoOne.name())))
                .andExpect(jsonPath("$[1].name", is(dtoTwo.name())))
                .andExpect(jsonPath("$[2].name", is(dtoThree.name())))
                .andExpect(jsonPath("$[3].name", is(dtoFour.name())))
                .andReturn();
    }

    @WithMockUser(username = "bobSmith@example.com")
    @Test
    @DisplayName("Get category from database by id, returns valid category response dto")
    void getCategoryById_ValidId_ReturnsValidCategoryDto() throws Exception {
        CategoryResponseDto expected = getCategoryResponseDto(getCategoryOne());
        long id = 1L;
        MvcResult result = mockMvc.perform(get("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(expected.name())))
                .andExpect(jsonPath("$.description", is(expected.description())))
                .andReturn();
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Updating existing category by valid id")
    void updateCategory_ValidId_ReturnsValidCategoryDto() throws Exception {
        CategoryRequestDto requestDto =
                new CategoryRequestDto("Europe History", "Europe History");
        long id = 1L;
        CategoryResponseDto expected = new CategoryResponseDto(id,
                "Europe History", "Europe History");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(put("/categories/{id}", id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(expected.name())))
                .andExpect(jsonPath("$.description", is(expected.description())))
                .andReturn();
    }

    @WithMockUser(username = "bobSmith@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete category by its id, valid status no content")
    void deleteCategory_ValidId_StatusNoContent() throws Exception {
        long id = 1L;
        mockMvc.perform(delete("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @WithMockUser(username = "bobSmith@example.com")
    @Test
    @DisplayName("Get books list by valid category id, returns valid list")
    void getBooksByCategoryId_ValidCategoryId_ReturnsBookList() throws Exception {
        List<BookDtoWithoutCategoryIds> expected = new ArrayList<>();
        expected.add(getBookOne());
        expected.add(getBookTwo());
        expected.add(getBookThree());
        long id = 1L;
        mockMvc.perform(get("/categories/{id}/books", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*").isArray())
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(jsonPath("$[0].title", is(getBookOne().getTitle())))
                .andExpect(jsonPath("$[1].title", is(getBookTwo().getTitle())))
                .andExpect(jsonPath("$[2].title", is(getBookThree().getTitle())))
                .andReturn();
    }

    private Category getCategoryOne() {
        Category category = new Category();
        category.setId(1L);
        category.setName("History");
        category.setDescription("Europe History");
        return category;
    }

    private Category getCategoryTwo() {
        Category category = new Category();
        category.setId(2L);
        category.setName("Action & Adventure Fiction");
        category.setDescription("Mystery, Fantasy, Science Fiction");
        return category;
    }

    private Category getCategoryThree() {
        Category category = new Category();
        category.setId(3L);
        category.setName("Magic & Fantasy");
        category.setDescription("Magic & Fantasy Graphic Novels");
        return category;
    }

    private Category getCategoryFour() {
        Category category = new Category();
        category.setId(4L);
        category.setName("Humor & Entertainment");
        category.setDescription("Humor, Movies, Television, Pop Culture");
        return category;
    }

    private CategoryRequestDto getCategoryRequestDto(Category category) {
        return new CategoryRequestDto(category.getName(), category.getDescription());
    }

    private CategoryResponseDto getCategoryResponseDto(Category category) {
        return new CategoryResponseDto(category.getId(),
                category.getName(), category.getDescription());
    }

    private BookDtoWithoutCategoryIds getBookOne() {
        BookDtoWithoutCategoryIds responseDto = new BookDtoWithoutCategoryIds();
        responseDto.setId(1L);
        responseDto.setTitle("Bloodlands");
        responseDto.setAuthor("Timothy Snyder");
        responseDto.setIsbn("978-1541600065");
        responseDto.setPrice(BigDecimal.valueOf(26));
        responseDto.setDescription("Europe between Hitler and Stalin");
        responseDto.setCoverImage("https://m.media-amazon.com/images/I/818gorntorL._SL1500_.jpg");
        return responseDto;
    }

    private BookDtoWithoutCategoryIds getBookTwo() {
        BookDtoWithoutCategoryIds responseDto = new BookDtoWithoutCategoryIds();
        responseDto.setId(2L);
        responseDto.setTitle("The Red Prince");
        responseDto.setAuthor("Timothy Snyder");
        responseDto.setIsbn("978-1845951207");
        responseDto.setPrice(BigDecimal.valueOf(31));
        responseDto.setDescription("Life of Wilhelm von Habsburg, a Habsburg archduke");
        responseDto.setCoverImage("https://m.media-amazon.com/images/I/716rUFPputL._SL1360_.jpg");
        return responseDto;
    }

    private BookDtoWithoutCategoryIds getBookThree() {
        BookDtoWithoutCategoryIds responseDto = new BookDtoWithoutCategoryIds();
        responseDto.setId(3L);
        responseDto.setTitle("The Gates of Europe");
        responseDto.setAuthor("Serhii Plohy");
        responseDto.setIsbn("978-0465094868");
        responseDto.setPrice(BigDecimal.valueOf(28));
        responseDto.setDescription("A History of Ukraine");
        responseDto.setCoverImage("https://m.media-amazon.com/images/I/812JAY5J35L._SL1500_.jpg");
        return responseDto;
    }
}
