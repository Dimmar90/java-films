package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    /*private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<Film>> violations;*/
    private final HashMap<Integer, Film> films = new HashMap<>();
    private Integer filmId = 1;

    @PostMapping //("/post")
    public Film create(@Valid @RequestBody Film film) {
        /*violations = validator.validate(film);
        if (!violations.isEmpty()) return film;*/

        film.setId(filmId++);
        films.put(film.getId(), film);
        log.info("Film {} has been CREATED", film);
        return film;
    }

    @PutMapping //("/update")
    public Film update(@Valid @RequestBody Film film) {
        //violations = validator.validate(film);
        if (film.getId() == null || !films.containsKey(film.getId())) {
            throw new ValidationException("Выполните post-запрос или введите корректный ID фильма");
        }
        //if (!violations.isEmpty()) return film;

        films.put(film.getId(), film);
        log.info("Film {} has been UPDATED", film);
        return film;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return  films.values();
    }
}
