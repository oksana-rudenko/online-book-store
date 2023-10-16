package springboot.onlinebookstore.repository.book.spec;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import springboot.onlinebookstore.model.Book;
import springboot.onlinebookstore.repository.SpecificationProvider;

@Component
public class PriceSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public String getKey() {
        return "price";
    }

    @Override
    public Specification<Book> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> {
            int minPrice = Arrays.stream(params).mapToInt(Integer::parseInt).min().getAsInt();
            int maxPrice = Arrays.stream(params).mapToInt(Integer::parseInt).max().getAsInt();
            if (params.length == 1) {
                minPrice = 0;
                maxPrice = Integer.parseInt(params[0]);
            }
            Predicate lowPrice = criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("price"), BigDecimal.valueOf(minPrice)),
                    criteriaBuilder.gt(root.get("price"), BigDecimal.valueOf(minPrice)));
            Predicate highPrice = criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("price"), BigDecimal.valueOf(maxPrice)),
                    criteriaBuilder.lt(root.get("price"), BigDecimal.valueOf(maxPrice)));
            return criteriaBuilder.and(lowPrice, highPrice);
        };
    }
}
