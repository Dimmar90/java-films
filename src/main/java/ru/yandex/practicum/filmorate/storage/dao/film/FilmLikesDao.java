package ru.yandex.practicum.filmorate.storage.dao.film;

public interface FilmLikesDao {
    void like(Long id, Long userId);

    void unlike(Long id, Long userId);
}
