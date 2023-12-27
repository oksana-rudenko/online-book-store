package springboot.onlinebookstore.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import springboot.onlinebookstore.dto.category.request.CategoryRequestDto;
import springboot.onlinebookstore.dto.category.response.CategoryResponseDto;

public interface CategoryService {
    CategoryResponseDto save(CategoryRequestDto categoryDto);

    List<CategoryResponseDto> findAll(Pageable pageable);

    CategoryResponseDto getById(Long id);

    CategoryResponseDto update(Long id, CategoryRequestDto categoryDto);

    void deleteById(Long id);
}
