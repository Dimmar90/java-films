package ru.yandex.practicum.filmorate.storage.dao.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewDao {
    Review create(Review review);

    Review update(Review review);

    Review findById(Integer id);

    List<Review> findAll(Integer filmId, Integer count);

    void delete(Integer id);

    boolean checkExist(Integer id);
}