package ru.yandex.practicum.filmorate.storage.dao.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;

public interface FilmDao extends FilmStorage {
    List<Film> findTop(Integer count, Integer genreId, Integer year);

    List<Film> findCommon(Integer userId, Integer friendId);

    boolean checkExist(Integer id);

    List<Film> findDirectorsFilmsSortedByRate(Integer directorId);

    List<Film> findDirectorsFilmsSortedByYears(Integer directorId);

    void delete(Integer filmId);

    List<Film> search(String keyWord, String whereSearch);
}
