package ru.yandex.practicum.filmorate.storage.dao.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewDao {
    Review create(Review review);

    Review update(Review review);

    Review findById(Long id);

    List<Review> findAll(Long filmId, Integer count);

    void delete(Long id);

    boolean checkExist(Long id);
}