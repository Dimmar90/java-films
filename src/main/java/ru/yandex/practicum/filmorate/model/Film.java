package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.validator.IsAfterDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * название не может быть пустым;
 * максимальная длина описания — 200 символов;
 * дата релиза — не раньше 28 декабря 1895 года;
 * продолжительность фильма должна быть положительной.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Film {
    private Long id;
    @NotBlank(message = "Film name must be completed")
    private String name;
    @Size(max = 200, message = "Description of the film must be no more than 200 characters")
    private String description;
    @Positive(message = "Film duration must be a positive number")
    private int duration;
    @IsAfterDate(value = "1895-12-28", message = "Release date cannot be before 1895-12-28")
    private LocalDate releaseDate;
    @NotNull(message = "Film rating MPA must be completed")
    private Mpa mpa;
    private Set<Genre> genres;
    private Set<Director> directors;
    @Setter(AccessLevel.NONE)
    private Set<Integer> likes = new HashSet<>(); // для реализации InMemory storage
}
