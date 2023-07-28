package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService dbReviewService;

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        return dbReviewService.create(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        return dbReviewService.update(review);
    }

    @GetMapping("/{reviewId}")
    public Review getReview(@PathVariable("reviewId") Integer id) {
        return dbReviewService.getById(id);
    }

    @GetMapping // ?filmId={filmId}&count={count}
    public List<Review> getALLReviews(@RequestParam(value = "filmId", required = false) Integer filmId,
                                   @Positive @RequestParam(value = "count", defaultValue = "10") Integer count) {
        return dbReviewService.getAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        dbReviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        dbReviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        dbReviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        dbReviewService.deleteDislike(id, userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Integer id) {
        dbReviewService.delete(id);
    }
}
