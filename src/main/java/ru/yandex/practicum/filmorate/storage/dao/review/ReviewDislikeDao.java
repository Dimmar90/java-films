package ru.yandex.practicum.filmorate.storage.dao.review;

public interface ReviewDislikeDao {
    void add(Integer id, Integer userId);

    void delete(Integer id, Integer userId);
}
