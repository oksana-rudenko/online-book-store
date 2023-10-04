package springboot.onlinebookstore.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springboot.onlinebookstore.dto.response.BookDto;
import springboot.onlinebookstore.dto.request.CreateBookRequestDto;
import springboot.onlinebookstore.exception.EntityNotFoundException;
import springboot.onlinebookstore.mapper.BookMapper;
import springboot.onlinebookstore.model.Book;
import springboot.onlinebookstore.repository.BookRepository;
import springboot.onlinebookstore.service.BookService;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public BookDto save(CreateBookRequestDto requestDto) {
        Book book = bookMapper.toModel(requestDto);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public BookDto findById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Book by id: " + id + " does not exist")
        );
        return bookMapper.toDto(book);
    }
}
