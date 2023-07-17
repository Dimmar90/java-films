package ru.yandex.practicum.filmorate.storage.dao.film;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

public interface GenreDao {

    void addGenreInFilm(Integer filmId, Integer genreId);

    Set<Genre> getFilmGenres(Integer filmId);

    Genre getGenre(Integer id);

    List<Genre> getAllGenres();

    void deleteFilmGenre(Integer filmId);
}
