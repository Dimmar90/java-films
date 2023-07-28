package ru.yandex.practicum.filmorate.storage.dao.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmDao {

    Film create(Film film);

    Film update(Film film);


    List<Film> findAll();

    Film findById(Long id);

    List<Film> findTop(Integer count);

    List<Film> findTop(Integer count, Integer genreId, Integer year);

    List<Film> findCommon(Long userId, Long friendId);

    boolean checkExist(Long id);

    List<Film> findDirectorsFilmsSortedByRate(Long directorId);

    List<Film> findDirectorsFilmsSortedByYears(Long directorId);

    void delete(Long filmId);

    List<Film> search(String keyWord, String whereSearch);
}
