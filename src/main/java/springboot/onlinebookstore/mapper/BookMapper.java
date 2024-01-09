package springboot.onlinebookstore.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import springboot.onlinebookstore.config.MapperConfig;
import springboot.onlinebookstore.dto.book.request.CreateBookRequestDto;
import springboot.onlinebookstore.dto.book.response.BookDtoWithoutCategoryIds;
import springboot.onlinebookstore.dto.book.response.BookResponseDto;
import springboot.onlinebookstore.model.Book;
import springboot.onlinebookstore.model.Category;

@Mapper(config = MapperConfig.class)
public interface BookMapper {
    @Mapping(target = "categoryIds", ignore = true)
    BookResponseDto toDto(Book book);

    @AfterMapping
    default void setCategoryIds(@MappingTarget BookResponseDto bookDto, Book book) {
        Set<Long> categoryIds = book.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        bookDto.setCategoryIds(categoryIds);
    }

    BookDtoWithoutCategoryIds toDtoWithoutCategories(Book book);

    @Mapping(target = "categories", ignore = true)
    Book toEntity(CreateBookRequestDto requestDto);

    @AfterMapping
    default void setCategories(@MappingTarget Book book, CreateBookRequestDto requestDto) {
        Set<Category> categories = requestDto.getCategoryIds().stream()
                .map(Category::new)
                .collect(Collectors.toSet());
        book.setCategories(categories);
    }
}
