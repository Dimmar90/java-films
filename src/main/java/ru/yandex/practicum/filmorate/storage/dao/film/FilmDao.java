package ru.yandex.practicum.filmorate.storage.dao.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;

public interface FilmDao extends FilmStorage {
    List<Film> getCommonFilms(Integer userId, Integer friendId);

    boolean checkFilmExist(Integer id);


    List<Film> findDirectorsFilmsSortedByRate(Integer directorId);

    List<Film> findDirectorsFilmsSortedByYears(Integer directorId);


    void deleteFilmById(Integer filmId);

    List<Film> getTopFilms(Integer count, Integer genreId, Integer year);
}
