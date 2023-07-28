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
        String sqlQuery = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setInt(5, 0);
            return ps;
        }, keyHolder);

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return review;
    }

    @Override
    public Review update(Review review) {
        String sqlQuery = "UPDATE reviews SET " +
                "content = ?," +
                "is_positive = ? " +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), review.getReviewId());
        return findById(review.getReviewId());
    }

    @Override
    public Review findById(Long id) {
        return jdbcTemplate.queryForObject("SELECT * FROM reviews WHERE id = ?", this::mapRowToReview, id);
    }

    @Override
    public List<Review> findAll(Long filmId, Integer count) {
        String sqlQuery;

        if (filmId != null) {
            sqlQuery = "SELECT * FROM reviews WHERE film_id = ? " +
                    "GROUP BY id ORDER BY useful DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapRowToReview, filmId, count);
        } else {
            sqlQuery = "SELECT * FROM reviews GROUP BY id ORDER BY useful DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapRowToReview, count);
        }
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", id);
    }

    @Override
    public boolean checkExist(Long id) throws NotFoundException {
        String sqlQuery = "SELECT id FROM reviews WHERE id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!rowSet.next()) {
            throw new NotFoundException(String.format("Review ID = %d does not exist", id));
        }
        return true;
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(rs.getLong("id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }
}
