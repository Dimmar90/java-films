package ru.yandex.practicum.filmorate.storage.dao.film;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface DirectorDao {
    Director create(Director director);

    Director update(Director director);

    List<Director> findAll();

    Optional<Director> findById(Long id);

    void delete(Long id);

    boolean checkExist(Long id);

    void addToFilm(Long directorId, Long filmId);

    void deleteFromFilm(Long filmId);

    void addDirectorsToFilm(Film film);
}
