package ru.yandex.practicum.filmorate.storage.dao.film;

public interface FilmLikesDao {
    void like(Integer id, Integer userId);

    void unlike(Integer id, Integer userId);
}
