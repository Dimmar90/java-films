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
        return findById(review.getReviewId());
    }

    @Override
    public Review findById(Integer id) {
        return jdbcTemplate.queryForObject("SELECT * FROM reviews WHERE review_id = ?", this::mapRowToReview, id);
    }

    @Override
    public List<Review> findAll(Integer filmId, Integer count) {
        String sqlQuery;

        if (filmId != null) {
            sqlQuery = "SELECT * FROM reviews WHERE film_id = ? " +
                    "GROUP BY review_id ORDER BY useful DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapRowToReview, filmId, count);
        } else {
            sqlQuery = "SELECT * FROM reviews GROUP BY review_id ORDER BY useful DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapRowToReview, count);
        }
    }

    @Override
    public void delete(Integer id) {
        jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", id);
    }

    @Override
    public boolean checkExist(Integer id) throws NotFoundException {
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
