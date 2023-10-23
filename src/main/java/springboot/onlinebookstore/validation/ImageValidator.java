package springboot.onlinebookstore.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class ImageValidator implements ConstraintValidator<Image, String> {
    private static final Pattern PATTERN_COMPILE = Pattern.compile(".*?(gif|jpeg|png|jpg|img|bmp)");

    @Override
    public boolean isValid(String image, ConstraintValidatorContext constraintValidatorContext) {
        return image != null && PATTERN_COMPILE.matcher(image).matches();
    }
}
