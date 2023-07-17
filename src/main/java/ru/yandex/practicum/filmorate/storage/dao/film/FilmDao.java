package ru.yandex.practicum.filmorate.storage.dao.film;

import ru.yandex.practicum.filmorate.storage.FilmStorage;

public interface FilmDao extends FilmStorage {
    boolean checkFilmExist(Integer id);
}
