package springboot.onlinebookstore.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import springboot.onlinebookstore.dto.book.BookSearchParametersDto;
import springboot.onlinebookstore.dto.book.request.CreateBookRequestDto;
import springboot.onlinebookstore.dto.book.response.BookDtoWithoutCategoryIds;
import springboot.onlinebookstore.dto.book.response.BookResponseDto;
import springboot.onlinebookstore.exception.EntityNotFoundException;
import springboot.onlinebookstore.mapper.BookMapper;
import springboot.onlinebookstore.model.Book;
import springboot.onlinebookstore.repository.book.BookRepository;
import springboot.onlinebookstore.repository.book.BookSpecificationBuilder;
import springboot.onlinebookstore.service.BookService;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder bookSpecificationBuilder;

    @Override
    public BookResponseDto save(CreateBookRequestDto requestDto) {
        Book book = bookMapper.toEntity(requestDto);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public List<BookResponseDto> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public BookResponseDto findById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Book by id: " + id + " does not exist")
        );
        return bookMapper.toDto(book);
    }

    @Override
    public void deleteById(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Book by id: " + id
                    + " does not exist and can't be deleted");
        }
        bookRepository.deleteById(id);
    }

    @Override
    public BookResponseDto update(Long id, CreateBookRequestDto requestDto) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't update book by id: " + id
                    + " as it does not exist");
        }
        Book book = bookMapper.toEntity(requestDto);
        book.setId(id);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public List<BookResponseDto> searchBooks(BookSearchParametersDto searchParameters,
                                             Pageable pageable) {
        Specification<Book> specification = bookSpecificationBuilder.build(searchParameters);
        return bookRepository.findAll(specification, pageable).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public List<BookDtoWithoutCategoryIds> getBooksByCategoryId(Long id, Pageable pageable) {
        return bookRepository.findBooksByCategoryId(id, pageable).stream()
                .map(bookMapper::toDtoWithoutCategories)
                .toList();
    }
}
