package ru.yandex.practicum.filmorate.storage.dao.review;

public interface ReviewLikeDao {
    void setLike(Integer id, Integer userId);

    void deleteLike(Integer id, Integer userId);
}
