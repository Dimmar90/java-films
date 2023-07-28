package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable("id") Long filmId) {
        return filmService.getById(filmId);
    }

    @GetMapping("/popular")
    public List<Film> getTopFilms(
            @Positive @RequestParam(value = "count", defaultValue = "10") Integer count,
            @RequestParam(value = "genreId", required = false) Integer genreId,
            @RequestParam(value = "year", required = false) Integer year) {
        return filmService.getTop(count, genreId, year);
    }

    @GetMapping("/common") //?userId={userId}&friendId={friendId}
    public List<Film> getCommonFilms(@RequestParam("userId") Long userId, @RequestParam("friendId") Long id) {
        return filmService.getCommon(userId, id);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getDirectorsFilms(@PathVariable("directorId") Long directorId, @RequestParam String sortBy) {
        return filmService.getDirectorsFilms(directorId, sortBy);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable("filmId") Long filmId) {
        filmService.delete(filmId);
    }

    @GetMapping("/search")
    public List<Film> search(@RequestParam("query") String keyWord,
                             @RequestParam("by") String whereSearch) {
        return filmService.search(keyWord, whereSearch);
    }

}