package springboot.onlinebookstore.repository;

import org.springframework.data.jpa.domain.Specification;
import springboot.onlinebookstore.dto.BookSearchParametersDto;

public interface SpecificationBuilder<T> {
    Specification<T> build(BookSearchParametersDto searchParameters);
}
