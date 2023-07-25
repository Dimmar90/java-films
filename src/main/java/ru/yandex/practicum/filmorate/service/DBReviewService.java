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
import ru.yandex.practicum.filmorate.storage.dao.user.UserDao;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DBReviewService {
    private final ReviewDao reviewDao;
    private final ReviewLikeDao reviewLikeDao;
    private final ReviewDislikeDao reviewDislikeDao;
    private final FilmDao filmDao;
    private final UserDao userDao;

    public Review create(Review review) {
        userDao.checkUserExist(review.getUserId());
        filmDao.checkFilmExist(review.getFilmId());
        review = reviewDao.create(review);

        log.info("Review {} has been CREATED", review);
        return review;
    }

    public Review update(Review review) {
        if (review.getReviewId() == null || !reviewDao.checkReviewExist(review.getReviewId())) {
            throw new NotFoundException("Can't update review with ID = null");
        }
        review = reviewDao.update(review);
        log.info("Review {} has been UPDATED", review);
        return review;
    }

    public Review getReview(Integer id) {
        if (id == null || !reviewDao.checkReviewExist(id)) {
            throw new NotFoundException("Can't get a review with ID = null");
        }
        log.info("Get a review with ID = {}", id);
        return reviewDao.getById(id);
    }

    public List<Review> getALLReviews(Integer filmId, Integer count) {
        if (filmId != null) filmDao.checkFilmExist(filmId);
        List<Review> reviews = reviewDao.getAllReviews(filmId, count);
        log.info("Get {} reviews", reviews.size());
        return reviews;
    }

    public void addLike(Integer id, Integer userId) {
        checkExistReviewAndUser(id, userId);
        reviewLikeDao.setLike(id, userId);
        log.info("User ID = {} liked review ID = {}", id, userId);
    }

    public void addDislike(Integer id, Integer userId) {
        checkExistReviewAndUser(id, userId);
        reviewDislikeDao.setDislike(id, userId);
        log.info("User ID = {} disliked review ID = {}", id, userId);
    }

    public void deleteLike(Integer id, Integer userId) {
        checkExistReviewAndUser(id, userId);
        reviewLikeDao.deleteLike(id, userId);
        log.info("User ID = {} deleted like review ID = {}", id, userId);
    }

    public void deleteDislike(Integer id, Integer userId) {
        checkExistReviewAndUser(id, userId);
        reviewDislikeDao.deleteDislike(id, userId);
        log.info("User ID = {} deleted dislike review ID = {}", id, userId);
    }

    public void delete(Integer id) {
        if (id == null || !reviewDao.checkReviewExist(id)) {
            throw new NotFoundException("Can't delete a review with ID = null");
        }
        reviewDao.deleteById(id);
        log.info("Deleted review ID = {}", id);
    }

    private void checkExistReviewAndUser(Integer id, Integer userId) {
        if (id == null || !reviewDao.checkReviewExist(id)) {
            throw new NotFoundException("Can't update review with ID = null");
        }
        if (userId == null || !userDao.checkUserExist(userId)) {
            throw new NotFoundException("Unable to like/dislike a review if the user ID = null");
        }
    }
}
