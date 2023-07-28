package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmDao;
import ru.yandex.practicum.filmorate.storage.dao.review.ReviewDao;
import ru.yandex.practicum.filmorate.storage.dao.review.ReviewDislikeDao;
import ru.yandex.practicum.filmorate.storage.dao.review.ReviewLikeDao;
import ru.yandex.practicum.filmorate.storage.dao.user.EventDao;
import ru.yandex.practicum.filmorate.storage.dao.user.UserDao;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewDao reviewDao;
    private final ReviewLikeDao reviewLikeDao;
    private final ReviewDislikeDao reviewDislikeDao;
    private final FilmDao filmDao;
    private final UserDao userDao;
    private final EventDao eventDao;

    public Review create(Review review) {
        userDao.checkExist(review.getUserId());
        filmDao.checkExist(review.getFilmId());
        review = reviewDao.create(review);

        log.info("Review {} has been CREATED", review);
        eventDao.add(review.getUserId(), "REVIEW", "ADD", review.getReviewId()); // добавляю событие в ленту
        return review;
    }

    public Review update(Review review) {
        if (review.getReviewId() == null || !reviewDao.checkExist(review.getReviewId())) {
            throw new NotFoundException("Can't update review with ID = null");
        }
        review = reviewDao.update(review);
        log.info("Review {} has been UPDATED", review);
        eventDao.add(review.getUserId(), "REVIEW", "UPDATE", review.getFilmId()); // добавляю событие в ленту
        return review;
    }

    public Review getById(Long id) {
        reviewDao.checkExist(id);
        log.info("Get a review with ID = {}", id);
        return reviewDao.findById(id);
    }

    public List<Review> getAll(Long filmId, Integer count) {
        if (filmId != null) { // Если передан id фильма - получаем все отзывы этого фильма
            filmDao.checkExist(filmId);
        }
        List<Review> reviews = reviewDao.findAll(filmId, count);
        log.info("Get {} reviews", reviews.size());
        return reviews;
    }

    public void addLike(Long id, Long userId) {
        reviewDao.checkExist(id);
        userDao.checkExist(userId);
        reviewLikeDao.add(id, userId);
        log.info("User ID = {} liked review ID = {}", id, userId);
    }

    public void addDislike(Long id, Long userId) {
        reviewDao.checkExist(id);
        userDao.checkExist(userId);
        reviewDislikeDao.add(id, userId);
        log.info("User ID = {} disliked review ID = {}", id, userId);
    }

    public void deleteLike(Long id, Long userId) {
        reviewDao.checkExist(id);
        userDao.checkExist(userId);
        reviewLikeDao.delete(id, userId);
        log.info("User ID = {} deleted like review ID = {}", id, userId);
    }

    public void deleteDislike(Long id, Long userId) {
        reviewDao.checkExist(id);
        userDao.checkExist(userId);
        reviewDislikeDao.delete(id, userId);
        log.info("User ID = {} deleted dislike review ID = {}", id, userId);
    }

    public void delete(Long id) {
        reviewDao.checkExist(id);
        eventDao.add(getById(id).getUserId(), "REVIEW", "REMOVE", getById(id).getFilmId()); // добавляю событие в ленту
        reviewDao.delete(id);
        log.info("Deleted review ID = {}", id);
    }
}
