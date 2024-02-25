package springboot.onlinebookstore.service.category;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
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
import springboot.onlinebookstore.dto.category.request.CategoryRequestDto;
import springboot.onlinebookstore.dto.category.response.CategoryResponseDto;
import springboot.onlinebookstore.exception.EntityNotFoundException;
import springboot.onlinebookstore.mapper.CategoryMapper;
import springboot.onlinebookstore.model.Category;
import springboot.onlinebookstore.repository.category.CategoryRepository;
import springboot.onlinebookstore.service.impl.CategoryServiceImpl;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    private static final Long INVALID_ID = -1L;
    private static final Long CATEGORY_ID = 1L;
    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 10;
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryServiceImpl categoryService;
    @Mock
    private CategoryMapper categoryMapper;

    @Test
    @DisplayName("Save valid category from valid request dto")
    void save_ValidCategory_ReturnsValidCategoryResponseDto() {
        Category category = getCategory();
        CategoryRequestDto requestDto =
                new CategoryRequestDto(category.getName(), category.getDescription());
        CategoryResponseDto responseDto = getResponseDto(category);
        when(categoryMapper.toEntity(requestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(responseDto);
        CategoryResponseDto actual = categoryService.save(requestDto);
        Assertions.assertEquals(responseDto, actual);
    }

    @Test
    @DisplayName("Find all categories from database, returns valid list")
    void findAll_ValidCategoryList_ReturnsValidList() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        Category category = getCategory();
        CategoryResponseDto responseDto = getResponseDto(category);
        List<Category> categories = Collections.singletonList(category);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());
        when(categoryMapper.toDto(category)).thenReturn(responseDto);
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        List<CategoryResponseDto> expected = Collections.singletonList(responseDto);
        List<CategoryResponseDto> actual = categoryService.findAll(pageable);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Find category by id, returns valid response dto")
    void findById_ValidId_ReturnsValidCategory() {
        Category category = getCategory();
        CategoryResponseDto responseDto = getResponseDto(category);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(responseDto);
        CategoryResponseDto actual = categoryService.getById(CATEGORY_ID);
        Assertions.assertEquals(responseDto, actual);
    }

    @Test
    @DisplayName("Find category by invalid id, returns exception")
    void findById_InvalidId_ReturnsException() {
        when(categoryRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> categoryService.getById(INVALID_ID)
        );
        String expected = "Can't find category by id: " + INVALID_ID;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(categoryMapper);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Delete category by id, soft deletion of category")
    void deleteById_ValidId_SoftDeleteById() {
        when(categoryRepository.existsById(anyLong())).thenReturn(true);
        categoryService.deleteById(CATEGORY_ID);
        verify(categoryRepository, times(1)).deleteById(anyLong());
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Delete category by invalid id, returns exception")
    void deleteById_InvalidId_ReturnsException() {
        when(categoryRepository.existsById(INVALID_ID)).thenReturn(false);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> categoryService.deleteById(INVALID_ID)
        );
        String expected = "Category by id: " + INVALID_ID + " does not exist and can't be deleted";
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(categoryRepository, times(1)).existsById(anyLong());
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Update category by valid id, returns updated category")
    void update_ValidCategory_ReturnsUpdatedCategory() {
        Category category = getCategory();
        CategoryRequestDto requestDto =
                new CategoryRequestDto(category.getName(), category.getDescription());
        CategoryResponseDto responseDto = getResponseDto(category);
        when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);
        when(categoryMapper.toEntity(requestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(responseDto);
        CategoryResponseDto actual = categoryService.update(CATEGORY_ID, requestDto);
        Assertions.assertEquals(responseDto, actual);
        verify(categoryRepository, times(1)).save(any());
        verify(categoryMapper, times(1)).toDto(any());
        verify(categoryMapper, times(1)).toEntity(any());
        verifyNoMoreInteractions(categoryRepository);
        verifyNoMoreInteractions(categoryMapper);
    }

    @Test
    @DisplayName("Update category by invalid id, returns exception")
    void update_InvalidId_ReturnsException() {
        CategoryRequestDto requestDto =
                new CategoryRequestDto("History", "Europe History");
        when(categoryRepository.existsById(INVALID_ID)).thenReturn(false);
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class,
                () -> categoryService.update(INVALID_ID, requestDto)
        );
        String expected = "Category by id: " + INVALID_ID + " does not exist";
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(categoryRepository, times(1)).existsById(any());
        verifyNoMoreInteractions(categoryRepository);
        verifyNoMoreInteractions(categoryMapper);
    }

    private Category getCategory() {
        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setName("History");
        category.setDescription("Europe History");
        return category;
    }

    private CategoryResponseDto getResponseDto(Category category) {
        return new CategoryResponseDto(category.getId(),
                category.getName(), category.getDescription());
    }
}
