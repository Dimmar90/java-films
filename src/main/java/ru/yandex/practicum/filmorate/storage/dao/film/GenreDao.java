package ru.yandex.practicum.filmorate.storage.dao.film;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

public interface GenreDao {

    void add(Long filmId, Integer genreId);

    Set<Genre> findFilmGenres(Long filmId);

    Genre findById(Long id);

    List<Genre> findAll();

    void deleteFilmGenres(Long filmId);
}
