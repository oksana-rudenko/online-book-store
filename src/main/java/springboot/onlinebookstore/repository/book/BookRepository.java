package springboot.onlinebookstore.repository.book;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import springboot.onlinebookstore.model.Book;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book>,
        PagingAndSortingRepository<Book, Long> {
    @Query("FROM Book b "
            + "LEFT JOIN FETCH b.categories c "
            + "WHERE c.id = :categoryId")
    List<Book> findBooksByCategoryId(Long categoryId, Pageable pageable);
}
