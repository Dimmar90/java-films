package ru.yandex.practicum.filmorate.storage.dao.review;

public interface ReviewLikeDao {
    void add(Long id, Long userId);

    void delete(Long id, Long userId);
}
