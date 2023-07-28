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
    public void addLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable("id") Integer filmId) {
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
    public List<Film> getCommonFilms(@RequestParam("userId") Integer userId, @RequestParam("friendId") Integer id) {
        return filmService.getCommon(userId, id);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getDirectorsFilms(@PathVariable("directorId") Integer directorId, @RequestParam String sortBy) {
        return filmService.getDirectorsFilms(directorId, sortBy);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable("filmId") Integer filmId) {
        filmService.delete(filmId);
    }

    @GetMapping("/search")
    public List<Film> search(@RequestParam("query") String keyWord,
                             @RequestParam("by") String whereSearch) {
        return filmService.search(keyWord, whereSearch);
    }

}