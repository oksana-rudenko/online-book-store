package springboot.onlinebookstore.service;

import java.util.List;
import springboot.onlinebookstore.model.Book;

public interface BookService {
    Book save(Book book);

    List<Book> findAll();
}
