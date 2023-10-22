package springboot.onlinebookstore.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import springboot.onlinebookstore.validation.Image;
import springboot.onlinebookstore.validation.Isbn;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateBookRequestDto {
    @NotNull
    @NotEmpty
    private String title;
    @NotNull
    @NotEmpty
    private String author;
    @NotNull
    @Isbn
    private String isbn;
    @Min(0)
    private BigDecimal price;
    private String description;
    @Image
    private String coverImage;
}
