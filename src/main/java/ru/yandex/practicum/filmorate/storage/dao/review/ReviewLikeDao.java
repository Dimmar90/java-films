package ru.yandex.practicum.filmorate.storage.dao.review;

public interface ReviewLikeDao {
    void add(Integer id, Integer userId);

    void delete(Integer id, Integer userId);
}
