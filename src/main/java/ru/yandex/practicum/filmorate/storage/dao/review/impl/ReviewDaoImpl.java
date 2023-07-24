package ru.yandex.practicum.filmorate.storage.dao.review.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dao.review.ReviewDao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ReviewDaoImpl implements ReviewDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        String sqlQuery = "INSERT INTO reviews (content, isPositive, user_id, film_id, useful) VALUES (?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"review_id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            ps.setInt(5, 0);
            return ps;
        }, keyHolder);

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return review;
    }

    @Override
    public Review update(Review review) {
        String sqlQuery = "UPDATE reviews SET " +
                "content = ?," +
                "isPositive = ? " +
                "WHERE review_id = ?";
        jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), review.getReviewId());
        return getById(review.getReviewId());
    }

    @Override
    public Review getById(Integer id) {
        return jdbcTemplate.queryForObject("SELECT * FROM reviews WHERE review_id = ?", this::mapRowToReview, id);
    }

    @Override
    public List<Review> getAllReviews(Integer filmId, Integer count) {
        if (jdbcTemplate.queryForList("SELECT * FROM reviews", Review.class).isEmpty()) return new ArrayList<>();

        String str = filmId != null ? "WHERE film_id = ? " : "";
        String sqlQuery = "SELECT * FROM reviews " +
                str +
                "GROUP BY review_id ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToReview);
    }

    @Override
    public void deleteById(Integer id) {
        jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", id);
    }

    @Override
    public boolean checkReviewExist(Integer id) {
        String sqlQuery = "SELECT review_id FROM reviews WHERE review_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!rowSet.next()) {
            throw new NotFoundException(String.format("Review ID = %d does not exist", id));
        }
        return true;
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(rs.getInt("review_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("isPositive"))
                .userId(rs.getInt("user_id"))
                .filmId(rs.getInt("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }
}
