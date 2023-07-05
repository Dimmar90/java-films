package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class IsAfterDateValidator implements ConstraintValidator<IsAfterDate, LocalDate> {
    private LocalDate date;

    @Override
    public void initialize(IsAfterDate constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.date = LocalDate.parse(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return !value.isBefore(date); // Должно выполняться t1 >= t2
    }

}
