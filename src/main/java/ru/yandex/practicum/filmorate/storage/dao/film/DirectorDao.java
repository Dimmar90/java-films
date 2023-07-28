package ru.yandex.practicum.filmorate.storage.dao.film;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface DirectorDao {
    Director create(Director director);

    Director update(Director director);

    List<Director> findAll();

    Optional<Director> findById(Integer id);

    void delete(Integer id);

    boolean checkExist(Integer id);

    void addToFilm(Integer directorId, Integer filmId);

    void deleteFromFilm(Integer filmId);

    void addDirectorsToFilm(Film film);
}
