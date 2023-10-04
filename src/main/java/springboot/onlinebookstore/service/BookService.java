package springboot.onlinebookstore.service;

import java.util.List;
import springboot.onlinebookstore.dto.response.BookDto;
import springboot.onlinebookstore.dto.request.CreateBookRequestDto;

public interface BookService {
    BookDto save(CreateBookRequestDto requestDto);

    List<BookDto> findAll();

    BookDto findById(Long id);
}
