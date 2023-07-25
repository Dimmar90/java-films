package ru.yandex.practicum.filmorate.storage.dao.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewDao {
    Review create(Review review);

    Review update(Review review);

    Review getById(Integer id);

    List<Review> getAllReviews(Integer filmId, Integer count);

    void deleteById(Integer id);

    boolean checkReviewExist(Integer id);
}