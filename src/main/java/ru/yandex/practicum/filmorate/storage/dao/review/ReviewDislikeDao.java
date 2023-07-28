package ru.yandex.practicum.filmorate.storage.dao.review;

public interface ReviewDislikeDao {
    void add(Long id, Long userId);

    void delete(Long id, Long userId);
}
