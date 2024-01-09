package springboot.onlinebookstore.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot.onlinebookstore.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
