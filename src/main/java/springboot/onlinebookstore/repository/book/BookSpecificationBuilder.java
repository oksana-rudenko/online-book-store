package springboot.onlinebookstore.repository.book;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import springboot.onlinebookstore.dto.BookSearchParametersDto;
import springboot.onlinebookstore.model.Book;
import springboot.onlinebookstore.repository.SpecificationBuilder;
import springboot.onlinebookstore.repository.SpecificationProviderManager;

@RequiredArgsConstructor
@Component
public class BookSpecificationBuilder implements SpecificationBuilder<Book> {
    private final SpecificationProviderManager<Book> bookSpecificationProviderManager;

    @Override
    public Specification<Book> build(BookSearchParametersDto searchParameters) {
        Specification<Book> specification = Specification.where(null);
        if (searchParameters.title() != null && searchParameters.title().length > 0) {
            Specification<Book> titleSpecification = bookSpecificationProviderManager
                    .getSpecificationProvider("title").getSpecification(searchParameters.title());
            specification = specification.and(titleSpecification);
        }
        if (searchParameters.author() != null && searchParameters.author().length > 0) {
            Specification<Book> authorSpecification = bookSpecificationProviderManager
                    .getSpecificationProvider("author")
                    .getSpecification(searchParameters.author());
            specification = specification.and(authorSpecification);
        }
        if (searchParameters.isbn() != null && searchParameters.isbn().length > 0) {
            Specification<Book> isbnSpecification = bookSpecificationProviderManager
                    .getSpecificationProvider("isbn").getSpecification(searchParameters.isbn());
            specification = specification.and(isbnSpecification);
        }
        if (searchParameters.price() != null && searchParameters.price().length > 0) {
            Specification<Book> priceSpecification = bookSpecificationProviderManager
                    .getSpecificationProvider("price").getSpecification(searchParameters.price());
            specification = specification.and(priceSpecification);
        }
        if (searchParameters.description() != null && searchParameters.description().length > 0) {
            Specification<Book> descriptionSpecification = bookSpecificationProviderManager
                    .getSpecificationProvider("description")
                    .getSpecification(searchParameters.description());
            specification = specification.and(descriptionSpecification);
        }
        return specification;
    }
}
