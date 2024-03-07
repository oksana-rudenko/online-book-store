package springboot.onlinebookstore.repository.book;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import springboot.onlinebookstore.model.Book;
import springboot.onlinebookstore.model.Category;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {
    private static final Long INVALID_ID = -1L;
    @Autowired
    private BookRepository bookRepository;

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/books/remove-book-and-category-data-from-tables.sql"
                    )
            );
        }
    }

    @Test
    @DisplayName("""
            Find all books by category id with pagination, returns three books list
            """)
    @Sql(scripts = {
            "classpath:database/books/remove-book-and-category-data-from-tables.sql",
            "classpath:database/books/add-books-to-book-table.sql",
            "classpath:database/books/add-categories-to-category-table.sql",
            "classpath:database/books/add-category-to-book-in-book-category-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void findBooksByCategoryId_ValidCategoryId_ReturnsThreeBooksList() {
        Book book1 = getBookOne();
        Book book2 = getBookTwo();
        Book book3 = getBookThree();
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> expected = new ArrayList<>();
        expected.add(book1);
        expected.add(book2);
        expected.add(book3);
        List<Book> actual = bookRepository
                .findBooksByCategoryId(getHistoryCategory().getId(), pageable);
        Assertions.assertEquals(3, actual.size());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            Find all books by category id with pagination, returns one book list
            """)
    @Sql(scripts = {
            "classpath:database/books/remove-book-and-category-data-from-tables.sql",
            "classpath:database/books/add-books-to-book-table.sql",
            "classpath:database/books/add-categories-to-category-table.sql",
            "classpath:database/books/add-category-to-book-in-book-category-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void findBooksByCategoryId_ValidCategoryId_ReturnsOneBookList() {
        Book book = getBookFour();
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> expected = new ArrayList<>();
        expected.add(book);
        List<Book> actual = bookRepository
                .findBooksByCategoryId(getFictionCategory().getId(), pageable);
        Assertions.assertEquals(1, actual.size());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            Find all books by not existing category id, returns empty list
            """)
    @Sql(scripts = "classpath:database/books/remove-book-and-category-data-from-tables.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void findBooksByCategoryId_InvalidCategoryId_ReturnsEmptyList() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> actual = bookRepository.findBooksByCategoryId(INVALID_ID, pageable);
        Assertions.assertEquals(0, actual.size());
    }

    private Book getBookOne() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Bloodlands");
        book.setAuthor("Timothy Snyder");
        book.setIsbn("978-1541600065");
        book.setPrice(BigDecimal.valueOf(26));
        book.setDescription("Europe between Hitler and Stalin");
        book.setCoverImage("https://m.media-amazon.com/images/I/818gorntorL._SL1500_.jpg");
        book.setDeleted(false);
        book.setCategories(Set.of(getHistoryCategory()));
        return book;
    }

    private Book getBookTwo() {
        Book book = new Book();
        book.setId(2L);
        book.setTitle("The Red Prince");
        book.setAuthor("Timothy Snyder");
        book.setIsbn("978-1845951207");
        book.setPrice(BigDecimal.valueOf(31));
        book.setDescription("Life of Wilhelm von Habsburg, a Habsburg archduke");
        book.setCoverImage("https://m.media-amazon.com/images/I/716rUFPputL._SL1360_.jpg");
        book.setDeleted(false);
        book.setCategories(Set.of(getHistoryCategory()));
        return book;
    }

    private Book getBookThree() {
        Book book = new Book();
        book.setId(3L);
        book.setTitle("The Gates of Europe");
        book.setAuthor("Serhii Plohy");
        book.setIsbn("978-0465094868");
        book.setPrice(BigDecimal.valueOf(28));
        book.setDescription("A History of Ukraine");
        book.setCoverImage("https://m.media-amazon.com/images/I/812JAY5J35L._SL1500_.jpg");
        book.setDeleted(false);
        book.setCategories(Set.of(getHistoryCategory()));
        return book;
    }

    private Book getBookFour() {
        Book book = new Book();
        book.setId(4L);
        book.setTitle("Witcher. The Last Wish");
        book.setAuthor("Andrzej Sapkowski");
        book.setIsbn("978-0316333528");
        book.setPrice(BigDecimal.valueOf(19));
        book.setDescription("Magic adventure");
        book.setCoverImage("https://m.media-amazon.com/images/I/81MTXlALp+L._SL1500_.jpg");
        book.setDeleted(false);
        book.setCategories(Set.of(getFictionCategory(), getFantasyCategory()));
        return book;
    }

    private Category getHistoryCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("History");
        category.setDescription("Europe History");
        return category;
    }

    private Category getFictionCategory() {
        Category category = new Category();
        category.setId(2L);
        category.setName("Action & Adventure Fiction");
        category.setDescription("Mystery, Fantasy, Science Fiction");
        return category;
    }

    private Category getFantasyCategory() {
        Category category = new Category();
        category.setId(2L);
        category.setName("Magic & Fantasy");
        category.setDescription("Magic & Fantasy Graphic Novels");
        return category;
    }
}
