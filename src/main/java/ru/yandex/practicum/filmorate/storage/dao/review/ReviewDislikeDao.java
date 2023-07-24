package ru.yandex.practicum.filmorate.storage.dao.review;

public interface ReviewDislikeDao {
    void setDislike(Integer id, Integer userId);

    void deleteDislike(Integer id, Integer userId);
}
