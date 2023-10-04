package springboot.onlinebookstore.service;

import java.util.List;
import springboot.onlinebookstore.dto.request.CreateBookRequestDto;
import springboot.onlinebookstore.dto.response.BookDto;

public interface BookService {
    BookDto save(CreateBookRequestDto requestDto);

    List<BookDto> findAll();

    BookDto findById(Long id);
}
