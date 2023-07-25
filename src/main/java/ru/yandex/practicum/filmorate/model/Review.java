package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {
    private Integer reviewId;
    @NotBlank(message = "Review content must be completed")
    private String content;
    @NotNull(message = "Review type must be positive(true) or negative(false)")
    private Boolean isPositive;
    @NotNull(message = "Review userId cannot be empty")
    private Integer userId;
    @NotNull(message = "Review filmId cannot be empty")
    private Integer filmId;
    @Setter(AccessLevel.NONE)
    private int useful;
}
