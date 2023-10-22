package springboot.onlinebookstore.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class ImageValidator implements ConstraintValidator<Image, String> {
    private static final String PATTERN_OF_IMAGE = ".*?(gif|jpeg|png|jpg|img|bmp)";

    @Override
    public boolean isValid(String image, ConstraintValidatorContext constraintValidatorContext) {
        return image != null && Pattern.compile(PATTERN_OF_IMAGE).matcher(image).matches();
    }
}
