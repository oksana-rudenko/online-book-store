package springboot.onlinebookstore.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import springboot.onlinebookstore.dto.book.BookSearchParametersDto;
import springboot.onlinebookstore.dto.book.request.CreateBookRequestDto;
import springboot.onlinebookstore.dto.book.response.BookDto;

public interface BookService {
    BookDto save(CreateBookRequestDto requestDto);

    List<BookDto> findAll(Pageable pageable);

    BookDto findById(Long id);

    void deleteById(Long id);

    BookDto update(Long id, CreateBookRequestDto requestDto);

    List<BookDto> searchBooks(BookSearchParametersDto searchParameters, Pageable pageable);

}
