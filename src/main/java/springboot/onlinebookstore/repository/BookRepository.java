package springboot.onlinebookstore.repository;

import java.util.List;
import springboot.onlinebookstore.model.Book;

public interface BookRepository {
    Book save(Book book);

    List<Book> findAll();
}
