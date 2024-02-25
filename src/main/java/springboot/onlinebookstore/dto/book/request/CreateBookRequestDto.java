package springboot.onlinebookstore.dto.book.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.ISBN;
import springboot.onlinebookstore.validation.Image;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class CreateBookRequestDto {
    @NotNull
    @NotEmpty
    private String title;
    @NotNull
    @NotEmpty
    private String author;
    @NotNull
    @ISBN
    private String isbn;
    @Min(0)
    private BigDecimal price;
    private String description;
    @Image
    private String coverImage;
    @NotNull
    private Set<Long> categoryIds;
}
