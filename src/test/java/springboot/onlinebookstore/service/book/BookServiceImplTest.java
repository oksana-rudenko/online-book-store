package springboot.onlinebookstore.service.book;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import springboot.onlinebookstore.dto.book.BookSearchParametersDto;
import springboot.onlinebookstore.dto.book.request.CreateBookRequestDto;
import springboot.onlinebookstore.dto.book.response.BookDtoWithoutCategoryIds;
import springboot.onlinebookstore.dto.book.response.BookResponseDto;
import springboot.onlinebookstore.exception.EntityNotFoundException;
import springboot.onlinebookstore.mapper.BookMapper;
import springboot.onlinebookstore.model.Book;
import springboot.onlinebookstore.model.Category;
import springboot.onlinebookstore.repository.book.BookRepository;
import springboot.onlinebookstore.repository.book.BookSpecificationBuilder;
import springboot.onlinebookstore.service.impl.BookServiceImpl;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    private static final CreateBookRequestDto REQUEST_DTO = new CreateBookRequestDto();
    private static final Book BOOK = new Book();
    private static final BookResponseDto RESPONSE_DTO = new BookResponseDto();
    private static final Long BOOK_ID = 1L;
    private static final Long INVALID_ID = -1L;
    private static final Long CATEGORY_ID = 1L;
    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 10;
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private BookServiceImpl bookService;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;

    @BeforeAll
    static void beforeAll() {
        REQUEST_DTO.setTitle("Witcher. The Last Wish")
                .setAuthor("Andrzej Sapkowski")
                .setIsbn("9780316333528")
                .setPrice(BigDecimal.valueOf(19))
                .setDescription("Magic adventures")
                .setCoverImage("https://m.media-amazon.com/images/I/81MTXlALp+L._SL1500_.jpg")
                .setCategoryIds(Set.of(CATEGORY_ID));

        BOOK.setId(BOOK_ID);
        BOOK.setTitle(REQUEST_DTO.getTitle());
        BOOK.setAuthor(REQUEST_DTO.getAuthor());
        BOOK.setIsbn(REQUEST_DTO.getIsbn());
        BOOK.setPrice(REQUEST_DTO.getPrice());
        BOOK.setDescription(REQUEST_DTO.getDescription());
        BOOK.setCoverImage(REQUEST_DTO.getCoverImage());
        BOOK.setCategories(REQUEST_DTO.getCategoryIds().stream()
                .map(Category::new)
                .collect(Collectors.toSet()));
        BOOK.setDeleted(false);

        RESPONSE_DTO.setId(BOOK.getId())
                .setTitle(REQUEST_DTO.getTitle())
                .setAuthor(REQUEST_DTO.getAuthor())
                .setIsbn(REQUEST_DTO.getIsbn())
                .setPrice(REQUEST_DTO.getPrice())
                .setDescription(REQUEST_DTO.getDescription())
                .setCoverImage(REQUEST_DTO.getCoverImage())
                .setCategoryIds(REQUEST_DTO.getCategoryIds());
    }

    @Test
    @DisplayName("Save valid book from valid request dto")
    void save_ValidCreteBookRequestDto_ReturnsValidBookResponseDto() {
        when(bookMapper.toEntity(REQUEST_DTO)).thenReturn(BOOK);
        when(bookRepository.save(BOOK)).thenReturn(BOOK);
        when(bookMapper.toDto(BOOK)).thenReturn(RESPONSE_DTO);
        BookResponseDto actual = bookService.save(REQUEST_DTO);
        Assertions.assertEquals(RESPONSE_DTO, actual);
    }

    @Test
    @DisplayName("Find all books from database, returns valid list of one book")
    void findAll_ValidBookList_ReturnsValidList() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        List<Book> books = Collections.singletonList(BOOK);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        when(bookMapper.toDto(BOOK)).thenReturn(RESPONSE_DTO);
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        List<BookResponseDto> expected = Collections.singletonList(RESPONSE_DTO);
        List<BookResponseDto> actual = bookService.findAll(pageable);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Find all books from database, returns empty list")
    void findAll_EmptyList_ReturnEmptyList() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        List<Book> books = Collections.emptyList();
        Page<Book> bookPage = new PageImpl<>(books, pageable, 0);
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        List<BookResponseDto> expected = Collections.emptyList();
        List<BookResponseDto> actual = bookService.findAll(pageable);
        Assertions.assertEquals(expected, actual);
        verify(bookRepository, times(1)).findAll(pageable);
        verifyNoMoreInteractions(bookMapper);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Find valid book by valid id, returns book dto")
    void findById_ValidBook_ReturnsValidBook() {
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(BOOK));
        when(bookMapper.toDto(BOOK)).thenReturn(RESPONSE_DTO);
        BookResponseDto actual = bookService.findById(BOOK_ID);
        Assertions.assertEquals(RESPONSE_DTO, actual);
    }

    @Test
    @DisplayName("Find book by invalid id, returns EntityNotFoundException")
    void findById_InvalidBookId_ReturnsException() {
        when(bookRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.findById(INVALID_ID)
        );
        String expected = "Book by id: " + INVALID_ID + " does not exist";
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(bookMapper);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Delete book by valid id, soft deletion of existing book")
    void deleteById_ValidBookId_SoftDeleteByBookId() {
        when(bookRepository.existsById(anyLong())).thenReturn(true);
        bookService.deleteById(anyLong());
        verify(bookRepository, times(1)).deleteById(anyLong());
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Delete book by invalid id, returns EntityNotFoundException")
    void deleteById_InvalidBookId_ReturnsException() {
        when(bookRepository.existsById(INVALID_ID)).thenReturn(false);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.deleteById(INVALID_ID)
        );
        String expected = "Book by id: " + INVALID_ID
                + " does not exist and can't be deleted";
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(bookRepository, times(1)).existsById(anyLong());
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Update book by valid id, returns valid book dto")
    void update_BookByValidId_ReturnsUpdatedBook() {
        when(bookRepository.existsById(BOOK_ID)).thenReturn(true);
        when(bookMapper.toEntity(REQUEST_DTO)).thenReturn(BOOK);
        when(bookRepository.save(BOOK)).thenReturn(BOOK);
        when(bookMapper.toDto(BOOK)).thenReturn(RESPONSE_DTO);
        BookResponseDto actual = bookService.update(BOOK_ID, REQUEST_DTO);
        Assertions.assertEquals(RESPONSE_DTO, actual);
        verify(bookRepository, times(1)).save(any());
        verify(bookMapper, times(1)).toDto(any());
        verify(bookMapper, times(1)).toEntity(any());
        verifyNoMoreInteractions(bookRepository);
        verifyNoMoreInteractions(bookMapper);
    }

    @Test
    @DisplayName("Update book by invalid id, returns EntityNotFoundException")
    void update_InvalidBookId_ReturnsException() {
        when(bookRepository.existsById(INVALID_ID)).thenReturn(false);
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class,
                () -> bookService.update(INVALID_ID, REQUEST_DTO)
        );
        String expected = "Can't update book by id: " + INVALID_ID
                + " as it does not exist";
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(bookRepository, times(1)).existsById(any());
        verifyNoMoreInteractions(bookRepository);
        verifyNoMoreInteractions(bookMapper);
    }

    @Test
    @DisplayName("Search book by valid parameters, returns valid list of one book")
    void searchBooks_ValidBookList_ReturnsListOfOneBook() {
        BookSearchParametersDto searchParametersDto = new BookSearchParametersDto(
                new String[]{},
                new String[]{"Andrzej Sapkowski"},
                new String[]{},
                new String[]{});
        Specification<Book> specification = mock(Specification.class);
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        List<Book> books = Collections.singletonList(BOOK);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        when(bookSpecificationBuilder.build(searchParametersDto)).thenReturn(specification);
        when(bookMapper.toDto(BOOK)).thenReturn(RESPONSE_DTO);
        when(bookRepository.findAll(specification, pageable)).thenReturn(bookPage);
        List<BookResponseDto> expected = Collections.singletonList(RESPONSE_DTO);
        List<BookResponseDto> actual = bookService.searchBooks(searchParametersDto, pageable);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Search book by parameters, wich does not match any book, returns empty list")
    void searchBooks_SearchParametersDoesNotMatchAnyBook_ReturnsEmptyList() {
        BookSearchParametersDto searchParametersDto = new BookSearchParametersDto(
                new String[]{"Book"},
                new String[]{"Timothy"},
                new String[]{},
                new String[]{"10", "15"});
        Specification<Book> specification = mock(Specification.class);
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        List<Book> books = Collections.emptyList();
        Page<Book> bookPage = new PageImpl<>(books, pageable, 0);
        when(bookSpecificationBuilder.build(searchParametersDto)).thenReturn(specification);
        when(bookRepository.findAll(specification, pageable)).thenReturn(bookPage);
        List<BookResponseDto> actual = bookService.searchBooks(searchParametersDto, pageable);
        Assertions.assertEquals(0, actual.size());
        verify(bookRepository, times(1)).findAll(specification, pageable);
        verify(bookSpecificationBuilder, times(1)).build(searchParametersDto);
        verifyNoMoreInteractions(bookRepository);
        verifyNoMoreInteractions(bookSpecificationBuilder);
        verifyNoMoreInteractions(bookMapper);
    }

    @Test
    @DisplayName("Find books by category id, returns valid list of one book")
    void getBooksByCategoryId_ValidCategoryId_ReturnsBookList() {
        BookDtoWithoutCategoryIds bookDtoWithoutCategoryIds = new BookDtoWithoutCategoryIds();
        bookDtoWithoutCategoryIds.setId(BOOK_ID)
                .setTitle(BOOK.getTitle())
                .setAuthor(BOOK.getAuthor())
                .setIsbn(BOOK.getIsbn())
                .setPrice(BOOK.getPrice())
                .setDescription(BOOK.getDescription())
                .setCoverImage(BOOK.getCoverImage());
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        List<Book> books = Collections.singletonList(BOOK);
        when(bookMapper.toDtoWithoutCategories(BOOK)).thenReturn(bookDtoWithoutCategoryIds);
        when(bookRepository.findBooksByCategoryId(CATEGORY_ID, pageable)).thenReturn(books);
        List<BookDtoWithoutCategoryIds> expected = Collections
                .singletonList(bookDtoWithoutCategoryIds);
        List<BookDtoWithoutCategoryIds> actual = bookService
                .getBooksByCategoryId(CATEGORY_ID, pageable);
        Assertions.assertEquals(expected, actual);
        verify(bookRepository, times(1)).findBooksByCategoryId(CATEGORY_ID, pageable);
        verify(bookMapper, times(1)).toDtoWithoutCategories(BOOK);
        verifyNoMoreInteractions(bookRepository);
        verifyNoMoreInteractions(bookMapper);
    }

    @Test
    @DisplayName("Find books by invalid category id, returns empty list")
    void getBooksByCategoryId_InvalidCategoryId_ReturnsEmptyList() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        List<Book> books = Collections.emptyList();
        when(bookRepository.findBooksByCategoryId(INVALID_ID, pageable)).thenReturn(books);
        List<BookDtoWithoutCategoryIds> actual = bookService
                .getBooksByCategoryId(INVALID_ID, pageable);
        Assertions.assertEquals(0, actual.size());
        verify(bookRepository, times(1)).findBooksByCategoryId(INVALID_ID, pageable);
        verifyNoMoreInteractions(bookMapper);
    }
}
