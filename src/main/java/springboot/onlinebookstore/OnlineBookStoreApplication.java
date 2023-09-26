package springboot.onlinebookstore;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springboot.onlinebookstore.model.Book;
import springboot.onlinebookstore.service.BookService;

@SpringBootApplication
public class OnlineBookStoreApplication {
    @Autowired
    private BookService bookService;

    public static void main(String[] args) {
        SpringApplication.run(OnlineBookStoreApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                Book book = new Book();
                book.setTitle("The Witcher");
                book.setAuthor("Andrzej Sapkowski");
                book.setIsbn("9781473232273");
                book.setPrice(BigDecimal.valueOf(11));
                book.setDescription("Magic adventures");
                bookService.save(book);
                System.out.println(bookService.findAll());
            }
        };
    }
}
